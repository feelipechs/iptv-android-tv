package com.iptv.tv.data

import com.iptv.tv.data.local.dao.FavoriteDao
import com.iptv.tv.data.local.dao.WatchHistoryDao
import com.iptv.tv.data.local.entity.FavoriteEntity
import com.iptv.tv.data.local.entity.WatchHistoryEntity
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.FavoriteEntry
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.model.WatchHistoryEntry
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao
) : FavoritesRepository {

    override fun getAllFavorites(): Flow<List<FavoriteEntry>> =
        favoriteDao.getAllFavorites().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getFavoritesByType(type: ContentType): Flow<List<FavoriteEntry>> =
        favoriteDao.getFavoritesByType(type).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun isFavorite(streamId: String): Flow<Boolean> =
        favoriteDao.isFavorite(streamId)

    override fun getFavoriteCountByType(type: ContentType): Flow<Int> =
        favoriteDao.getFavoriteCountByType(type)

    override fun getTotalFavoriteCount(): Flow<Int> =
        favoriteDao.getTotalFavoriteCount()

    override suspend fun toggleFavorite(stream: Stream) {
        favoriteDao.toggle(
            streamId = stream.id,
            name = stream.name,
            categoryId = stream.categoryId,
            type = stream.type,
            posterUrl = stream.posterUrl,
            streamUrl = stream.streamUrl
        )
    }

    override suspend fun addFavorite(stream: Stream) {
        favoriteDao.insert(stream.toFavoriteEntity())
    }

    override suspend fun removeFavorite(streamId: String) {
        favoriteDao.delete(streamId)
    }

    private fun FavoriteEntity.toDomain() = FavoriteEntry(
        streamId = streamId,
        name = name,
        categoryId = categoryId,
        type = type,
        posterUrl = posterUrl,
        streamUrl = streamUrl,
        addedAt = addedAt
    )

    private fun Stream.toFavoriteEntity() = FavoriteEntity(
        streamId = id,
        name = name,
        categoryId = categoryId,
        type = type,
        posterUrl = posterUrl,
        streamUrl = streamUrl
    )
}

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : WatchHistoryRepository {

    override fun getAllHistory(): Flow<List<WatchHistoryEntry>> =
        watchHistoryDao.getAllHistory().map { entities ->
            entities.filter { it.name.isNotBlank() }.map { it.toDomain() }
        }

    override fun getHistoryByType(type: ContentType): Flow<List<WatchHistoryEntry>> =
        watchHistoryDao.getHistoryByType(type).map { entities ->
            entities.filter { it.name.isNotBlank() }.map { it.toDomain() }
        }

    override fun getRecentHistory(limit: Int): Flow<List<WatchHistoryEntry>> =
        watchHistoryDao.getRecentHistory(limit).map { entities ->
            entities.filter { it.name.isNotBlank() }.map { it.toDomain() }
        }

    override fun getHistoryCount(): Flow<Int> =
        watchHistoryDao.getHistoryCount()

    override suspend fun addToHistory(
        stream: Stream,
        progress: Float,
        episodeNum: Int?,
        episodeTitle: String?,
        season: String?,
        episodeUrl: String?
    ) {
        if (stream.name.isBlank() || stream.id.isBlank()) return
        watchHistoryDao.addToHistory(
            streamId = stream.id,
            name = stream.name,
            categoryId = stream.categoryId,
            type = stream.type,
            posterUrl = stream.posterUrl,
            streamUrl = stream.streamUrl,
            progress = progress,
            lastEpisodeNum = episodeNum,
            lastEpisodeTitle = episodeTitle,
            lastSeason = season,
            lastEpisodeUrl = episodeUrl
        )
    }

    override suspend fun updateProgress(streamId: String, progress: Float) {
        watchHistoryDao.updateProgress(streamId, progress)
    }

    override suspend fun clearHistory() {
        watchHistoryDao.clearAll()
    }

    override suspend fun deleteHistoryEntry(streamId: String) {
        watchHistoryDao.delete(streamId)
    }

    private fun WatchHistoryEntity.toDomain() = WatchHistoryEntry(
        streamId = streamId,
        name = name,
        categoryId = categoryId,
        type = type,
        posterUrl = posterUrl,
        streamUrl = streamUrl,
        lastWatchedAt = lastWatchedAt,
        progress = progress,
        lastEpisodeNum = lastEpisodeNum,
        lastEpisodeTitle = lastEpisodeTitle,
        lastSeason = lastSeason,
        lastEpisodeUrl = lastEpisodeUrl
    )
}