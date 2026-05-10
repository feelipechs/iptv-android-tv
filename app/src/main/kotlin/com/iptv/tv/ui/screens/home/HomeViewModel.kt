package com.iptv.tv.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.domain.usecase.GetCategoriesUseCase
import com.iptv.tv.domain.usecase.RefreshContentUseCase
import com.iptv.tv.domain.usecase.RefreshStreamsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

const val FAVORITES_CATEGORY_ID = "favorites_special"
const val RECENTS_CATEGORY_ID = "recents_special"
const val ALL_CATEGORY_ID = "all_special"

data class HomeUiState(
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    private val refreshContentUseCase: RefreshContentUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val refreshStreamsUseCase: RefreshStreamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            credentialsRepository.getCredentials().collect { creds ->
                _uiState.value = HomeUiState(isLoggedIn = creds != null)
            }
        }
        viewModelScope.launch {
            val credentials = credentialsRepository.getCredentials().first()
            if (credentials != null) {
            launch {
                try {
                    val hasCache = withTimeoutOrNull(2_000) {
                        getCategoriesUseCase(ContentType.LIVE)
                            .first { it.isNotEmpty() }
                    } != null
                    if (!hasCache) {
                        refreshAllContent()
                    }
                } catch (e: Exception) {
                    Log.w("HomeViewModel", "Background refresh falhou: ${e.message}")
                }
            }
            }
        }
    }

    private suspend fun refreshAllContent() {
        val types = listOf(ContentType.LIVE, ContentType.VOD, ContentType.SERIES)

        coroutineScope {
            types.map { type ->
                async {
                    try { refreshContentUseCase(type) }
                    catch (e: Exception) {
                        Log.w("HomeViewModel", "Refresh categorias $type falhou: ${e.message}")
                    }
                }
            }.awaitAll()
        }

        coroutineScope {
            types.map { type ->
                async {
                    try {
                        val categories = getCategoriesUseCase(type).first()
                        categories.map { category ->
                            async {
                                try { refreshStreamsUseCase(category.id, type) }
                                catch (e: Exception) {
                                    Log.w("HomeViewModel", "Refresh streams ${category.id} falhou: ${e.message}")
                                }
                            }
                        }.awaitAll()
                    } catch (e: Exception) {
                        Log.w("HomeViewModel", "Leitura categorias $type falhou: ${e.message}")
                    }
                }
            }.awaitAll()
        }
    }
}
