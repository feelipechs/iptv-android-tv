package com.iptv.tv.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.player.PlayerManager
import com.iptv.tv.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = playerManager.state
    val player get() = playerManager.player

    fun play(url: String) {
        viewModelScope.launch { playerManager.play(url) }
    }

    fun togglePlayPause() {
        if (playerManager.state.value.isPlaying) playerManager.pause()
        else playerManager.resume()
    }

    override fun onCleared() {
        playerManager.stop()
        super.onCleared()
    }
}
