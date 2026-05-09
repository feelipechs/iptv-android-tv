package com.iptv.tv.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.ProviderType
import com.iptv.tv.ui.components.TvTextField

@Composable
fun EditCredentialsScreen(
    credentials: Credentials,
    onNavigateBack: () -> Unit,
    onSave: (Credentials) -> Unit
) {
    var server by remember { mutableStateOf(credentials.server) }
    var username by remember { mutableStateOf(credentials.username) }
    var password by remember { mutableStateOf(credentials.password) }
    var m3uSource by remember { mutableStateOf(credentials.m3uSource ?: "") }

    val firstFieldFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        firstFieldFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Editar Provedor",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (credentials.providerType) {
                ProviderType.XTREAM -> {
                    TvTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = "Servidor",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        focusRequester = firstFieldFocusRequester
                    )
                    TvTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Usuário",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )
                    TvTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha",
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        )
                    )
                }
                ProviderType.M3U_LIST -> {
                    TvTextField(
                        value = m3uSource,
                        onValueChange = { m3uSource = it },
                        label = "URL M3U",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        focusRequester = firstFieldFocusRequester
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                onClick = {
                    val updated = when (credentials.providerType) {
                        ProviderType.XTREAM -> credentials.copy(
                            server = server,
                            username = username,
                            password = password
                        )
                        ProviderType.M3U_LIST -> credentials.copy(m3uSource = m3uSource)
                    }
                    onSave(updated)
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)),
                colors = ClickableSurfaceDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimary,
                    pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                    pressedContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Salvar")
                }
            }
        }
    }
}
