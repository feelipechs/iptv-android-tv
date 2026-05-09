package com.iptv.tv.ui.screens.stream

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.ui.components.LiveChannelCard
import com.iptv.tv.ui.components.PosterCard
import com.iptv.tv.ui.components.TvSearchField

@Composable
fun StreamScreen(
    categoryId: String,
    type: ContentType,
    onStreamSelected: (Stream) -> Unit,
    onPlayEpisode: (String, String, String, Long) -> Unit,
    onBack: () -> Unit,
    viewModel: StreamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val firstItemFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        firstItemFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        TvSearchField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchChange,
            placeholder = "Buscar streams...",
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.streams.isEmpty() && uiState.error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Erro ao carregar: ${uiState.error}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            uiState.streams.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhum resultado encontrado",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                when (type) {
                    ContentType.LIVE -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 180.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(uiState.streams, key = { _, stream -> stream.id }) { index, stream ->
                                LiveChannelCard(
                                    name = stream.name,
                                    isFavorite = stream.id in uiState.favoriteIds,
                                    onFavorite = { viewModel.toggleFavorite(stream) },
                                    onClick = {
                                        viewModel.recordToHistory(stream)
                                        onStreamSelected(stream)
                                    },
                                    modifier = if (index == 0) Modifier.focusRequester(firstItemFocus) else Modifier
                                )
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 140.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(uiState.streams, key = { _, stream -> stream.id }) { index, stream ->
                                val isSeriesEpisode = stream.type == ContentType.SERIES && stream.lastEpisodeUrl != null
                                PosterCard(
                                    name = stream.name,
                                    posterUrl = stream.posterUrl,
                                    isFavorite = stream.id in uiState.favoriteIds,
                                    onFavorite = { viewModel.toggleFavorite(stream) },
                                    onClick = {
                                        if (isSeriesEpisode) {
                                            val episodeUrl = stream.lastEpisodeUrl ?: return@PosterCard
                                            val episodeId = extractEpisodeIdFromUrl(episodeUrl)
                                            onPlayEpisode(
                                                episodeId,
                                                episodeUrl,
                                                stream.lastEpisodeTitle ?: stream.name,
                                                -1L
                                            )
                                        } else {
                                            viewModel.recordToHistory(stream)
                                            onStreamSelected(stream)
                                        }
                                    },
                                    modifier = if (index == 0) Modifier.focusRequester(firstItemFocus) else Modifier
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun extractEpisodeIdFromUrl(url: String): String {
    return try {
        val path = java.net.URL(url).path
        val filename = path.substringAfterLast("/")
        filename.substringBeforeLast(".")
    } catch (_: Exception) {
        url
    }
}
