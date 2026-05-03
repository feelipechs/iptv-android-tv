package com.iptv.tv.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.data.remote.api.XtreamApiService
import com.iptv.tv.data.remote.dto.VodInfoDto
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val api: XtreamApiService
) : ViewModel() {

    private val _vodInfo = MutableStateFlow<VodInfoDto?>(null)
    val vodInfo: StateFlow<VodInfoDto?> = _vodInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadVodInfo(streamId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val creds = credentialsRepository.getCredentials().first() ?: return@launch
                _vodInfo.value = api.getVodInfo(creds.username, creds.password, vodId = streamId)
            }.onFailure {
                // ignora erro, mostra só o que tiver
            }
            _isLoading.value = false
        }
    }

    fun isFavorite(streamId: String): Flow<Boolean> =
        favoritesRepository.isFavorite(streamId)

    fun toggleFavorite(stream: Stream) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(stream) }
    }
}