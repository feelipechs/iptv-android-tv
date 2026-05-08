package com.iptv.tv.domain.usecase

import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
  private val repository: ContentRepository
) {
  operator fun invoke(type: ContentType): Flow<List<Category>> =
    repository.getCategories(type)

  fun getStreamCountsByType(type: ContentType): Flow<Map<String, Int>> =
    repository.getStreamCountsByType(type)
}

class GetStreamsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(categoryId: String, type: ContentType): Flow<List<Stream>> =
        repository.getStreams(categoryId, type)
}

class RefreshContentUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType) =
        repository.refreshCategories(type)
}

class RefreshStreamsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(categoryId: String, type: ContentType) =
        repository.refreshStreams(categoryId, type)
}

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke(type: ContentType? = null) = if (type != null) {
        repository.getFavoritesByType(type)
    } else {
        repository.getAllFavorites()
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(stream: Stream) =
        repository.toggleFavorite(stream)
}

class GetWatchHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    operator fun invoke(type: ContentType? = null, limit: Int? = null) = when {
        limit != null -> repository.getRecentHistory(limit)
        type != null -> repository.getHistoryByType(type)
        else -> repository.getAllHistory()
    }
}

class AddToHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(stream: Stream, progress: Float = 0f) =
        repository.addToHistory(stream, progress)
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}
