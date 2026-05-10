package com.iptv.tv.ui.screens.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.ui.components.CategoryItem
import com.iptv.tv.ui.components.TvSearchField
import com.iptv.tv.ui.screens.home.FAVORITES_CATEGORY_ID
import com.iptv.tv.ui.screens.home.RECENTS_CATEGORY_ID

@Composable
fun CategoryScreen(
    type: ContentType,
    onNavigateToStream: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val firstItemFocus = remember { FocusRequester() }
    val searchFieldFocus = remember { FocusRequester() }
    var searchVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        firstItemFocus.requestFocus()
    }

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
                text = when (type) {
                    ContentType.LIVE -> "TV ao Vivo"
                    ContentType.VOD -> "Filmes"
                    ContentType.SERIES -> "Séries"
                },
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
                value = uiState.categorySearch,
                onValueChange = viewModel::onCategorySearchChange,
                placeholder = "Buscar categorias...",
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                focusRequester = searchFieldFocus,
                onBack = {
                    searchVisible = false
                    viewModel.onCategorySearchChange("")
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
            uiState.error != null && uiState.categories.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Erro ao carregar: ${uiState.error}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            onClick = { viewModel.refresh() },
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                focusedContainerColor = MaterialTheme.colorScheme.primary,
                                focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                                pressedContainerColor = MaterialTheme.colorScheme.primary,
                                pressedContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                        ) {
                            Text("Tentar novamente", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        CategoryItem(
                            name = "Favoritos",
                            streamCount = 0,
                            isSelected = false,
                            onClick = { onNavigateToStream(FAVORITES_CATEGORY_ID) },
                            modifier = Modifier.focusRequester(firstItemFocus),
                            icon = Icons.Filled.Favorite
                        )
                    }
                    item {
                        CategoryItem(
                            name = "Recentes",
                            streamCount = 0,
                            isSelected = false,
                            onClick = { onNavigateToStream(RECENTS_CATEGORY_ID) },
                            icon = Icons.Filled.History
                        )
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(8.dp))
                    }
                    item {
                        CategoryItem(
                            name = "Todos",
                            streamCount = uiState.totalStreamCount,
                            isSelected = false,
                            onClick = { onNavigateToStream(com.iptv.tv.ui.screens.home.ALL_CATEGORY_ID) },
                            icon = Icons.Filled.Apps
                        )
                    }

                    items(viewModel.filteredCategories) { category ->
                        CategoryItem(
                            name = category.name,
                            streamCount = category.streamCount,
                            isSelected = false,
                            onClick = { onNavigateToStream(category.id) }
                        )
                    }

                    if (viewModel.filteredCategories.isEmpty() && uiState.categorySearch.isNotBlank()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Nenhuma categoria encontrada",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
