package com.iptv.tv.ui.screens.stream

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
    onPlayEpisode: (String, String, String, Long, String, String, String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: StreamViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val firstItemFocus = remember { FocusRequester() }
    val searchFieldFocus = remember { FocusRequester() }
    var searchVisible by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = uiState.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = uiState.savedScrollOffset
    )

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            delay(100)
            try { searchFieldFocus.requestFocus() } catch (_: Exception) { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.categoryName,
                style = MaterialTheme.typography.headlineMedium
            )
            Surface(
                onClick = { searchVisible = true },
                modifier = Modifier.size(48.dp),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                    pressedContainerColor = MaterialTheme.colorScheme.primary,
                    pressedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        if (searchVisible) {
            TvSearchField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = "Buscar streams...",
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                focusRequester = searchFieldFocus,
                onBack = {
                    searchVisible = false
                    viewModel.onSearchChange("")
                    firstItemFocus.requestFocus()
                },
                onSearch = { searchVisible = false }
            )
        }

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
                    columns = GridCells.Fixed(1),
                    state = gridState,
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
                            viewModel.saveScrollPosition(
                                gridState.firstVisibleItemIndex,
                                gridState.firstVisibleItemScrollOffset
                            )
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
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                itemsIndexed(uiState.streams, key = { _, stream -> stream.id }) { index, stream ->
                        PosterCard(
                            name = stream.name,
                            posterUrl = stream.posterUrl,
                            isFavorite = stream.id in uiState.favoriteIds,
                            onFavorite = { viewModel.toggleFavorite(stream) },
                        onClick = {
                            viewModel.saveScrollPosition(
                                gridState.firstVisibleItemIndex,
                                gridState.firstVisibleItemScrollOffset
                            )
                            if (stream.type == ContentType.SERIES) {
                                onStreamSelected(stream)
                            } else {
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
