package com.iptv.tv.domain.repository

import com.iptv.tv.data.remote.dto.SeriesInfoResponse
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.FavoriteEntry
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.model.WatchHistoryEntry
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    /** Retorna categorias normalizadas, com cache local */
    fun getCategories(type: ContentType): Flow<List<Category>>

    /** Retorna streams de uma categoria */
    fun getStreams(categoryId: String, type: ContentType): Flow<List<Stream>>

    /** Retorna todos os streams de um tipo */
    fun getStreamsByType(type: ContentType): Flow<List<Stream>>

    /** Retorna contagem de streams por categoria */
    fun getStreamCountsByType(type: ContentType): Flow<Map<String, Int>>

    /** Força refresh da API ignorando cache */
    suspend fun refreshCategories(type: ContentType)

    /** Força refresh de streams de uma categoria ignorando cache */
    suspend fun refreshStreams(categoryId: String, type: ContentType)

    /** Valida credenciais contra o servidor */
    suspend fun validateCredentials(credentials: Credentials): Result<Unit>

    /** Retorna informações detalhadas de uma série */
    suspend fun getSeriesInfo(seriesId: Int): SeriesInfoResponse
}

interface FavoritesRepository {
    fun getAllFavorites(): Flow<List<FavoriteEntry>>
    fun getFavoritesByType(type: ContentType): Flow<List<FavoriteEntry>>
    fun isFavorite(streamId: String): Flow<Boolean>
    fun getFavoriteCountByType(type: ContentType): Flow<Int>
    fun getTotalFavoriteCount(): Flow<Int>
    suspend fun toggleFavorite(stream: Stream)
    suspend fun addFavorite(stream: Stream)
    suspend fun removeFavorite(streamId: String)
}

interface WatchHistoryRepository {
    fun getAllHistory(): Flow<List<WatchHistoryEntry>>
    fun getHistoryByType(type: ContentType): Flow<List<WatchHistoryEntry>>
    fun getRecentHistory(limit: Int): Flow<List<WatchHistoryEntry>>
    fun getHistoryCount(): Flow<Int>
    suspend fun getHistoryEntry(streamId: String): WatchHistoryEntry?
    fun observeHistoryEntry(streamId: String): Flow<WatchHistoryEntry?>
    suspend fun addToHistory(
        stream: Stream,
        progress: Float = 0f,
        episodeNum: Int? = null,
        episodeTitle: String? = null,
        season: String? = null,
        episodeUrl: String? = null
    )
    suspend fun updateProgress(streamId: String, progress: Float)
    suspend fun clearHistory()
    suspend fun deleteHistoryEntry(streamId: String)
}