package com.iptv.tv.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {
    val credentials: StateFlow<Credentials?> = credentialsRepository.getCredentials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentTheme: StateFlow<AppTheme> = credentialsRepository.getTheme()
        .map { if (it == "DARK") AppTheme.DARK else AppTheme.LIGHT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.LIGHT)

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            credentialsRepository.saveTheme(theme.name)
        }
    }

    fun saveCredentials(credentials: Credentials) {
        viewModelScope.launch {
            credentialsRepository.saveCredentials(credentials)
        }
    }

    fun logout() {
        viewModelScope.launch {
            credentialsRepository.clearCredentials()
        }
    }
}