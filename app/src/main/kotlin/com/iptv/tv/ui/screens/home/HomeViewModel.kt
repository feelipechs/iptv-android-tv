package com.iptv.tv.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.repository.CredentialsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val FAVORITES_CATEGORY_ID = "favorites_special"
const val RECENTS_CATEGORY_ID = "recents_special"

data class HomeUiState(
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            credentialsRepository.getCredentials().collect { creds ->
                _uiState.value = HomeUiState(isLoggedIn = creds != null)
            }
        }
    }
}
