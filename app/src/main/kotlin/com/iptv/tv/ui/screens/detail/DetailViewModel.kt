package com.iptv.tv.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.data.remote.api.XtreamApiService
import com.iptv.tv.data.remote.dto.VodInfoDto
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailUiState(
    val vodInfo: VodInfoDto? = null,
    val isLoading: Boolean = false,
    val savedProgress: Float? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val credentialsRepository: CredentialsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val api: XtreamApiService
) : ViewModel() {

    private val _vodInfo = MutableStateFlow<VodInfoDto?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _savedProgress = MutableStateFlow<Float?>(null)
    private val _isFavorite = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DetailUiState> = combine(
        _vodInfo, _isLoading, _savedProgress, _isFavorite, _error
    ) { vodInfo, isLoading, savedProgress, isFavorite, error ->
        DetailUiState(
            vodInfo = vodInfo,
            isLoading = isLoading,
            savedProgress = savedProgress,
            isFavorite = isFavorite,
            error = error
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun loadVodInfo(streamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val creds = credentialsRepository.getCredentials().first() ?: return@launch
                // TODO: substituir por contentRepository.getVodInfo() quando disponível
                _vodInfo.value = api.getVodInfo(
                    creds.username,
                    creds.password,
                    vodId = streamId
                )
        }.onFailure {
            android.util.Log.e("DetailViewModel", "Erro ao carregar VOD", it)
            _error.value = it.message
        }
            _isLoading.value = false
        }
        loadProgress(streamId)
    }

    fun observeFavorite(streamId: String) {
        viewModelScope.launch {
            favoritesRepository.isFavorite(streamId).collect { _isFavorite.value = it }
        }
    }

    private fun loadProgress(streamId: String) {
        viewModelScope.launch {
            watchHistoryRepository.observeHistoryEntry(streamId).collect { entry ->
                val progress = if (entry != null && entry.progress > 0f) entry.progress else null
                _savedProgress.value = progress
                android.util.Log.d("DetailVM", "streamId=$streamId, savedProgress=$progress")
            }
        }
    }

    fun toggleFavorite(stream: Stream) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(stream) }
    }
}
