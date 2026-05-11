# Prompt — Scroll: salvar posição no onClick em vez de onDispose

Consulte o AGENTS.md e as skills antes de começar.

---

## Problema
O `scrollToItem` é chamado com valores corretos mas o grid não responde.
A abordagem com `DisposableEffect` + `LaunchedEffect` está funcionando no log
mas não visualmente. Vamos mudar a estratégia completamente.

## Nova estratégia
Em vez de salvar no `onDispose` e restaurar no `LaunchedEffect`, salvar a posição
no momento exato do clique (antes de navegar) e restaurar com `initialFirstVisibleItemIndex`
no `rememberLazyGridState` / `rememberLazyListState` — que funciona corretamente na
primeira composição após o back stack.

---

## StreamViewModel.kt

Mantenha `_savedScrollIndex`, `_savedScrollOffset` e `saveScrollPosition()` como estão.

---

## StreamScreen.kt

### Remova completamente:
- O `LaunchedEffect(uiState.savedScrollIndex, uiState.streams.size)` 
- O `DisposableEffect(Unit)`

### Mude o `rememberLazyGridState` para usar `initial*`:

```kotlin
val gridState = rememberLazyGridState(
    initialFirstVisibleItemIndex = uiState.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = uiState.savedScrollOffset
)
```

### No `PosterCard` de cada item, salve a posição ANTES de navegar:

No `itemsIndexed` do branch `else` (VOD/SERIES), mude o `onClick`:

```kotlin
onClick = {
    viewModel.saveScrollPosition(
        gridState.firstVisibleItemIndex,
        gridState.firstVisibleItemScrollOffset
    )
    if (stream.type == ContentType.SERIES) {
        onStreamSelected(stream)
    } else {
        onStreamSelected(stream)
    }
},
```

No `itemsIndexed` do branch `ContentType.LIVE`, mude o `onClick`:

```kotlin
onClick = {
    viewModel.saveScrollPosition(
        gridState.firstVisibleItemIndex,
        gridState.firstVisibleItemScrollOffset
    )
    onStreamSelected(stream)
},
```

### Remova os logs de ScrollDebug que restarem no arquivo.

---

## CategoryViewModel.kt

Mantenha `savedScrollIndex`, `savedScrollOffset` e `saveScrollPosition()` como estão.

---

## CategoryScreen.kt

### Remova completamente:
- O `LaunchedEffect(uiState.savedScrollIndex, viewModel.filteredCategories.size)`
- O `DisposableEffect(Unit)`

### Mude o `rememberLazyListState` para usar `initial*`:

```kotlin
val listState = androidx.compose.foundation.lazy.rememberLazyListState(
    initialFirstVisibleItemIndex = uiState.savedScrollIndex,
    initialFirstVisibleItemScrollOffset = uiState.savedScrollOffset
)
```

### Em cada `CategoryItem`, salve a posição ANTES de navegar.

No `CategoryItem` de "Favoritos":
```kotlin
onClick = {
    viewModel.saveScrollPosition(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    )
    onNavigateToStream(FAVORITES_CATEGORY_ID)
},
```

No `CategoryItem` de "Recentes":
```kotlin
onClick = {
    viewModel.saveScrollPosition(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    )
    onNavigateToStream(RECENTS_CATEGORY_ID)
},
```

No `CategoryItem` de "Todos":
```kotlin
onClick = {
    viewModel.saveScrollPosition(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    )
    onNavigateToStream(ALL_CATEGORY_ID)
},
```

No `items(viewModel.filteredCategories)`:
```kotlin
onClick = {
    viewModel.saveScrollPosition(
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    )
    onNavigateToStream(category.id)
}
```

### Remova os logs de ScrollDebug que restarem no arquivo.

---

## Não mexa em mais nada. Build, instale e teste visualmente.
