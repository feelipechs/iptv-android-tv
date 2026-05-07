package com.iptv.tv.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.iptv.tv.domain.model.Credentials
import com.iptv.tv.domain.model.ProviderType

@Composable
fun EditCredentialsScreen(
    credentials: Credentials,
    onBack: () -> Unit,
    onSave: (Credentials) -> Unit
) {
    var server by remember { mutableStateOf(credentials.server) }
    var username by remember { mutableStateOf(credentials.username) }
    var password by remember { mutableStateOf(credentials.password) }
    var m3uSource by remember { mutableStateOf(credentials.m3uSource ?: "") }

    val firstFieldFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        firstFieldFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
      Surface(
        onClick = onBack,
        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
          containerColor = MaterialTheme.colorScheme.surface,
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.onSurface,
          focusedContentColor = MaterialTheme.colorScheme.onSurface,
          pressedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
          pressedContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
      ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Editar Provedor",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

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
                    EditTextField(
                        value = server,
                        onValueChange = { server = it },
                        label = "Servidor",
                        imeAction = ImeAction.Next,
                        focusRequester = firstFieldFocusRequester
                    )
                    EditTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Usuário",
                        imeAction = ImeAction.Next
                    )
                    EditTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Senha",
                        imeAction = ImeAction.Done,
                        isPassword = true
                    )
                }
                ProviderType.M3U_LIST -> {
                    EditTextField(
                        value = m3uSource,
                        onValueChange = { m3uSource = it },
                        label = "URL M3U",
                        imeAction = ImeAction.Done,
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
          onBack()
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

@Composable
private fun EditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction = ImeAction.Done,
    isPassword: Boolean = false,
    focusRequester: FocusRequester? = null
) {
    val focusManager = LocalFocusManager.current

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (focusRequester != null) Modifier.focusRequester(focusRequester)
                        else Modifier
                    ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                visualTransformation = if (isPassword) PasswordVisualTransformation()
                else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
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