package com.iptv.tv.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.FavoriteEntry
import com.iptv.tv.domain.model.WatchHistoryEntry
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val favorites: List<FavoriteEntry> = emptyList(),
    val history: List<WatchHistoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = combine(
        favoritesRepository.getAllFavorites(),
        watchHistoryRepository.getRecentHistory(20)
    ) { favorites, history ->
        FavoritesUiState(
            favorites = favorites,
            history = history,
            isLoading = false
        )
    }
    .catch { e ->
        emit(FavoritesUiState(error = e.message, isLoading = false))
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    init {
        loadContent()
    }

    private fun loadContent() {
        viewModelScope.launch {
            favoritesRepository.getAllFavorites()
                .collect { favs ->
                    // Already handled by combine above
                }
        }
    }

    fun removeFavorite(streamId: String) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(streamId)
        }
    }

    fun deleteHistoryEntry(streamId: String) {
        viewModelScope.launch {
            watchHistoryRepository.deleteHistoryEntry(streamId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            watchHistoryRepository.clearHistory()
        }
    }
}