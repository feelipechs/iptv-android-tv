package com.iptv.tv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import com.iptv.tv.domain.usecase.GetCategoriesUseCase
import com.iptv.tv.domain.usecase.GetStreamsUseCase
import com.iptv.tv.domain.usecase.RefreshContentUseCase
import com.iptv.tv.domain.usecase.RefreshStreamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class Panel {
    Categories,
    Content
}

data class HomeUiState(
    val selectedContentType: ContentType = ContentType.LIVE,
    val selectedCategoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val streams: List<Stream> = emptyList(),
    val isLoadingCategories: Boolean = true,
    val isLoadingStreams: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val activePanel: Panel = Panel.Categories,
    val favoriteCount: Int = 0,
    val historyCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val getStreamsUseCase: GetStreamsUseCase,
    private val refreshContentUseCase: RefreshContentUseCase,
    private val refreshStreamsUseCase: RefreshStreamsUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    private val _selectedContentType = MutableStateFlow(ContentType.LIVE)
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    private val _activePanel = MutableStateFlow(Panel.Categories)
    private val _pinnedCategories = MutableStateFlow<Set<String>>(emptySet())
    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoadingCategories = MutableStateFlow(true)
    private val _isLoadingStreams = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _favoriteCount = MutableStateFlow(0)
    private val _historyCount = MutableStateFlow(0)

    val pinnedCategories: StateFlow<Set<String>> = _pinnedCategories.asStateFlow()

    private val categoriesFlow: Flow<List<Category>> = _selectedContentType
        .flatMapLatest { type -> getCategoriesUseCase(type) }

    private val streamsFlow: Flow<List<Stream>> = combine(
        _selectedCategoryId,
        _selectedContentType
    ) { categoryId, contentType -> categoryId to contentType }
        .filter { (categoryId, _) -> categoryId != null }
        .flatMapLatest { (categoryId, contentType) ->
            getStreamsUseCase(categoryId!!, contentType)
        }
        .onStart { emit(emptyList()) }

    val uiState: StateFlow<HomeUiState> = combine(
        _selectedContentType,
        _selectedCategoryId,
        _activePanel,
        _pinnedCategories,
        _isRefreshing,
        _isLoadingCategories,
        _isLoadingStreams,
        _error,
        _favoriteCount,
        _historyCount,
        categoriesFlow,
        streamsFlow
    ) { values ->
        val contentType = values[0] as ContentType
        val categoryId = values[1] as String?
        val activePanel = values[2] as Panel
        val pinned = values[3] as Set<String>
        val isRefreshing = values[4] as Boolean
        val isLoadingCategories = values[5] as Boolean
        val isLoadingStreams = values[6] as Boolean
        val error = values[7] as String?
        val favCount = values[8] as Int
        val histCount = values[9] as Int
        @Suppress("UNCHECKED_CAST")
        val categories = values[10] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val streams = values[11] as List<Stream>

        val pinned_sorted = categories.sortedByDescending { cat -> pinned.contains(cat.id) }

        HomeUiState(
            selectedContentType = contentType,
            selectedCategoryId = categoryId,
            categories = pinned_sorted,
            streams = streams,
            isLoadingCategories = isLoadingCategories,
            isLoadingStreams = isLoadingStreams,
            isRefreshing = isRefreshing,
            error = error,
            activePanel = activePanel,
            favoriteCount = favCount,
            historyCount = histCount
        )
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HomeUiState()
        )

    init {
        viewModelScope.launch {
            val credentials = credentialsRepository.getCredentials().first()
            when {
                credentials == null -> _error.value = "Credenciais não encontradas"
                credentials.providerType == ProviderType.M3U_LIST && !credentials.m3uSource.isNullOrBlank() -> refreshCategories()
                credentials.providerType == ProviderType.XTREAM -> {
                    val server = credentials.server.trim()
                    if (server.isEmpty() || server.contains("placeholder")) {
                        _error.value = "Servidor não configurado"
                    } else {
                        refreshCategories()
                    }
                }
            }
        }
        observeCounts()
    }

    private fun observeCounts() {
        viewModelScope.launch {
            favoritesRepository.getTotalFavoriteCount()
                .collect { count -> _favoriteCount.value = count }
        }
        viewModelScope.launch {
            watchHistoryRepository.getHistoryCount()
                .collect { count -> _historyCount.value = count }
        }
    }

    fun selectContentType(type: ContentType) {
        if (_selectedContentType.value != type) {
            _selectedContentType.value = type
            _selectedCategoryId.value = null
            _activePanel.value = Panel.Categories
            refreshCategories()
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
        _activePanel.value = Panel.Content
        refreshStreams()
    }

    fun selectCategoryAndShowContent(categoryId: String) {
        _selectedCategoryId.value = categoryId
        _activePanel.value = Panel.Content
    }

    fun goToCategoriesPanel() {
        _activePanel.value = Panel.Categories
    }

    fun goToContentPanel() {
        if (_selectedCategoryId.value != null) {
            _activePanel.value = Panel.Content
        }
    }

    fun togglePinCategory(categoryId: String) {
        _pinnedCategories.update { pinned ->
            if (pinned.contains(categoryId)) pinned - categoryId else pinned + categoryId
        }
    }

    fun refresh() {
        val type = _selectedContentType.value
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            runCatching {
                if (_selectedCategoryId.value != null) {
                    refreshStreamsUseCase(_selectedCategoryId.value!!, type)
                }
                refreshContentUseCase(type)
            }
                .onFailure { e -> _error.value = e.message }
            _isRefreshing.value = false
        }
    }

    private fun refreshCategories() {
        viewModelScope.launch {
            _isLoadingCategories.value = true
            runCatching { refreshContentUseCase(_selectedContentType.value) }
                .onFailure { e -> _error.value = e.message }
            _isLoadingCategories.value = false
        }
    }

    private fun refreshStreams() {
        val categoryId = _selectedCategoryId.value ?: return
        val type = _selectedContentType.value
        viewModelScope.launch {
            _isLoadingStreams.value = true
            _isRefreshing.value = true
            runCatching { refreshStreamsUseCase(categoryId, type) }
                .onFailure { e -> _error.value = e.message }
            _isRefreshing.value = false
            _isLoadingStreams.value = false
        }
    }

    fun recordToHistory(stream: Stream) {
        viewModelScope.launch {
            watchHistoryRepository.addToHistory(stream, 0f)
        }
    }
}