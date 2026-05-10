package com.iptv.tv.ui.screens.login

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.domain.repository.ContentRepository
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
import javax.inject.Inject

data class LoginUiState(
    val providerType: ProviderType = ProviderType.XTREAM,
    val server: String = "",
    val username: String = "",
    val password: String = "",
    val m3uSource: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAlreadyLoggedIn: Boolean = false,
    val isLoggedIn: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val credentialsRepository: CredentialsRepository,
    private val contentRepository: ContentRepository,
    private val refreshContentUseCase: RefreshContentUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val refreshStreamsUseCase: RefreshStreamsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val saved = credentialsRepository.getCredentials().first()
            if (saved != null) {
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

    private suspend fun refreshAllContent() {
        val types = listOf(ContentType.LIVE, ContentType.VOD, ContentType.SERIES)

        coroutineScope {
            types.map { type ->
                async {
                    try { refreshContentUseCase(type) }
                    catch (e: Exception) {
                        Log.w("LoginViewModel", "Refresh categorias $type falhou: ${e.message}")
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
                                    Log.w("LoginViewModel", "Refresh streams ${category.id} falhou: ${e.message}")
                                }
                            }
                        }.awaitAll()
                    } catch (e: Exception) {
                        Log.w("LoginViewModel", "Leitura categorias $type falhou: ${e.message}")
                    }
                }
            }.awaitAll()
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
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    try {
                        refreshAllContent()
                    } catch (e: Exception) {
                        Log.w("LoginViewModel", "Refresh inicial falhou: ${e.message}")
                    }
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
        Log.d("LoginViewModel", "loginM3u: source='$source', isUrl=$isUrl")

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)

            val finalSource = if (isUrl) {
                source
            } else {
                try {
                    val uri = Uri.parse(source)
                    Log.d("LoginViewModel", "loginM3u: parsing URI scheme=${uri.scheme}")
                    uri.path ?: source
                } catch (e: Exception) {
                    Log.e("LoginViewModel", "loginM3u: URI parse error", e)
                    source
                }
            }

            Log.d("LoginViewModel", "loginM3u: finalSource='$finalSource'")

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
                    _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                    try {
                        refreshAllContent()
                    } catch (e: Exception) {
                        Log.w("LoginViewModel", "Refresh inicial falhou: ${e.message}")
                    }
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
