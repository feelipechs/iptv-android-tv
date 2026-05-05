package com.iptv.tv.ui.screens.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import androidx.compose.foundation.background

@Composable
fun ContentScreen(
    categoryId: String,
    contentType: ContentType,
    onStreamSelected: (Stream) -> Unit,
    onBack: () -> Unit,
    viewModel: ContentViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {

    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            onClick = onBack,
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small),
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Text("← Voltar", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        }
        Text(
            text = if (contentType == ContentType.LIVE) "Canais" else "Conteúdo",
            style = MaterialTheme.typography.headlineMedium
        )
    }

        Spacer(Modifier.height(20.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carregando…")
                }
            }
            state.streams.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum conteúdo nesta categoria")
                }
            }
            else -> {
                if (contentType == ContentType.LIVE) {
                    LiveStreamList(
                        streams = state.streams,
                        favoriteStreamIds = state.favoriteStreamIds,
                        onStreamSelected = { stream ->
                            viewModel.recordToHistory(stream)
                            onStreamSelected(stream)
                        },
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                } else {
                    VodGrid(
                        streams = state.streams,
                        favoriteStreamIds = state.favoriteStreamIds,
                        onStreamSelected = { stream ->
                            viewModel.recordToHistory(stream)
                            onStreamSelected(stream)
                        },
                        onToggleFavorite = viewModel::toggleFavorite
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStreamList(
    streams: List<Stream>,
    favoriteStreamIds: Set<String>,
    onStreamSelected: (Stream) -> Unit,
    onToggleFavorite: (Stream) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(streams.size) { idx ->
            val stream = streams[idx]
            val isFavorite = favoriteStreamIds.contains(stream.id)

            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Surface(
                    onClick = { onStreamSelected(stream) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stream.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Surface(
                    onClick = { onToggleFavorite(stream) },
                    modifier = Modifier.size(56.dp).fillMaxHeight(),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remover favorito" else "Adicionar favorito",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VodGrid(
    streams: List<Stream>,
    favoriteStreamIds: Set<String>,
    onStreamSelected: (Stream) -> Unit,
    onToggleFavorite: (Stream) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(streams) { stream ->
            val isFavorite = favoriteStreamIds.contains(stream.id)

            Surface(
                onClick = { onStreamSelected(stream) },
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium)
            ) {
                Box {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = stream.posterUrl,
                                contentDescription = stream.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )

                            Surface(
                                onClick = { onToggleFavorite(stream) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(40.dp),
                                colors = ClickableSurfaceDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isFavorite) "Remover favorito" else "Adicionar favorito",
                                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                    }
                    if (stream.progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(stream.progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                    Text(
                        text = stream.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
}
