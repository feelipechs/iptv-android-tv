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
        focusedContentColor = MaterialTheme.colorScheme.onPrimary,
        pressedContainerColor = MaterialTheme.colorScheme.primary,
        pressedContentColor = MaterialTheme.colorScheme.onPrimary
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
- Cada tela deve ter foco único e bem definido — **sem painéis simultâneos**
- Use `Modifier.focusGroup()` para criar fronteiras de foco em listas/grids
- Foco inicial: use `FocusRequester` + `LaunchedEffect(Unit)` para direcionar
  o foco ao primeiro item relevante ao entrar na tela
- Intercepte eventos de teclado com `Modifier.onKeyEvent` no elemento raiz
  para controlar comportamento de Back no D-pad
- **Nunca adicione botão "Voltar" na UI** — o Back é sempre via controle/D-pad

### Foco em cards e itens de lista
- Todo item clicável deve ter clip antes dos modificadores de foco:
  `Modifier.clip(RoundedCornerShape(8.dp))` — a ordem importa: clip → focusable → border
- Use borda colorida para indicar foco, não escala:
```kotlin
val isFocused by interactionSource.collectIsFocusedAsState()
Modifier.border(
    width = if (isFocused) 2.dp else 0.dp,
    color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
    shape = RoundedCornerShape(8.dp)
)
```
- Nunca use `graphicsLayer { scaleX/scaleY }` em cards — causa vazamento visual

### Cores do tema
```kotlin
// Tema está em Theme.kt — use sempre via MaterialTheme.colorScheme
// Não hardcode cores — use os tokens do tema
MaterialTheme.colorScheme.background       // fundo principal
MaterialTheme.colorScheme.surface          // painéis/cards
MaterialTheme.colorScheme.surfaceVariant   // elementos secundários
MaterialTheme.colorScheme.primary          // destaque/foco
MaterialTheme.colorScheme.onSurface        // texto principal
MaterialTheme.colorScheme.onSurfaceVariant // texto secundário
MaterialTheme.colorScheme.error            // favorito ativo (vermelho)
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

### Cards por tipo de conteúdo

**Canal ao vivo (LiveChannelCard)**
- Layout: nome centralizado + ícone de favorito
- Sem poster (EPG futuro)
- Tamanho fixo: largura ~180dp, altura ~72dp
- Foco via borda colorida

**VOD / Série (PosterCard)**
- Layout: poster (imagem) + nome abaixo
- Poster com aspect ratio 2:3 (retrato)
- Tamanho fixo: largura ~140dp, altura ~210dp + nome ~20dp
- Placeholder quando poster ausente

### Grid de streams
- Live: `LazyVerticalGrid` com `GridCells.Adaptive(minSize = 180.dp)`
- VOD/Série: `LazyVerticalGrid` com `GridCells.Adaptive(minSize = 140.dp)`
- Espaçamento entre cards: `horizontalArrangement = Arrangement.spacedBy(8.dp)`
