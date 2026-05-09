package com.iptv.tv.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.iptv.tv.data.ContentRepositoryImpl
import com.iptv.tv.data.CredentialsRepositoryImpl
import com.iptv.tv.data.FavoritesRepositoryImpl
import com.iptv.tv.data.M3uContentRepository
import com.iptv.tv.data.M3UParser
import com.iptv.tv.data.WatchHistoryRepositoryImpl
import com.iptv.tv.data.local.AppDatabase
import com.iptv.tv.data.local.MIGRATION_1_2
import com.iptv.tv.data.local.MIGRATION_2_3
import com.iptv.tv.data.local.MIGRATION_3_4
import com.iptv.tv.data.local.dao.CategoryDao
import com.iptv.tv.data.local.dao.FavoriteDao
import com.iptv.tv.data.local.dao.StreamDao
import com.iptv.tv.data.local.dao.WatchHistoryDao
import com.iptv.tv.data.remote.api.XtreamApiService
import com.iptv.tv.data.remote.dto.SeriesInfoResponse
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "iptv_credentials")

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        serverUrlInterceptor: ServerUrlInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(serverUrlInterceptor)
        .apply {
            if (com.iptv.tv.BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("http://placeholder.invalid/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideXtreamApi(retrofit: Retrofit): XtreamApiService =
        retrofit.create(XtreamApiService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "iptv_cache.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideStreamDao(db: AppDatabase): StreamDao = db.streamDao()
    @Provides fun provideFavoriteDao(db: AppDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideWatchHistoryDao(db: AppDatabase): WatchHistoryDao = db.watchHistoryDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}

@Module
@InstallIn(SingletonComponent::class)
object M3uModule {

    @Provides
    @Singleton
    fun provideM3UParser(@ApplicationContext context: Context): M3UParser =
        M3UParser(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCredentialsRepository(impl: CredentialsRepositoryImpl): CredentialsRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindWatchHistoryRepository(impl: WatchHistoryRepositoryImpl): WatchHistoryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ContentRepositoryModule {

    @Provides
    @Singleton
    fun provideContentRepository(
        xtreamRepo: ContentRepositoryImpl,
        m3uRepo: M3uContentRepository,
        credentialsRepository: CredentialsRepository,
        @ApplicationScope applicationScope: CoroutineScope
    ): ContentRepository = DelegatingContentRepository(xtreamRepo, m3uRepo, credentialsRepository, applicationScope)
}

private class DelegatingContentRepository(
    private val xtreamRepo: ContentRepositoryImpl,
    private val m3uRepo: M3uContentRepository,
    credentialsRepository: CredentialsRepository,
    applicationScope: CoroutineScope
) : ContentRepository {

    @Volatile
    private var delegate: ContentRepository = xtreamRepo

    init {
        applicationScope.launch {
            credentialsRepository.getCredentials().collect { credentials ->
                val newDelegate = when (credentials?.providerType) {
                    ProviderType.M3U_LIST -> m3uRepo
                    else -> xtreamRepo
                }
                delegate = newDelegate
            }
        }
    }

    override fun getCategories(type: com.iptv.tv.domain.model.ContentType) =
        delegate.getCategories(type)

    override fun getStreams(categoryId: String, type: com.iptv.tv.domain.model.ContentType) =
        delegate.getStreams(categoryId, type)

    override fun getStreamCountsByType(type: com.iptv.tv.domain.model.ContentType) =
        delegate.getStreamCountsByType(type)

    override suspend fun refreshCategories(type: com.iptv.tv.domain.model.ContentType) =
        delegate.refreshCategories(type)

    override suspend fun refreshStreams(categoryId: String, type: com.iptv.tv.domain.model.ContentType) =
        delegate.refreshStreams(categoryId, type)

    override suspend fun validateCredentials(credentials: com.iptv.tv.domain.model.Credentials): Result<Unit> {
        val type = credentials.providerType
        return when (type) {
            ProviderType.M3U_LIST -> m3uRepo.validateCredentials(credentials)
            ProviderType.XTREAM -> xtreamRepo.validateCredentials(credentials)
        }
    }

    override suspend fun getSeriesInfo(seriesId: Int): SeriesInfoResponse =
        xtreamRepo.getSeriesInfo(seriesId)
}