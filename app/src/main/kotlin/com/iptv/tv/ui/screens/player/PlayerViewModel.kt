package com.iptv.tv.ui.screens.player

import android.util.Log
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

    private val streamId: String = savedStateHandle["streamId"] ?: ""
    private val streamUrl: String = savedStateHandle["streamUrl"] ?: ""
    private val streamName: String = savedStateHandle["streamName"] ?: ""
    private val startPosition: Long = savedStateHandle["startPosition"] ?: 0L

    val playerState: StateFlow<PlayerState> = playerManager.state
    val player get() = playerManager.player
    val currentPosition: Long get() = playerManager.state.value.currentPosition
    val duration: Long get() = playerManager.state.value.duration

    fun play(url: String, startPosition: Long = 0L) {
        Log.d("VodDebug", "play() streamId='$streamId' startPosition=$startPosition")
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
        if (streamId.isBlank()) return
        val entry = watchHistoryRepository.getHistoryEntry(streamId) ?: return
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
        Log.d("VodDebug", "onCleared() streamId='$streamId' duration=$duration position=$currentPosition")
        if (streamId.isNotBlank() && duration > 0L) {
            val pos = currentPosition
            val dur = duration
            val progress = if (dur > 0L) pos.toFloat() / dur.toFloat() else 0f
            Log.d("VodDebug", "Salvando: progress=$progress streamId='$streamId'")
            val stream = Stream(
                id = streamId,
                name = streamName,
                categoryId = "",
                type = ContentType.VOD,
                streamUrl = streamUrl,
                posterUrl = null
            )
            applicationScope.launch {
                watchHistoryRepository.addToHistory(stream, progress)
            }
        } else {
            Log.d("VodDebug", "NÃO salvou: streamId='$streamId' duration=$duration")
        }
        playerManager.stop()
        super.onCleared()
    }
}
