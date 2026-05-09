package com.iptv.tv.ui.screens.stream

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.FavoriteEntry
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.model.WatchHistoryEntry
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import com.iptv.tv.domain.usecase.GetStreamsUseCase
import com.iptv.tv.domain.usecase.RefreshStreamsUseCase
import com.iptv.tv.ui.screens.home.FAVORITES_CATEGORY_ID
import com.iptv.tv.ui.screens.home.RECENTS_CATEGORY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StreamUiState(
    val streams: List<Stream> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val favoriteIds: Set<String> = emptySet(),
    val searchQuery: String = ""
)

@HiltViewModel
class StreamViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getStreamsUseCase: GetStreamsUseCase,
    private val refreshStreamsUseCase: RefreshStreamsUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    private val categoryId: String = try {
        java.net.URLDecoder.decode(savedStateHandle.get<String>("categoryId") ?: "", "UTF-8")
    } catch (_: Exception) { savedStateHandle.get<String>("categoryId") ?: "" }

    private val type: ContentType = try {
        ContentType.valueOf(savedStateHandle.get<String>("type") ?: "LIVE")
    } catch (_: Exception) { ContentType.LIVE }

    private val isSpecialCategory = categoryId == FAVORITES_CATEGORY_ID || categoryId == RECENTS_CATEGORY_ID

    private val _searchQuery = MutableStateFlow("")
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _streams = MutableStateFlow<List<Stream>>(emptyList())

    val uiState: StateFlow<StreamUiState> = combine(
        _streams,
        _searchQuery,
        _favoriteIds,
        _isLoading,
        _error
    ) { streams, query, favIds, loading, error ->
        val filtered = if (query.isBlank()) streams else streams.filter { it.name.contains(query, ignoreCase = true) }
        StreamUiState(
            streams = filtered,
            isLoading = loading,
            error = error,
            favoriteIds = favIds,
            searchQuery = query
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StreamUiState())

    init {
        loadStreams()
        observeFavorites()
    }

    private fun loadStreams() {
        viewModelScope.launch {
            _isLoading.value = true
            when (categoryId) {
                FAVORITES_CATEGORY_ID -> {
                    favoritesRepository.getFavoritesByType(type).collect { favs: List<FavoriteEntry> ->
                        _streams.value = favs.map { fav: FavoriteEntry ->
                            Stream(fav.streamId, fav.name, fav.categoryId, fav.type, fav.streamUrl, fav.posterUrl)
                        }
                        _isLoading.value = false
                    }
                }
                RECENTS_CATEGORY_ID -> {
                    watchHistoryRepository.getHistoryByType(type).collect { history: List<WatchHistoryEntry> ->
                        _streams.value = history.map { entry: WatchHistoryEntry ->
                            Stream(
                                entry.streamId, entry.name, entry.categoryId, entry.type, entry.streamUrl, entry.posterUrl,
                                progress = entry.progress,
                                lastEpisodeNum = entry.lastEpisodeNum,
                                lastEpisodeTitle = entry.lastEpisodeTitle,
                                lastSeason = entry.lastSeason,
                                lastEpisodeUrl = entry.lastEpisodeUrl
                            )
                        }
                        _isLoading.value = false
                    }
                }
                else -> {
                    getStreamsUseCase(categoryId, type).collect { streams ->
                        _streams.value = streams
                        _isLoading.value = false
                    }
                    refreshStreams()
                }
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesRepository.getFavoritesByType(type).collect { favs ->
                _favoriteIds.value = favs.map { it.streamId }.toSet()
            }
        }
    }

    private fun refreshStreams() {
        viewModelScope.launch {
            runCatching { refreshStreamsUseCase(categoryId, type) }
                .onFailure { e -> _error.value = e.message }
        }
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun toggleFavorite(stream: Stream) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(stream)
        }
    }

    fun recordToHistory(stream: Stream) {
        viewModelScope.launch {
            watchHistoryRepository.addToHistory(stream, 0f)
        }
    }
}
