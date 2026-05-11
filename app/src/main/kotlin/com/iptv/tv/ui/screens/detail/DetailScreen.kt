package com.iptv.tv.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import androidx.tv.material3.Icon
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ClickableSurfaceDefaults
import coil.compose.AsyncImage
import com.iptv.tv.domain.model.Stream

@Composable
fun DetailScreen(
    stream: Stream,
    onPlay: (Stream, Long) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(stream.id) {
        viewModel.loadVodInfo(stream.id)
        viewModel.observeFavorite(stream.id)
    }

    val playButtonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(uiState.isLoading, uiState.savedProgress) {
        if (!uiState.isLoading) {
            delay(150)
            try { playButtonFocusRequester.requestFocus() } catch (_: Exception) {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detalhes",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
            val year = uiState.vodInfo?.info?.releaseDate ?: uiState.vodInfo?.info?.releaseDate2
            val genre = uiState.vodInfo?.info?.genre
            val plot = uiState.vodInfo?.info?.plot ?: uiState.vodInfo?.info?.description
            val rating = uiState.vodInfo?.info?.rating
            val actors = uiState.vodInfo?.info?.actors
            val director = uiState.vodInfo?.info?.director

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val imageUrl = stream.posterUrl

                    if (!imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stream.name,
                            modifier = Modifier
                                .width(200.dp)
                                .height(280.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Surface(
                            modifier = Modifier
                                .width(200.dp)
                                .height(280.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            colors = androidx.tv.material3.SurfaceDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {}
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stream.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                val progress = uiState.savedProgress
                val showContinue = progress != null && progress > 0.01f

                if (showContinue) {
      Surface(
        onClick = { onPlay(stream, -1L) },
        modifier = Modifier
          .focusRequester(playButtonFocusRequester)
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
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Continuar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                val progressPercent = ((uiState.savedProgress ?: 0f) * 100).toInt()
                if (progressPercent > 1) {
                    Text(
                        text = "$progressPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
                            }
                        }

      Surface(
        onClick = { onPlay(stream, 0L) },
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
        Row(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Filled.Replay,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Do início",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
      Surface(
        onClick = { onPlay(stream, 0L) },
        modifier = Modifier
          .focusRequester(playButtonFocusRequester)
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
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Assistir",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

      Surface(
        onClick = { viewModel.toggleFavorite(stream) },
        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          pressedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
          pressedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
      ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Favoritar",
                                        tint = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (uiState.isFavorite) "Favoritado" else "Favoritar",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (uiState.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

            if (rating != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format("%.1f", rating),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

                        if (!year.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = year.take(4),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!genre.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = genre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

if (!actors.isNullOrBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = actors,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

                        if (!director.isNullOrBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = director,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

if (!plot.isNullOrBlank()) {
            Text(
                text = plot,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
                    }
                }
            }
        }
    }
}