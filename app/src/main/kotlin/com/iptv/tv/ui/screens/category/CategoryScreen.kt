package com.iptv.tv.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import com.iptv.tv.domain.model.ContentType

@Composable
fun CategoryScreen(
    contentType: ContentType,
    onCategorySelected: (String) -> Unit,
    onTypeChange: (ContentType) -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pinnedCategories by viewModel.pinnedCategories.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 24.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categorias",
                style = MaterialTheme.typography.headlineMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (state.historyCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.tertiary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${state.historyCount} Histórico", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (state.favoriteCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("${state.favoriteCount} Favoritos", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Surface(
                        onClick = onNavigateToFavorites,
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                    ) {
                        Text(
                            "Ver favoritos",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tabs = ContentType.entries
            items(tabs.size) { idx ->
                val tab = tabs[idx]
                val selected = tab == contentType
                Surface(
                    onClick = { if (!selected) onTypeChange(tab) },
                    colors = if (selected)
                        ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    else
                        ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                    shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                ) {
                    Text(
                        text = tab.name,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Carregando categorias…")
                }
            }
            state.error != null && state.categories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Erro ao carregar: ${state.error}")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = viewModel::refresh) { Text("Tentar novamente") }
                    }
                }
            }
            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsIndexed(state.categories) { _, category ->
                        val isPinned = pinnedCategories.contains(category.id)
                        Surface(
                            onClick = { onCategorySelected(category.id) },
                            onLongClick = { viewModel.togglePinCategory(category.id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = if (isPinned)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                focusedContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isPinned) "★ ${category.name}" else category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (category.streamCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("${category.streamCount}", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}