package com.iptv.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream

private val MAIN_PANEL_WIDTH = 72.dp
private val CATEGORIES_PANEL_WIDTH = 260.dp

@Composable
fun HomeScreen(
    onStreamSelected: (Stream) -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // PAINEL 1 — Menu principal (72dp, só ícones)
        Column(
            modifier = Modifier
                .width(MAIN_PANEL_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MainMenuItem.entries.forEach { item ->
                val isSelected = when (item) {
                    MainMenuItem.LIVE -> state.selectedContentType == ContentType.LIVE && !state.isFavoritesMode
                    MainMenuItem.VOD -> state.selectedContentType == ContentType.VOD && !state.isFavoritesMode
                    MainMenuItem.SERIES -> state.selectedContentType == ContentType.SERIES && !state.isFavoritesMode
                    MainMenuItem.FAVORITES -> state.isFavoritesMode
                    MainMenuItem.REFRESH -> false
                }
                Surface(
                    onClick = { viewModel.selectMainItem(item) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                         else MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = when (item) {
                                MainMenuItem.LIVE -> "📺"
                                MainMenuItem.VOD -> "🎬"
                                MainMenuItem.SERIES -> "📡"
                                MainMenuItem.FAVORITES -> "★"
                                MainMenuItem.REFRESH -> "↺"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }

        // Divisor
        Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant))

        // PAINEL 2 — Categorias (260dp)
        Column(
            modifier = Modifier
                .width(CATEGORIES_PANEL_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text(
                text = when {
                    state.isFavoritesMode -> "Favoritos"
                    state.selectedContentType == ContentType.LIVE -> "Ao Vivo"
                    state.selectedContentType == ContentType.VOD -> "Filmes"
                    else -> "Séries"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            if (state.isFavoritesMode) {
                val favCategories = listOf(
                    Category("fav_live", "★ Canais", ContentType.LIVE, 0),
                    Category("fav_vod", "★ Filmes", ContentType.VOD, 0),
                    Category("fav_series", "★ Séries", ContentType.SERIES, 0)
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(favCategories) { cat ->
                        CategoryItem(
                            category = cat,
                            isSelected = cat.id == state.selectedCategoryId,
                            onSelect = { viewModel.selectCategory(cat.id) }
                        )
                    }
                }
            } else {
                if (state.isLoadingCategories && state.categories.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Carregando...", style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    if (state.isLoadingCategories) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp).align(Alignment.End),
                            strokeWidth = 2.dp
                        )
                    }
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(state.categories) { category ->
                            CategoryItem(
                                category = category,
                                isSelected = category.id == state.selectedCategoryId,
                                onSelect = { viewModel.selectCategory(category.id) }
                            )
                        }
                    }
                }
            }
        }

        // Divisor
        Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant))

        // PAINEL 3 — Streams
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                state.selectedCategoryId == null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Selecione uma categoria",
                             style = MaterialTheme.typography.bodyLarge,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                state.isLoadingStreams -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.streams.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Nenhum stream nesta categoria",
                             style = MaterialTheme.typography.bodyLarge,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                state.selectedContentType == ContentType.LIVE || state.isFavoritesMode -> {
                    LiveStreamGrid(streams = state.streams, onStreamSelected = {
                        viewModel.recordToHistory(it)
                        onStreamSelected(it)
                    })
                }
                else -> {
                    VodStreamGrid(streams = state.streams, onStreamSelected = {
                        viewModel.recordToHistory(it)
                        onStreamSelected(it)
                    })
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: Category,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                             else MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onSurface,
            focusedContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (category.streamCount > 0) {
                Text(
                    text = "${category.streamCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun LiveStreamGrid(
    streams: List<Stream>,
    onStreamSelected: (Stream) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(streams) { _, stream ->
            Surface(
                onClick = { onStreamSelected(stream) },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.small)
            ) {
                Text(
                    text = stream.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun VodStreamGrid(
    streams: List<Stream>,
    onStreamSelected: (Stream) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 140.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(streams, key = { it.id }) { stream ->
            Surface(
                onClick = { onStreamSelected(stream) },
                modifier = Modifier.aspectRatio(0.7f),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
            ) {
                Column {
                    stream.posterUrl?.let { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = stream.name,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Text(
                        text = stream.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
