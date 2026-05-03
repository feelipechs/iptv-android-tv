package com.iptv.tv.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val error: String? = null,
    val currentUrl: String? = null
)

@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val player: ExoPlayer = ExoPlayer.Builder(context)
        .build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

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
    }

    fun pause() = player.pause()
    fun resume() = player.play()
    fun stop() {
        player.stop()
        _state.value = PlayerState()
    }

    fun release() = player.release()
}
