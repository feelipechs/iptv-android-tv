---
name: android-tv-ui
description: Regras obrigatórias de UI para Android TV com Compose — imports corretos, botões focáveis, D-pad, temas, sem Dialog
---

# Android TV Compose UI

## Descrição
Regras obrigatórias para criar e modificar telas no app IPTV Android TV com Jetpack Compose.

## Regras críticas

### Imports — NUNCA use material3 padrão para UI
```kotlin
// ✅ CORRETO
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.Surface
import androidx.tv.material3.Button
import androidx.tv.material3.Icon
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ButtonDefaults

// ❌ ERRADO — quebra o tema escuro/claro
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
```

### Exceção permitida
```kotlin
// CircularProgressIndicator pode vir do compose.material3
import androidx.compose.material3.CircularProgressIndicator
```

### Botões e superfícies clicáveis
Sempre defina TODAS as 4 cores no ClickableSurfaceDefaults:
```kotlin
Surface(
    onClick = { ... },
    colors = ClickableSurfaceDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        focusedContainerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onSurface,
        focusedContentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp))
) { ... }
```

### NUNCA use Dialog do Compose em telas TV
`androidx.compose.ui.window.Dialog` não funciona corretamente com D-pad no Android TV.
- ❌ Nunca use `Dialog { }` wrapper
- ✅ Use tela separada (nova rota no NavHost) para edição/confirmação
- ✅ Use overlay `Box` com `focusGroup()` se inevitável

### Campos de texto (BasicTextField)
Sempre inclua `singleLine`, `keyboardOptions` e `keyboardActions`:
```kotlin
BasicTextField(
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    modifier = Modifier
        .fillMaxSize()
        .focusRequester(focusRequester)
        .onFocusChanged { onFocusChange(it.isFocused) },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
    keyboardActions = KeyboardActions(
        onDone = { focusManager.clearFocus() }
    ),
    ...
)
```

### Navegação D-pad
- Cada painel deve ter foco independente
- Use `Panel` enum para controlar painel ativo: `Main`, `Categories`, `Content`
- Não aninhe elementos focáveis dentro de outros focáveis

### Cores do tema
```kotlin
// Tema está em Theme.kt — use sempre via MaterialTheme.colorScheme
// Não hardcode cores — use os tokens do tema
MaterialTheme.colorScheme.background    // fundo principal
MaterialTheme.colorScheme.surface       // painéis/cards
MaterialTheme.colorScheme.surfaceVariant // elementos secundários
MaterialTheme.colorScheme.primary       // destaque/foco
MaterialTheme.colorScheme.onSurface     // texto principal
MaterialTheme.colorScheme.onSurfaceVariant // texto secundário
MaterialTheme.colorScheme.error         // favorito ativo (vermelho)
```

### Scroll em telas de detalhes
Telas com conteúdo longo precisam de scroll explícito:
```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) { ... }
```
