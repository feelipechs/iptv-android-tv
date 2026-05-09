package com.iptv.tv.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.usecase.GetCategoriesUseCase
import com.iptv.tv.domain.usecase.RefreshContentUseCase
import com.iptv.tv.ui.screens.home.FAVORITES_CATEGORY_ID
import com.iptv.tv.ui.screens.home.RECENTS_CATEGORY_ID
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val type: ContentType = ContentType.LIVE,
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val categorySearch: String = ""
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val refreshContentUseCase: RefreshContentUseCase
) : ViewModel() {

    private val type: ContentType = try {
        ContentType.valueOf(savedStateHandle.get<String>("type") ?: "LIVE")
    } catch (_: Exception) { ContentType.LIVE }

    private val _uiState = MutableStateFlow(CategoryUiState(type = type))
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    val filteredCategories: List<Category>
        get() = if (_uiState.value.categorySearch.isBlank()) _uiState.value.categories
        else _uiState.value.categories.filter { it.name.contains(_uiState.value.categorySearch, ignoreCase = true) }

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            getCategoriesUseCase(type).collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories, isLoading = false)
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { refreshContentUseCase(type) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }

    fun onCategorySearchChange(query: String) {
        _uiState.value = _uiState.value.copy(categorySearch = query)
    }
}
