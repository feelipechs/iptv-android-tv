package com.iptv.tv.ui.screens.content

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import com.iptv.tv.domain.usecase.GetStreamsUseCase
import com.iptv.tv.domain.usecase.RefreshStreamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContentUiState(
    val streams: List<Stream> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val favoriteStreamIds: Set<String> = emptySet()
)

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val getStreamsUseCase: GetStreamsUseCase,
    private val refreshStreamsUseCase: RefreshStreamsUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: String = savedStateHandle["categoryId"] ?: ""
    private val contentType: ContentType =
        ContentType.valueOf(savedStateHandle.get<String>("type") ?: "LIVE")

    private val _favoriteStreamIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<ContentUiState> = getStreamsUseCase(categoryId, contentType)
        .onEach { streams -> Log.d("ContentViewModel", "uiState emitting ${streams.size} streams for categoryId=$categoryId, type=$contentType") }
        .map { streams ->
            val favIds = _favoriteStreamIds.value
            val streamsWithFav = streams.map { it.copy(isFavorite = favIds.contains(it.id)) }
            ContentUiState(streams = streamsWithFav, isLoading = false)
        }
        .catch { e ->
            Log.e("ContentViewModel", "uiState ERROR: ${e.message}", e)
            emit(ContentUiState(error = e.message, isLoading = false))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ContentUiState())

    init {
        Log.d("ContentViewModel", "=== INIT: categoryId=$categoryId, contentType=$contentType ===")
        refreshStreams()
        observeFavorites()
    }

    private fun observeFavorites() {
        favoritesRepository.getFavoritesByType(contentType)
            .onEach { favorites ->
                _favoriteStreamIds.value = favorites.map { it.streamId }.toSet()
            }
            .launchIn(viewModelScope)
    }

    private fun refreshStreams() {
        Log.d("ContentViewModel", "refreshStreams() called: categoryId=$categoryId, contentType=$contentType")
        viewModelScope.launch {
            runCatching { refreshStreamsUseCase(categoryId, contentType) }
                .onSuccess {
                    Log.d("ContentViewModel", "refreshStreams() SUCCESS for $contentType")
                }
                .onFailure { e ->
                    Log.e("ContentViewModel", "refreshStreams() FAILED: ${e.message}", e)
                }
        }
    }

    fun toggleFavorite(stream: Stream) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(stream)
        }
    }

    fun recordToHistory(stream: Stream) {
        if (stream.type != ContentType.LIVE) return
        viewModelScope.launch {
            watchHistoryRepository.addToHistory(stream, 0f)
        }
    }
}
