package com.iptv.tv.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.ui.screens.home.Panel as Panel

private val SIDE_PANEL_WIDTH = 280.dp

@Composable
fun HomeScreen(
    onStreamSelected: (Stream) -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pinnedCategories by viewModel.pinnedCategories.collectAsStateWithLifecycle()

    Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SidePanel(
            selectedContentType = state.selectedContentType,
            selectedCategoryId = state.selectedCategoryId,
            categories = state.categories,
            isLoadingCategories = state.isLoadingCategories,
            isRefreshing = state.isRefreshing,
            favoriteCount = state.favoriteCount,
            historyCount = state.historyCount,
            pinnedCategories = pinnedCategories,
            isActive = state.activePanel == Panel.Categories,
            onContentTypeSelect = viewModel::selectContentType,
            onCategorySelect = viewModel::selectCategory,
            onTogglePin = viewModel::togglePinCategory,
            onNavigateToFavorites = onNavigateToFavorites,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .width(SIDE_PANEL_WIDTH)
                .fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )

        ContentPanel(
            selectedContentType = state.selectedContentType,
            categoryId = state.selectedCategoryId,
            streams = state.streams,
            isLoading = state.isLoadingStreams,
            isActive = state.activePanel == Panel.Content,
            onStreamSelected = { stream ->
                viewModel.recordToHistory(stream)
                onStreamSelected(stream)
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun SidePanel(
    selectedContentType: ContentType,
    selectedCategoryId: String?,
    categories: List<com.iptv.tv.domain.model.Category>,
    isLoadingCategories: Boolean,
    isRefreshing: Boolean,
    favoriteCount: Int,
    historyCount: Int,
    pinnedCategories: Set<String>,
    isActive: Boolean,
    onContentTypeSelect: (ContentType) -> Unit,
    onCategorySelect: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IPTV",
                style = MaterialTheme.typography.titleLarge
            )
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        ContentType.entries.forEach { contentType ->
            val isSelected = contentType == selectedContentType
            Surface(
                onClick = { onContentTypeSelect(contentType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
            ) {
                Text(
                    text = contentType.name,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(Modifier.height(12.dp))

        when {
            categories.isEmpty() && isLoadingCategories -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Carregando...", style = MaterialTheme.typography.bodySmall)
                }
            }
            categories.isEmpty() && !isLoadingCategories -> {
                Text(
                    text = "Nenhuma categoria",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                if (isLoadingCategories) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(categories) { category ->
                        val isSelected = category.id == selectedCategoryId
                        val isPinned = pinnedCategories.contains(category.id)

                        Surface(
                            onClick = { onCategorySelect(category.id) },
                            onLongClick = { onTogglePin(category.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = if (isSelected)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                                focusedContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPinned) "★ ${category.name}" else category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (category.streamCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${category.streamCount}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (historyCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${historyCount}h",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            if (favoriteCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${favoriteCount}f",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Surface(
            onClick = onNavigateToFavorites,
            modifier = Modifier.fillMaxWidth(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.primary
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
        ) {
            Text(
                text = "Favoritos & Histórico",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(12.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        Surface(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth(),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.primary
            ),
            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
        ) {
            Text(
                text = "Atualizar",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun ContentPanel(
    selectedContentType: ContentType,
    categoryId: String?,
    streams: List<Stream>,
    isLoading: Boolean,
    isActive: Boolean,
    onStreamSelected: (Stream) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        when {
            categoryId == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecione uma categoria no painel esquerdo",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Carregando streams...")
                }
            }
            streams.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum stream nesta categoria",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                if (selectedContentType == ContentType.LIVE) {
                    LiveStreamGrid(
                        streams = streams,
                        onStreamSelected = onStreamSelected,
                        isActive = isActive
                    )
                } else {
                    VodStreamGrid(
                        streams = streams,
                        onStreamSelected = onStreamSelected,
                        isActive = isActive
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveStreamGrid(
    streams: List<Stream>,
    onStreamSelected: (Stream) -> Unit,
    isActive: Boolean
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(streams) { _, stream ->
            Surface(
                onClick = { onStreamSelected(stream) },
                modifier = Modifier.fillMaxWidth(),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
            ) {
                Text(
                    text = stream.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun VodStreamGrid(
    streams: List<Stream>,
    onStreamSelected: (Stream) -> Unit,
    isActive: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        streams.chunked(4).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEach { stream ->
                    Surface(
                        onClick = { onStreamSelected(stream) },
                        modifier = Modifier
                            .width(180.dp)
                            .height(180.dp),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.medium)
                    ) {
                        Column {
                            AsyncImage(
                                model = stream.posterUrl,
                                contentDescription = stream.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            )
                            Text(
                                text = stream.name,
                                style = MaterialTheme.typography.bodySmall,
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
}