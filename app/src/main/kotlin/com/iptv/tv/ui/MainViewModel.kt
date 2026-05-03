package com.iptv.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.repository.CredentialsRepository
import com.iptv.tv.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository
) : ViewModel() {
    val theme: StateFlow<AppTheme> = credentialsRepository.getTheme()
        .map { if (it == "DARK") AppTheme.DARK else AppTheme.LIGHT }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppTheme.LIGHT)
}