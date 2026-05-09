package com.iptv.tv.ui.screens.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView
import androidx.tv.material3.*
import androidx.compose.ui.graphics.Color
import com.iptv.tv.domain.model.ContentType
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    streamId: String,
    streamUrl: String,
    streamName: String = "",
    streamType: ContentType = ContentType.LIVE,
    startPosition: Long = 0L,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val state by viewModel.playerState.collectAsStateWithLifecycle()

    var showControls by remember { mutableStateOf(false) }
    var autoHideKey by remember { mutableStateOf(0L) }

    val rootFocusRequester = remember { FocusRequester() }
    val controlsFocusRequester = remember { FocusRequester() }
    val errorFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        rootFocusRequester.requestFocus()
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            controlsFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(showControls, autoHideKey) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            delay(100)
            try { errorFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(streamUrl) {
        viewModel.play(streamUrl, startPosition)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .focusRequester(rootFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onKeyEvent false

                when {
keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter -> {
                    if (state.error != null) return@onKeyEvent false
                    if (!showControls) {
                            showControls = true
                            autoHideKey = System.currentTimeMillis()
                        } else {
                            autoHideKey = System.currentTimeMillis()
                        }
                        true
                    }
                    keyEvent.key == Key.Back -> {
                        if (showControls) {
                            showControls = false
                            rootFocusRequester.requestFocus()
                            true
                        } else {
                            false
                        }
                    }
                    keyEvent.key == Key.DirectionLeft || keyEvent.key == Key.DirectionRight -> {
                        if (showControls) {
                            autoHideKey = System.currentTimeMillis()
                        }
                        false
                    }
                    else -> false
                }
            }
    ) {
AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player = viewModel.player
                useController = false
                setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    if (state.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.focusGroup()
            ) {
                Text(
                    text = "Erro: ${state.error}",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    onClick = { viewModel.play(streamUrl) },
                    modifier = Modifier
                        .focusRequester(errorFocusRequester)
                        .clip(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
              containerColor = MaterialTheme.colorScheme.primary,
              focusedContainerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
              focusedContentColor = MaterialTheme.colorScheme.onPrimary,
              pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
              pressedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
          ) {
            Box(Modifier.padding(horizontal = 24.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
              Text("Tentar novamente")
            }
          }
          Spacer(Modifier.height(8.dp))
          Surface(
            onClick = onBack,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
              focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              pressedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
              pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
          ) {
            Box(Modifier.padding(horizontal = 24.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
              Text("Voltar")
            }
          }
        }
            }
        }

        if (state.isBuffering && state.error == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
      text = "Carregando stream…",
      color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }

if (showControls) {
        if (streamName.isNotBlank()) {
            Text(
                text = streamName,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
        }
        PlayerControlsOverlay(
                isPlaying = state.isPlaying,
                currentPosition = state.currentPosition,
                duration = state.duration,
                onPlayPause = {
                    viewModel.togglePlayPause()
                    autoHideKey = System.currentTimeMillis()
                },
                onSeekForward = {
                    val newPos = (state.currentPosition + 10_000L).coerceAtMost(state.duration)
                    viewModel.seekTo(newPos)
                    autoHideKey = System.currentTimeMillis()
                },
                onSeekBackward = {
                    val newPos = (state.currentPosition - 10_000L).coerceAtLeast(0L)
                    viewModel.seekTo(newPos)
                    autoHideKey = System.currentTimeMillis()
                },
                onBack = {
                    showControls = false
                    rootFocusRequester.requestFocus()
                },
                controlsFocusRequester = controlsFocusRequester,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun PlayerControlsOverlay(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onBack: () -> Unit,
    controlsFocusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f))
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        SeekBar(
            currentPosition = currentPosition,
            duration = duration
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.focusGroup(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlayerControlButton(
                onClick = onBack,
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                modifier = Modifier.size(56.dp)
            )

            PlayerControlButton(
                onClick = onSeekBackward,
                icon = Icons.Filled.FastRewind,
                contentDescription = "Retroceder 10s",
                modifier = Modifier.size(56.dp)
            )

            PlayerControlButton(
                onClick = onPlayPause,
                icon = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproduzir",
                modifier = Modifier
                    .focusRequester(controlsFocusRequester)
                    .size(72.dp)
            )

            PlayerControlButton(
                onClick = onSeekForward,
                icon = Icons.Filled.FastForward,
                contentDescription = "Avançar 10s",
                modifier = Modifier.size(56.dp)
            )

            Spacer(Modifier.weight(1f))

      Text(
        text = "${formatDuration(currentPosition)} / ${formatDuration(duration)}",
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@Composable
private fun SeekBar(
    currentPosition: Long,
    duration: Long,
    modifier: Modifier = Modifier
) {
    val progress = if (duration > 0L) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Box(
            modifier = Modifier
                .weight(1f - progress)
                .fillMaxHeight()
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms < 0) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun PlayerControlButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

  Surface(
    onClick = onClick,
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .border(
        width = if (isFocused) 2.dp else 0.dp,
        color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(8.dp)
      ),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f),
      focusedContainerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onSurface,
      focusedContentColor = MaterialTheme.colorScheme.onPrimary,
      pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
      pressedContentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
    interactionSource = interactionSource
  ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
