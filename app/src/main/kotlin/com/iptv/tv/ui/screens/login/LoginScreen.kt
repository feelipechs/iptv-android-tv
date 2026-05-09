package com.iptv.tv.ui.screens.login

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.ui.components.TvTextField

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    val focusManager = LocalFocusManager.current
    val serverFocusRequester = remember { FocusRequester() }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onM3uSourceChange(it.toString()) }
    }

    val isXtreamValid = state.server.isNotBlank() && state.username.isNotBlank() && state.password.isNotBlank()
    val isM3uValid = state.m3uSource.isNotBlank()
    val canLogin = when (state.providerType) {
        ProviderType.XTREAM -> isXtreamValid
        ProviderType.M3U_LIST -> isM3uValid
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(480.dp)
        ) {
            if (onBack != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        onClick = onBack,
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                            pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            pressedContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "← Cancelar",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Text(
                text = "IPTV Player",
                style = MaterialTheme.typography.headlineLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isXtream = state.providerType == ProviderType.XTREAM
                Surface(
                    onClick = { viewModel.onProviderTypeChange(ProviderType.XTREAM) },
                    modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isXtream) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isXtream) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                        pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        pressedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Xtream")
                    }
                }
                val isM3u = state.providerType == ProviderType.M3U_LIST
                Surface(
                    onClick = { viewModel.onProviderTypeChange(ProviderType.M3U_LIST) },
                    modifier = Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(8.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = if (isM3u) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isM3u) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.primary,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                        pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        pressedContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("M3U")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when (state.providerType) {
                ProviderType.XTREAM -> {
                    TvTextField(
                        value = state.server,
                        onValueChange = viewModel::onServerChange,
                        label = "Servidor (ex: http://host:porta)",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        focusRequester = serverFocusRequester
                    )

                    TvTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "Usuário",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    TvTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Senha",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }

                ProviderType.M3U_LIST -> {
                    TvTextField(
                        value = state.m3uSource,
                        onValueChange = viewModel::onM3uSourceChange,
                        label = "URL da lista M3U",
                        placeholder = "https://exemplo.com/lista.m3u",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        focusRequester = serverFocusRequester
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ou",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Surface(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                            pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            pressedContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (state.m3uSource.isNotEmpty()) "Arquivo selecionado" else "Selecionar arquivo local"
                            )
                        }
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Surface(
                onClick = { if (canLogin && !state.isLoading) viewModel.login() },
                modifier = Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = if (canLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = if (canLogin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    focusedContainerColor = if (canLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedContentColor = if (canLogin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    pressedContainerColor = if (canLogin) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    pressedContentColor = if (canLogin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (state.isLoading) {
                        Text("Carregando...")
                    } else {
                        Text("Entrar")
                    }
                }
            }
        }
    }
}
