package com.iptv.tv.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.di.ApplicationScope
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.WatchHistoryRepository
import com.iptv.tv.player.PlayerManager
import com.iptv.tv.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager,
    private val watchHistoryRepository: WatchHistoryRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    private val streamId: String = URLDecoder.decode(savedStateHandle["streamId"] ?: "", "UTF-8")
    private val streamUrl: String = URLDecoder.decode(savedStateHandle["streamUrl"] ?: "", "UTF-8")
    private val streamName: String = URLDecoder.decode(savedStateHandle["streamName"] ?: "", "UTF-8")
    private val streamType: ContentType = try {
        ContentType.valueOf(URLDecoder.decode(savedStateHandle["streamType"] ?: "LIVE", "UTF-8"))
    } catch (e: Exception) { ContentType.LIVE }
    private val startPosition: Long = savedStateHandle["startPosition"] ?: 0L
    private val seriesId: String = URLDecoder.decode(savedStateHandle["seriesId"] ?: "", "UTF-8")
    private val posterUrl: String = URLDecoder.decode(savedStateHandle["posterUrl"] ?: "", "UTF-8")
    private val episodeSeason: String = URLDecoder.decode(savedStateHandle["episodeSeason"] ?: "", "UTF-8")
    private val episodeNum: String = URLDecoder.decode(savedStateHandle["episodeNum"] ?: "", "UTF-8")
    private val episodeTitle: String = URLDecoder.decode(savedStateHandle["episodeTitle"] ?: "", "UTF-8")

    val playerState: StateFlow<PlayerState> = playerManager.state
    val player get() = playerManager.player
    val currentPosition: Long get() = playerManager.state.value.currentPosition
    val duration: Long get() = playerManager.state.value.duration

    fun play(url: String, startPosition: Long = 0L) {
        viewModelScope.launch {
            playerManager.play(url)
            if (startPosition == -1L) {
                resumeFromHistory()
            } else if (startPosition > 0L) {
                playerManager.seekTo(startPosition)
            }
        }
    }

    private suspend fun resumeFromHistory() {
        val historyLookupId = if (streamType == ContentType.SERIES && seriesId.isNotBlank()) seriesId else streamId
        if (historyLookupId.isBlank()) return
        val entry = watchHistoryRepository.getHistoryEntry(historyLookupId) ?: return
        if (entry.progress <= 0f) return
        for (i in 1..30) {
            val dur = playerManager.state.value.duration
            if (dur > 0L) {
                playerManager.seekTo((entry.progress * dur).toLong())
                return
            }
            delay(500L)
        }
    }

    fun togglePlayPause() {
        if (playerManager.state.value.isPlaying) playerManager.pause()
        else playerManager.resume()
    }

    fun seekTo(position: Long) = playerManager.seekTo(position)

    override fun onCleared() {
        if (streamId.isNotBlank() && duration > 0L) {
            val pos = currentPosition
            val dur = duration
            val progress = if (dur > 0L) pos.toFloat() / dur.toFloat() else 0f
            val historyId = if (streamType == ContentType.SERIES && seriesId.isNotBlank()) seriesId else streamId
        applicationScope.launch {
            val existingEntry = watchHistoryRepository.getHistoryEntry(historyId)
            val stream = Stream(
                id = historyId,
                name = streamName,
                categoryId = existingEntry?.categoryId ?: "",
                type = streamType,
                streamUrl = streamUrl,
                posterUrl = existingEntry?.posterUrl ?: posterUrl.ifBlank { null }
            )
            if (streamType == ContentType.SERIES && episodeSeason.isNotBlank()) {
                watchHistoryRepository.addToHistory(
                    stream = stream,
                    progress = progress,
                    episodeNum = episodeNum.toIntOrNull(),
                    episodeTitle = episodeTitle.ifBlank { null },
                    season = episodeSeason,
                    episodeUrl = streamUrl
                )
        } else {
            watchHistoryRepository.addToHistory(stream, progress)
        }
        }
        }
        playerManager.stop()
        super.onCleared()
    }
}
