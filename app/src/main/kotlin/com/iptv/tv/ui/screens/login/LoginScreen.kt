package com.iptv.tv.ui.screens.login

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.tv.material3.*
import com.iptv.tv.domain.model.ProviderType

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) onLoginSuccess()
    }

    var serverFocused by remember { mutableStateOf(false) }
    var userFocused by remember { mutableStateOf(false) }
    var passFocused by remember { mutableStateOf(false) }
    var m3uFocused by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onM3uSourceChange(it.toString()) }
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
            Text(
                text = "IPTV Player",
                style = MaterialTheme.typography.headlineLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.onProviderTypeChange(ProviderType.XTREAM) },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (state.providerType == ProviderType.XTREAM) "[Xtream]" else "Xtream")
                }
                Button(
                    onClick = { viewModel.onProviderTypeChange(ProviderType.M3U_LIST) },
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (state.providerType == ProviderType.M3U_LIST) "[M3U]" else "M3U")
                }
            }

            Spacer(Modifier.height(8.dp))

            when (state.providerType) {
                ProviderType.XTREAM -> {
                    StyledTextField(
                        value = state.server,
                        onValueChange = viewModel::onServerChange,
                        label = "Servidor (ex: http://host:porta)",
                        isFocused = serverFocused,
                        onFocusChange = { serverFocused = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                    )

                    StyledTextField(
                        value = state.username,
                        onValueChange = viewModel::onUsernameChange,
                        label = "Usuário",
                        isFocused = userFocused,
                        onFocusChange = { userFocused = it },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                    )

                    StyledTextField(
                        value = state.password,
                        onValueChange = viewModel::onPasswordChange,
                        label = "Senha",
                        isFocused = passFocused,
                        onFocusChange = { passFocused = it },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                    )
                }

                ProviderType.M3U_LIST -> {
                    StyledTextField(
                        value = state.m3uSource,
                        onValueChange = viewModel::onM3uSourceChange,
                        label = "URL da lista M3U",
                        placeholder = "https://exemplo.com/lista.m3u",
                        isFocused = m3uFocused,
                        onFocusChange = { m3uFocused = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
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

                    Button(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (state.m3uSource.isNotEmpty()) "Arquivo selecionado" else "Selecionar arquivo local"
                        )
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

            Button(
                onClick = viewModel::login,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isLoading) {
                    Text("Carregando...")
                } else {
                    Text("Entrar")
                }
            }
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isFocused: Boolean,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp))
                .clickable { onFocusChange(true) }
                .padding(horizontal = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    if (!isFocused) onFocusChange(true)
                },
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}