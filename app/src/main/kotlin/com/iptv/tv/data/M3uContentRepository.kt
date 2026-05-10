package com.iptv.tv.data

import android.util.Log
import com.iptv.tv.data.local.dao.CategoryDao
import com.iptv.tv.data.local.dao.StreamDao
import com.iptv.tv.data.local.entity.CategoryEntity
import com.iptv.tv.data.local.entity.StreamEntity
import com.iptv.tv.data.remote.dto.SeriesInfoResponse
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.CredentialsRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3uContentRepository @Inject constructor(
    private val m3uParser: M3UParser,
    private val categoryDao: CategoryDao,
    private val streamDao: StreamDao,
    private val credentialsRepository: CredentialsRepository
) : ContentRepository {

    companion object {
        private const val TAG = "M3uContentRepository"
    }

    private var cachedParseResult: M3UParser.ParseResult? = null
    private var cachedM3uSource: String? = null

    override fun getCategories(type: ContentType): Flow<List<Category>> =
        categoryDao.getCategories(type).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getStreams(categoryId: String, type: ContentType): Flow<List<Stream>> =
        streamDao.getStreamsByCategory(categoryId, type).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getStreamsByType(type: ContentType): Flow<List<Stream>> =
        streamDao.getStreamsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getStreamCountsByType(type: ContentType): Flow<Map<String, Int>> =
        streamDao.getStreamCountsByType(type).map { list ->
            list.associate { it.categoryId to it.count }
        }

    private suspend fun getOrFetchParseResult(source: String): M3UParser.ParseResult? {
        if (cachedParseResult != null && cachedM3uSource == source) {
            Log.d(TAG, "getOrFetchParseResult: returning cached result for '$source'")
            return cachedParseResult
        }

        Log.d(TAG, "getOrFetchParseResult: fetching fresh result for '$source'")
        val parseResult = if (source.startsWith("http://") || source.startsWith("https://")) {
            m3uParser.parseFromUrl(source)
        } else {
            m3uParser.parseFromFile(source)
        }

        cachedParseResult = parseResult
        cachedM3uSource = source
        return parseResult
    }

    override suspend fun refreshCategories(type: ContentType) {
        Log.d(TAG, "refreshCategories: type=$type")

        val creds = credentialsRepository.getCredentials().first() ?: run {
            Log.e(TAG, "No credentials found")
            return
        }

        if (creds.providerType != ProviderType.M3U_LIST) {
            Log.w(TAG, "Not an M3U provider, skipping")
            return
        }

        val m3uSource = creds.m3uSource ?: run {
            Log.e(TAG, "No M3U source specified")
            return
        }

        val parseResult = try {
            getOrFetchParseResult(m3uSource)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse M3U: ${e.message}")
            return
        } ?: return

        val categoryEntities = parseResult.categories.map { cat ->
            CategoryEntity(
                id = cat.id,
                name = cat.name.trim(),
                type = ContentType.LIVE,
                streamCount = cat.streamCount
            )
        }

        categoryDao.replaceAll(ContentType.LIVE, categoryEntities)
        Log.d(TAG, "Saved ${categoryEntities.size} categories")
    }

    override suspend fun refreshStreams(categoryId: String, type: ContentType) {
        Log.d(TAG, "refreshStreams: categoryId=$categoryId, type=$type")

        val creds = credentialsRepository.getCredentials().first() ?: run {
            Log.e(TAG, "No credentials found")
            return
        }

        if (creds.providerType != ProviderType.M3U_LIST) {
            Log.w(TAG, "Not an M3U provider, skipping")
            return
        }

        val m3uSource = creds.m3uSource ?: run {
            Log.e(TAG, "No M3U source specified")
            return
        }

        val parseResult = try {
            getOrFetchParseResult(m3uSource)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse M3U: ${e.message}")
            return
        } ?: return

        val filteredStreams = if (categoryId == "all") {
            parseResult.streams
        } else {
            parseResult.streams.filter { it.categoryId == categoryId }
        }

        val streamEntities = filteredStreams.map { stream ->
            StreamEntity(
                id = stream.id,
                name = stream.name,
                categoryId = stream.categoryId,
                type = type,
                streamUrl = stream.streamUrl,
                posterUrl = stream.posterUrl,
                epgChannelId = null,
                containerExtension = stream.containerExtension
            )
        }

        streamDao.replaceByCategory(categoryId, type, streamEntities)
        Log.d(TAG, "Saved ${streamEntities.size} streams for category $categoryId")
    }

    override suspend fun validateCredentials(credentials: Credentials): Result<Unit> = runCatching {
        val source = credentials.m3uSource ?: error("Fonte M3U não especificada")

        Log.d(TAG, "Validating M3U source: '$source'")
        Log.d(TAG, "Source startsWith https: ${source.startsWith("https://")}")
        Log.d(TAG, "Source startsWith http: ${source.startsWith("http://")}")

        if (source.startsWith("http://") || source.startsWith("https://")) {
            Log.d(TAG, "Calling parseFromUrl with: '$source'")
            m3uParser.parseFromUrl(source)
            Log.d(TAG, "parseFromUrl completed successfully")
        } else {
            Log.d(TAG, "Trying as file: '$source'")
            val file = File(source)
            if (!file.exists()) error("Arquivo não encontrado: $source")
            m3uParser.parseFromFile(source)
        }

        Log.d(TAG, "M3U validation successful")
    }

    private fun CategoryEntity.toDomain() = Category(
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

    override suspend fun getSeriesInfo(seriesId: Int): SeriesInfoResponse =
        error("Séries não são suportadas em listas M3U")
}