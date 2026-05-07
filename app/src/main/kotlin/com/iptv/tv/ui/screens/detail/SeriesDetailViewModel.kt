package com.iptv.tv.ui.screens.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.data.remote.dto.Episode
import com.iptv.tv.data.remote.dto.SeriesInfo
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
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
    val isLoading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val lastEpisodeUrl: String? = null,
    val lastEpisodeNum: Int? = null,
    val lastSeason: String? = null,
    val episodeProgress: Map<String, Float> = emptyMap()
)

@HiltViewModel
class SeriesDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
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
        viewModelScope.launch {
            loadHistory()
            loadSeriesInfo()
            loadEpisodeProgress()
            observeFavorite()
        }
    }

    private suspend fun loadHistory() {
        try {
            val history = watchHistoryRepository.getAllHistory().first()
            val entry = history.firstOrNull { it.streamId == streamId }
            _uiState.value = _uiState.value.copy(
                lastEpisodeUrl = entry?.lastEpisodeUrl,
                lastEpisodeNum = entry?.lastEpisodeNum,
                lastSeason = entry?.lastSeason
            )
        } catch (e: Exception) {
        }
    }

    private suspend fun loadEpisodeProgress() {
        val episodes = _uiState.value.episodes?.values?.flatten() ?: emptyList()
        val progressMap = mutableMapOf<String, Float>()
        episodes.forEach { episode ->
            val url = episode.directSource ?: return@forEach
            val epEntry = watchHistoryRepository.getHistoryEntry(url)
            if (epEntry != null && epEntry.progress > 0f) {
                progressMap[url] = epEntry.progress
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
            val firstSeason = response.episodes?.keys?.minOrNull() ?: ""
            val selectedSeason = if (!historySeason.isNullOrBlank() &&
                response.episodes?.containsKey(historySeason) == true
            ) historySeason
            else firstSeason

            _uiState.value = _uiState.value.copy(
                seriesInfo = response.info,
                episodes = response.episodes,
                selectedSeason = selectedSeason,
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
            watchHistoryRepository.addToHistory(
                stream = s,
                progress = 0f,
                episodeNum = episode.episodeNum,
                episodeTitle = episode.title,
                season = season,
                episodeUrl = episode.directSource
            )
            loadHistory()
            loadEpisodeProgress()
        }
    }

    fun selectSeason(season: String) {
        _uiState.value = _uiState.value.copy(selectedSeason = season)
    }

    fun getEpisodesForSelectedSeason(): List<Episode> {
        val season = _uiState.value.selectedSeason
        return _uiState.value.episodes?.get(season) ?: emptyList()
    }

    fun getResumeUrl(): String? {
        val fromHistory = _uiState.value.lastEpisodeUrl
        if (!fromHistory.isNullOrBlank()) return fromHistory

        val firstSeason = _uiState.value.episodes?.keys?.minOrNull()
        return _uiState.value.episodes?.get(firstSeason)?.firstOrNull()?.directSource
    }

    fun getResumeEpisode(): Pair<Episode, String>? {
        val lastSeason = _uiState.value.lastSeason
        val lastEpNum = _uiState.value.lastEpisodeNum
        if (lastSeason != null && lastEpNum != null) {
            val ep = _uiState.value.episodes?.get(lastSeason)
                ?.firstOrNull { it.episodeNum == lastEpNum }
            if (ep != null) return Pair(ep, lastSeason)
        }
        val firstSeason = _uiState.value.episodes?.keys?.minOrNull() ?: return null
        val firstEp = _uiState.value.episodes?.get(firstSeason)?.firstOrNull() ?: return null
        return Pair(firstEp, firstSeason)
    }
}