package com.iptv.tv.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.iptv.tv.domain.model.FavoriteEntry
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.model.WatchHistoryEntry

@Composable
fun FavoritesScreen(
    onStreamSelected: (Stream) -> Unit,
    onBack: () -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                    text = "Favoritos & Histórico",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
            if (state.history.isNotEmpty()) {
                Surface(
                    onClick = viewModel::clearHistory,
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Limpar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carregando…")
                }
            }
            state.favorites.isEmpty() && state.history.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhum favorito ou histórico ainda.")
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    if (state.favorites.isNotEmpty()) {
                        item {
                            Text(
                                text = "Favoritos (${state.favorites.size})",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        items(state.favorites) { favorite ->
                            FavoriteItem(
                                favorite = favorite,
                                onRemove = { viewModel.removeFavorite(favorite.streamId) },
                                onClick = {
                                    onStreamSelected(
                                        Stream(
                                            id = favorite.streamId,
                                            name = favorite.name,
                                            categoryId = favorite.categoryId,
                                            type = favorite.type,
                                            streamUrl = favorite.streamUrl,
                                            posterUrl = favorite.posterUrl
                                        )
                                    )
                                }
                            )
                        }
                    }

                    if (state.history.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Histórico recente (${state.history.size})",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        items(state.history) { historyEntry ->
                            HistoryItem(
                                entry = historyEntry,
                                onDelete = { viewModel.deleteHistoryEntry(historyEntry.streamId) },
                                onClick = {
                                    onStreamSelected(
                                        Stream(
                                            id = historyEntry.streamId,
                                            name = historyEntry.name,
                                            categoryId = historyEntry.categoryId,
                                            type = historyEntry.type,
                                            streamUrl = historyEntry.streamUrl,
                                            posterUrl = historyEntry.posterUrl,
                                            progress = historyEntry.progress
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    favorite: FavoriteEntry,
    onRemove: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (favorite.posterUrl != null) {
                    AsyncImage(
                        model = favorite.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(56.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = favorite.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = favorite.type.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Surface(
            onClick = onRemove,
            modifier = Modifier.size(56.dp).fillMaxHeight(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                focusedContainerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                focusedContentColor = MaterialTheme.colorScheme.onError
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Remover favorito",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HistoryItem(
    entry: WatchHistoryEntry,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.weight(1f).fillMaxHeight(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onSurface,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.posterUrl != null) {
                    AsyncImage(
                        model = entry.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(56.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${entry.type.name} • ${formatRelativeTime(entry.lastWatchedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (entry.progress > 0f) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(entry.progress)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Surface(
            onClick = onDelete,
            modifier = Modifier.size(56.dp).fillMaxHeight(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContentColor = MaterialTheme.colorScheme.onError
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Remover do histórico",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60_000
    val hours = minutes / 60
    val days = hours / 24
    return when {
        minutes < 1 -> "agora"
        minutes < 60 -> "${minutes}min"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> "${days / 7}sem"
    }
}