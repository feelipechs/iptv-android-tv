package com.iptv.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.filled.Favorite

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.Flow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.iptv.tv.ui.components.TvSearchField

private val MAIN_PANEL_WIDTH = 72.dp
private val CATEGORIES_PANEL_WIDTH = 260.dp

@Composable
fun HomeScreen(
    onStreamSelected: (Stream) -> Unit,
    onPlayEpisode: ((String, String, Long) -> Unit)? = null,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val firstCategoryFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { firstCategoryFocus.requestFocus() }

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // PAINEL 1 — Menu principal (72dp, só ícones)
        Column(
            modifier = Modifier
                .width(MAIN_PANEL_WIDTH)
                .fillMaxHeight()
                .focusGroup()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MainMenuItem.entries.forEach { item ->
                val isSelected = when (item) {
                    MainMenuItem.LIVE -> state.selectedContentType == ContentType.LIVE
                    MainMenuItem.VOD -> state.selectedContentType == ContentType.VOD
                    MainMenuItem.SERIES -> state.selectedContentType == ContentType.SERIES
                    MainMenuItem.REFRESH -> false
                    MainMenuItem.SETTINGS -> false
                }
                Surface(
                    onClick = {
                        if (item == MainMenuItem.SETTINGS) {
                            onNavigateToSettings()
                        } else {
                            viewModel.selectMainItem(item)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ClickableSurfaceDefaults.colors(
          containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
          else MaterialTheme.colorScheme.surface,
          focusedContainerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onSurface,
          focusedContentColor = MaterialTheme.colorScheme.onPrimary,
          pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
          pressedContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
      ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (item) {
                                MainMenuItem.LIVE -> Icons.Filled.PlayArrow
                                MainMenuItem.VOD -> Icons.Filled.Home
                                MainMenuItem.SERIES -> Icons.AutoMirrored.Filled.List
                                MainMenuItem.REFRESH -> Icons.Filled.Refresh
                                MainMenuItem.SETTINGS -> Icons.Filled.Settings
                            },
                            contentDescription = item.name,
                            modifier = Modifier.size(28.dp)
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
                .focusGroup()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Text(
                text = when {
                    state.selectedContentType == ContentType.LIVE -> "Ao Vivo"
                    state.selectedContentType == ContentType.VOD -> "Filmes"
                    else -> "Séries"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

        Surface(
            onClick = { viewModel.selectCategory("favorites_special") },
            modifier = Modifier.fillMaxWidth().focusRequester(firstCategoryFocus),
      colors = ClickableSurfaceDefaults.colors(
        containerColor = if (state.selectedCategoryId == "favorites_special") MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surface,
        focusedContainerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        pressedContentColor = MaterialTheme.colorScheme.onPrimary
      ),
      shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp))
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Favoritos",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
                }
            }
            Spacer(Modifier.height(2.dp))
            Surface(
                onClick = { viewModel.selectCategory("recents_special") },
                modifier = Modifier.fillMaxWidth(),
    colors = ClickableSurfaceDefaults.colors(
      containerColor = if (state.selectedCategoryId == "recents_special") MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surface,
      focusedContainerColor = MaterialTheme.colorScheme.primary,
      contentColor = MaterialTheme.colorScheme.onSurface,
      focusedContentColor = MaterialTheme.colorScheme.onPrimary,
      pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
      pressedContentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(6.dp))
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
                ) {
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Recentes",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
                }
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.surfaceVariant))
            Spacer(Modifier.height(4.dp))

        TvSearchField(
            value = state.categorySearch,
            onValueChange = { viewModel.onCategorySearchChange(it) },
            placeholder = "Buscar categoria..."
        )

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
                LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
                    items(state.categories.filter { it.name.contains(state.categorySearch, ignoreCase = true) }) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = category.id == state.selectedCategoryId,
                            onSelect = { viewModel.selectCategory(category.id) }
                        )
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
                .focusGroup()
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
        else -> {
            Column(Modifier.focusGroup()) {
                TvSearchField(
                    value = state.streamSearch,
                    onValueChange = { viewModel.onStreamSearchChange(it) },
                    placeholder = "Buscar stream...",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                        if (state.selectedContentType == ContentType.LIVE || state.selectedCategoryId == "favorites_special" || state.selectedCategoryId == "recents_special") {
LiveStreamGrid(
                                streams = state.streams.filter { it.name.contains(state.streamSearch, ignoreCase = true) },
                                onStreamSelected = {
                                    viewModel.recordToHistory(it)
                                    onStreamSelected(it)
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                isFavorite = { viewModel.isFavorite(it) },
                    onPlayEpisodeUrl = { url, name, startPosition ->
                        onPlayEpisode?.invoke(url, name, startPosition)
                    }
                            )
                        } else {
                            VodStreamGrid(streams = state.streams.filter { it.name.contains(state.streamSearch, ignoreCase = true) }, onStreamSelected = {
                                viewModel.recordToHistory(it)
                                onStreamSelected(it)
                            })
                        }
                    }
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
      focusedContentColor = MaterialTheme.colorScheme.onPrimary,
      pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
      pressedContentColor = MaterialTheme.colorScheme.onPrimary
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
    onStreamSelected: (Stream) -> Unit,
    onToggleFavorite: (Stream) -> Unit,
    isFavorite: (String) -> Flow<Boolean>,
    onPlayEpisodeUrl: ((String, String, Long) -> Unit)? = null
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(streams) { _, stream ->
            Row(modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isFocused by interactionSource.collectIsFocusedAsState()
                    Surface(
                        onClick = { onStreamSelected(stream) },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = if (isFocused) 2.dp else 1.dp,
                                color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
      colors = ClickableSurfaceDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContentColor = MaterialTheme.colorScheme.onSurface,
        pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        pressedContentColor = MaterialTheme.colorScheme.onPrimary
      ),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stream.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (stream.type == ContentType.SERIES && stream.lastSeason != null && stream.lastEpisodeNum != null) {
                                Text(
                                    text = buildString {
                                        append("T")
                                        append(stream.lastSeason.substringAfter(" "))
                                        append(" E")
                                        append(stream.lastEpisodeNum)
                                        stream.lastEpisodeTitle?.let { append(" · $it") }
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (stream.type == ContentType.VOD && stream.progress > 0f) {
                                val percent = (stream.progress * 100).toInt()
                                Text(
                                    text = "$percent% assistido",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    if (stream.type == ContentType.VOD && stream.progress > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .align(Alignment.BottomStart)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(stream.progress)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(4.dp))
        if (stream.type == ContentType.SERIES && stream.lastEpisodeUrl != null && onPlayEpisodeUrl != null) {
        Surface(
            onClick = { onPlayEpisodeUrl(stream.lastEpisodeUrl, stream.lastEpisodeTitle ?: stream.name, -1L) },
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.primary,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                pressedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Reproduzir",
                    modifier = Modifier.size(24.dp)
                )
                        }
                    }
                    Spacer(Modifier.width(2.dp))
                }
                if (stream.type == ContentType.VOD && stream.progress > 0f) {
        Surface(
            onClick = { onStreamSelected(stream) },
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.primary,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                pressedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Continuar",
                    modifier = Modifier.size(24.dp)
                )
                        }
                    }
                    Spacer(Modifier.width(2.dp))
                }
                val isFav by isFavorite(stream.id).collectAsStateWithLifecycle(initialValue = false)
        Surface(
            onClick = { onToggleFavorite(stream) },
            modifier = Modifier
                .width(52.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.primary,
                contentColor = if (isFav) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                pressedContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favoritar",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = if (isFocused) 2.dp else 0.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Surface(
                    onClick = { onStreamSelected(stream) },
                    interactionSource = interactionSource,
                    modifier = Modifier.aspectRatio(0.7f),
      colors = ClickableSurfaceDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContentColor = MaterialTheme.colorScheme.onSurface,
        pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        pressedContentColor = MaterialTheme.colorScheme.onPrimary
      ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                ) {
            Column {
                stream.posterUrl?.let { url ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = stream.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        if (stream.progress > 0f) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PlayArrow,
                                    contentDescription = "Continuar",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
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
}
