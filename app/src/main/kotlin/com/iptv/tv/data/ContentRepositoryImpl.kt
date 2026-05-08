package com.iptv.tv.data

import android.util.Log
import com.iptv.tv.data.local.dao.CategoryDao
import com.iptv.tv.data.local.dao.StreamDao
import com.iptv.tv.data.local.entity.CategoryEntity
import com.iptv.tv.data.local.entity.StreamEntity
import com.iptv.tv.data.remote.api.XtreamApiService
import com.iptv.tv.data.remote.dto.LiveStreamDto
import com.iptv.tv.data.remote.dto.VodStreamDto
import com.iptv.tv.data.remote.dto.SeriesStreamDto
import com.iptv.tv.data.remote.dto.SeriesInfoResponse
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.CredentialsRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val api: XtreamApiService,
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao,
    private val credentialsRepository: CredentialsRepository,
    private val okHttpClient: OkHttpClient
) : ContentRepository {

    override fun getCategories(type: ContentType): Flow<List<com.iptv.tv.domain.model.Category>> =
        categoryDao.getCategories(type).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getStreams(categoryId: String, type: ContentType): Flow<List<Stream>> =
        streamDao.getStreamsByCategory(categoryId, type).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getStreamCountsByType(type: ContentType): Flow<Map<String, Int>> =
        streamDao.getStreamCountsByType(type).map { list ->
            list.associate { it.categoryId to it.count }
        }

    override suspend fun refreshCategories(type: ContentType) {
        val creds = credentialsRepository.getCredentials().first() ?: return

        val dtos = when (type) {
            ContentType.LIVE -> api.getLiveCategories(creds.username, creds.password)
            ContentType.VOD -> api.getVodCategories(creds.username, creds.password)
            ContentType.SERIES -> api.getSeriesCategories(creds.username, creds.password)
        }

    val entities = dtos.map { it.toEntity(type) }

      Log.d("ContentRepo", "refreshCategories: ${entities.size} entities")
      categoryDao.replaceAll(type, entities)
    }

    override suspend fun refreshStreams(categoryId: String, type: ContentType) {
        Log.d("ContentRepo", "refreshStreams() START: categoryId=$categoryId, type=$type")

        val creds = credentialsRepository.getCredentials().first()
        if (creds == null) {
            Log.e("ContentRepo", "refreshStreams() NO CREDENTIALS FOUND")
            return
        }

        val entities: List<StreamEntity> = when (type) {
            ContentType.LIVE -> {
                val result = api.getLiveStreams(creds.username, creds.password, categoryId = categoryId)
                result.map { (it as LiveStreamDto).toEntity(creds, type) }
            }
            ContentType.VOD -> {
                val result = api.getVodStreams(creds.username, creds.password, categoryId = categoryId)
                result.map { (it as VodStreamDto).toEntity(creds, type) }
            }
            ContentType.SERIES -> {
                val result = api.getSeriesStreams(creds.username, creds.password, categoryId = categoryId)
                result.map { (it as SeriesStreamDto).toEntity(creds, type) }
            }
        }

        Log.d("ContentRepo", "Saving ${entities.size} entities to DB for categoryId=$categoryId")
        streamDao.replaceByCategory(categoryId, type, entities)
        Log.d("ContentRepo", "refreshStreams() SUCCESS")
    }

    override suspend fun validateCredentials(credentials: Credentials): Result<Unit> =
        runCatching {
            val serverUrl = credentials.server.trimEnd('/') + "/"
            val tempRetrofit = Retrofit.Builder()
                .baseUrl(serverUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val tempApi = tempRetrofit.create(XtreamApiService::class.java)

            val response = tempApi.getUserInfo(credentials.username, credentials.password)
            val status = response.userInfo?.status ?: error("Resposta inválida do servidor")
            if (status != "Active") error("Conta inativa ou banida: $status")
        }

    override suspend fun getSeriesInfo(seriesId: Int): SeriesInfoResponse {
        val creds = credentialsRepository.getCredentials().first()
            ?: error("Credenciais não encontradas")
        return api.getSeriesInfo(creds.username, creds.password, seriesId = seriesId)
    }

    // --- Mapeamentos ---

  private fun com.iptv.tv.data.remote.dto.CategoryDto.toEntity(type: ContentType) = CategoryEntity(
    id = categoryId,
    name = categoryName.trim(),
    type = type,
    streamCount = streamCount ?: 0
  )

    private fun CategoryEntity.toDomain() = com.iptv.tv.domain.model.Category(
        id = id,
        name = name,
        type = type,
        streamCount = streamCount
    )

    private fun StreamEntity.toDomain() = Stream(
        id = id,
        name = name,
        categoryId = categoryId,
        type = type,
        streamUrl = streamUrl,
        posterUrl = posterUrl,
        epgChannelId = epgChannelId,
        containerExtension = containerExtension
    )

    private fun LiveStreamDto.toEntity(
        creds: Credentials,
        type: ContentType
    ) = StreamEntity(
        id = streamId.toString(),
        name = name,
        categoryId = categoryId,
        type = type,
        streamUrl = creds.liveUrl(streamId.toString()),
        posterUrl = streamIcon,
        epgChannelId = epgChannelId,
        containerExtension = "m3u8"
    )

    private fun VodStreamDto.toEntity(
        creds: Credentials,
        type: ContentType
    ) = StreamEntity(
        id = streamId.toString(),
        name = name,
        categoryId = categoryId,
        type = type,
        streamUrl = creds.vodUrl(streamId.toString(), containerExtension),
        posterUrl = streamIcon,
        epgChannelId = null,
        containerExtension = containerExtension ?: "mp4"
    )

    private fun SeriesStreamDto.toEntity(
        creds: Credentials,
        type: ContentType
    ) = StreamEntity(
        id = seriesId.toString(),
        name = name,
        categoryId = categoryId,
        type = type,
        streamUrl = creds.seriesUrl(seriesId.toString(), containerExtension),
        posterUrl = cover,
        epgChannelId = null,
        containerExtension = containerExtension ?: "mp4"
    )
}
