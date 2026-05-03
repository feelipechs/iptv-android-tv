package com.iptv.tv.ui.screens.login

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.CredentialsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val providerType: ProviderType = ProviderType.XTREAM,
    val server: String = "",
    val username: String = "",
    val password: String = "",
    val m3uSource: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    // FIX: separado em dois estados distintos:
    // - isAlreadyLoggedIn: credencial existe, mas usuário não acabou de fazer login agora
    //   (usado apenas para pré-preencher campos, não dispara navegação)
    // - isLoggedIn: usuário acabou de logar com sucesso agora (dispara navegação)
    val isAlreadyLoggedIn: Boolean = false,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    private val contentRepository: ContentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = credentialsRepository.getCredentials().first()
            if (saved != null) {
                // FIX: seta isAlreadyLoggedIn=true mas isLoggedIn=false
                // Assim a LoginScreen usada em LoginEdit não navega automaticamente,
                // mas os campos são pré-preenchidos para o usuário editar.
                _uiState.value = LoginUiState(
                    providerType = saved.providerType,
                    server = saved.server,
                    username = saved.username,
                    password = saved.password,
                    m3uSource = saved.m3uSource ?: "",
                    isAlreadyLoggedIn = true,
                    isLoggedIn = false
                )
            }
        }
    }

    fun onProviderTypeChange(type: ProviderType) {
        _uiState.value = _uiState.value.copy(providerType = type, error = null)
    }

    fun onServerChange(value: String) {
        _uiState.value = _uiState.value.copy(server = value, error = null)
    }

    fun onUsernameChange(value: String) {
        _uiState.value = _uiState.value.copy(username = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun onM3uSourceChange(value: String) {
        _uiState.value = _uiState.value.copy(m3uSource = value, error = null)
    }

    fun login() {
        val state = _uiState.value

        when (state.providerType) {
            ProviderType.XTREAM -> loginXtream(state)
            ProviderType.M3U_LIST -> loginM3u(state)
        }
    }

    private fun loginXtream(state: LoginUiState) {
        if (state.server.isBlank() || state.username.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            val credentials = Credentials(
                server = state.server.trim(),
                username = state.username.trim(),
                password = state.password.trim(),
                providerType = ProviderType.XTREAM
            )

            contentRepository.validateCredentials(credentials)
                .onSuccess {
                    credentialsRepository.saveCredentials(credentials)
                    // FIX: isLoggedIn=true só depois do login efetivo
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Falha na autenticação"
                    )
                }
        }
    }

    private fun loginM3u(state: LoginUiState) {
        val source = state.m3uSource.trim()
        if (source.isBlank()) {
            _uiState.value = state.copy(error = "Informe a URL ou selecione um arquivo")
            return
        }

        val isUrl = source.startsWith("http://") || source.startsWith("https://")
        android.util.Log.d("LoginViewModel", "loginM3u: source='$source', isUrl=$isUrl")

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            val finalSource = if (isUrl) {
                source
            } else {
                try {
                    val uri = Uri.parse(source)
                    android.util.Log.d("LoginViewModel", "loginM3u: parsing URI scheme=${uri.scheme}")
                    uri.path ?: source
                } catch (e: Exception) {
                    android.util.Log.e("LoginViewModel", "loginM3u: URI parse error", e)
                    source
                }
            }

            android.util.Log.d("LoginViewModel", "loginM3u: finalSource='$finalSource'")

            val credentials = Credentials(
                server = "",
                username = "",
                password = "",
                providerType = ProviderType.M3U_LIST,
                m3uSource = finalSource
            )

            val validationCreds = credentials.copy(m3uSource = finalSource)
            contentRepository.validateCredentials(validationCreds)
                .onSuccess {
                    credentialsRepository.saveCredentials(credentials)
                    // FIX: isLoggedIn=true só depois do login efetivo
                    _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Falha ao carregar lista M3U"
                    )
                }
        }
    }
}
