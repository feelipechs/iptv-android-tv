package com.iptv.tv.ui.screens.category

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import com.iptv.tv.domain.usecase.GetCategoriesUseCase
import com.iptv.tv.domain.usecase.RefreshContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val favoriteCount: Int = 0,
    val historyCount: Int = 0
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val refreshContentUseCase: RefreshContentUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contentTypeFlow = savedStateHandle.getStateFlow("type", "LIVE")
    private val contentType: ContentType get() = ContentType.valueOf(contentTypeFlow.value)

    private val _uiState = MutableStateFlow(CategoryUiState(isLoading = true))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    // Categorias pinadas (armazenadas localmente - em produção usar DataStore/SharedPrefs)
    private val _pinnedCategories = MutableStateFlow<Set<String>>(emptySet())
    val pinnedCategories: StateFlow<Set<String>> = _pinnedCategories.asStateFlow()

    init {
        observeCategories()
        observeCounts()
        refresh()

        viewModelScope.launch {
            var first = true
            contentTypeFlow.collect { typeStr ->
                if (!first) {
                    refresh()
                }
                first = false
            }
        }
    }

    private fun observeCategories() {
        contentTypeFlow
            .map { typeStr ->
                ContentType.valueOf(typeStr)
            }
            .flatMapLatest { type ->
                getCategoriesUseCase(type)
            }
            .onEach { categories ->
                val pinned = _pinnedCategories.value
                val sorted = categories.sortedByDescending { cat -> pinned.contains(cat.id) }
                _uiState.update { it.copy(categories = sorted, isLoading = false) }
            }
            .catch { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCounts() {
        viewModelScope.launch {
            favoritesRepository.getFavoriteCountByType(contentType)
                .collect { count ->
                    _uiState.update { it.copy(favoriteCount = count) }
                }
        }
        viewModelScope.launch {
            watchHistoryRepository.getHistoryCount()
                .collect { count ->
                    _uiState.update { it.copy(historyCount = count) }
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            runCatching { refreshContentUseCase(contentType) }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun pinCategory(categoryId: String) {
        _pinnedCategories.update { it + categoryId }
        refreshCategoriesOrder()
    }

    fun unpinCategory(categoryId: String) {
        _pinnedCategories.update { it - categoryId }
        refreshCategoriesOrder()
    }

    fun togglePinCategory(categoryId: String) {
        if (_pinnedCategories.value.contains(categoryId)) {
            unpinCategory(categoryId)
        } else {
            pinCategory(categoryId)
        }
    }

    private fun refreshCategoriesOrder() {
        val categories = _uiState.value.categories
        val pinned = _pinnedCategories.value
        val sorted = categories.sortedByDescending { cat -> pinned.contains(cat.id) }
        _uiState.update { it.copy(categories = sorted) }
    }
}
