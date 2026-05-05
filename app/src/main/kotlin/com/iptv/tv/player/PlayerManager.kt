package com.iptv.tv.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null,
    val currentUrl: String? = null,
    val currentPosition: Long = 0L,
    val duration: Long = 0L
)

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    val player: ExoPlayer = ExoPlayer.Builder(context)
        .build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var positionTrackingJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.value = _state.value.copy(isPlaying = isPlaying)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.value = _state.value.copy(
                    isBuffering = playbackState == Player.STATE_BUFFERING
                )
            }

            override fun onPlayerError(error: PlaybackException) {
                _state.value = _state.value.copy(
                    error = error.message ?: "Erro de reprodução",
                    isPlaying = false,
                    isBuffering = false
                )
            }
        })
    }

    fun play(url: String) {
        _state.value = PlayerState(isBuffering = true, currentUrl = url)
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        startPositionTracking()
    }

    fun pause() = player.pause()
    fun resume() = player.play()

    fun seekTo(position: Long) {
        scope.launch {
            withContext(Dispatchers.Main) {
                player.seekTo(position)
            }
        }
    }

    fun stop() {
        positionTrackingJob?.cancel()
        positionTrackingJob = null
        player.stop()
        _state.value = PlayerState()
    }

    fun release() = player.release()

    private fun startPositionTracking() {
        positionTrackingJob?.cancel()
        positionTrackingJob = scope.launch {
            while (isActive) {
                withContext(Dispatchers.Main) {
                    val position = player.currentPosition
                    val dur = player.duration
                    _state.value = _state.value.copy(
                        currentPosition = if (position >= 0) position else 0L,
                        duration = if (dur > 0) dur else 0L
                    )
                }
                delay(1000L)
            }
        }
    }
}
