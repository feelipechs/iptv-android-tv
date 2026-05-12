package com.iptv.tv.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.data.remote.dto.Episode
import com.iptv.tv.data.remote.dto.SeriesInfo
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SeriesDetailUiState(
    val seriesInfo: SeriesInfo? = null,
    val episodes: Map<String, List<Episode>>? = null,
    val selectedSeason: String = "",
    val episodesForSelectedSeason: List<Episode> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val lastEpisodeUrl: String? = null,
    val lastEpisodeNum: Int? = null,
    val lastSeason: String? = null,
    val episodeProgress: Map<String, Float> = emptyMap(),
    val episodeStreamUrls: Map<String, String> = emptyMap()
)

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val credentialsRepository: CredentialsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SeriesDetailUiState())
    val uiState: StateFlow<SeriesDetailUiState> = _uiState.asStateFlow()

    private val streamId: String = java.net.URLDecoder.decode(
        savedStateHandle.get<String>("streamId") ?: "", "UTF-8"
    )

    private var stream: Stream? = null

    fun setStream(s: Stream) {
        stream = s
    }

    init {
        loadHistory()
        viewModelScope.launch {
            loadSeriesInfo()
            loadEpisodeProgress()
        }
        viewModelScope.launch {
            observeFavorite()
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            watchHistoryRepository.observeHistoryEntry(streamId).collect { entry ->
        _uiState.value = _uiState.value.copy(
            lastEpisodeUrl = entry?.lastEpisodeUrl,
            lastEpisodeNum = entry?.lastEpisodeNum,
            lastSeason = entry?.lastSeason
        )
            }
        }
    }

    private suspend fun loadEpisodeProgress() {
        val episodes = _uiState.value.episodes?.values?.flatten() ?: emptyList()
        val progressMap = mutableMapOf<String, Float>()
        episodes.forEach { episode ->
            val entry = watchHistoryRepository.getHistoryEntry(episode.id)
            if (entry != null && entry.progress > 0f) {
                progressMap[episode.id] = entry.progress
            }
        }
        _uiState.value = _uiState.value.copy(episodeProgress = progressMap)
    }

    private suspend fun loadSeriesInfo() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        runCatching {
            val seriesId = streamId.toIntOrNull() ?: return
            val response = contentRepository.getSeriesInfo(seriesId)

            val historySeason = _uiState.value.lastSeason
            val firstSeason = response.episodes?.keys
            ?.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
            ?.firstOrNull() ?: ""
            val selectedSeason = if (!historySeason.isNullOrBlank() &&
                response.episodes?.containsKey(historySeason) == true
            ) historySeason
            else firstSeason

        val urlMap = mutableMapOf<String, String>()
        response.episodes?.values?.flatten()?.forEach { ep ->
            urlMap[ep.id] = getEpisodeStreamUrl(ep)
        }

        _uiState.value = _uiState.value.copy(
            seriesInfo = response.info,
            episodes = response.episodes,
            selectedSeason = selectedSeason,
            episodesForSelectedSeason = response.episodes?.get(selectedSeason) ?: emptyList(),
            episodeStreamUrls = urlMap,
        isLoading = false
        )
    }.onFailure { e ->
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Erro ao carregar série"
            )
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            favoritesRepository.isFavorite(streamId).collect { isFav ->
                _uiState.value = _uiState.value.copy(isFavorite = isFav)
            }
        }
    }

    fun toggleFavorite(s: Stream) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(s)
        }
    }

    fun addToHistory(s: Stream, episode: Episode, season: String) {
        viewModelScope.launch {
        val existing = watchHistoryRepository.getHistoryEntry(s.id)
        if (existing != null && existing.lastSeason == season && existing.lastEpisodeNum == episode.episodeNum.toIntOrNull()) {
            return@launch
        }
            val epUrl = getEpisodeStreamUrl(episode)
            watchHistoryRepository.addToHistory(
                stream = s,
                progress = 0f,
                episodeNum = episode.episodeNum.toIntOrNull(),
                episodeTitle = episode.title,
                season = season,
                episodeUrl = epUrl
            )
            loadHistory()
            loadEpisodeProgress()
        }
    }

    fun selectSeason(season: String) {
        val eps = _uiState.value.episodes?.get(season) ?: emptyList()
        _uiState.value = _uiState.value.copy(
            selectedSeason = season,
            episodesForSelectedSeason = eps
        )
    }

suspend fun getResumeUrl(): String? {
    val fromHistory = _uiState.value.lastEpisodeUrl
    if (!fromHistory.isNullOrBlank()) return fromHistory

    val firstSeason = _uiState.value.episodes?.keys
        ?.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
        ?.firstOrNull()
        val firstEp = _uiState.value.episodes?.get(firstSeason)?.firstOrNull() ?: return null
        return getEpisodeStreamUrl(firstEp)
    }

    fun getResumeEpisode(): Pair<Episode, String>? {
        val lastSeason = _uiState.value.lastSeason
        val lastEpNum = _uiState.value.lastEpisodeNum
        if (lastSeason != null && lastEpNum != null) {
            val ep = _uiState.value.episodes?.get(lastSeason)
            ?.firstOrNull { it.episodeNum.toIntOrNull() == lastEpNum }
            if (ep != null) return Pair(ep, lastSeason)
        }
        val firstSeason = _uiState.value.episodes?.keys
        ?.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
        ?.firstOrNull() ?: return null
        val firstEp = _uiState.value.episodes?.get(firstSeason)?.firstOrNull() ?: return null
        return Pair(firstEp, firstSeason)
    }

    suspend fun getEpisodeStreamUrl(episode: Episode): String {
        if (!episode.directSource.isNullOrBlank()) return episode.directSource
        val creds = credentialsRepository.getCredentials().first() ?: return ""
        return creds.seriesUrl(episode.id, episode.containerExtension)
    }
}