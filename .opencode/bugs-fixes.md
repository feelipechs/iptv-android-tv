# Prompt — Diagnóstico de scroll position

Consulte o AGENTS.md e as skills antes de começar.

---

## Objetivo
Adicionar logs temporários para diagnosticar por que o scroll position não está sendo
restaurado ao voltar do back stack.

---

## StreamScreen.kt

Nos dois branches (LIVE e else), dentro do `DisposableEffect`, adicione log antes de salvar:

```kotlin
DisposableEffect(Unit) {
    onDispose {
        android.util.Log.d("ScrollDebug",
            "StreamScreen onDispose — index=${gridState.firstVisibleItemIndex}, offset=${gridState.firstVisibleItemScrollOffset}")
        viewModel.saveScrollPosition(
            gridState.firstVisibleItemIndex,
            gridState.firstVisibleItemScrollOffset
        )
    }
}
```

No `LaunchedEffect(uiState.savedScrollIndex)`, adicione log antes do scrollToItem:

```kotlin
LaunchedEffect(uiState.savedScrollIndex) {
    android.util.Log.d("ScrollDebug",
        "StreamScreen LaunchedEffect — savedIndex=${uiState.savedScrollIndex}, savedOffset=${uiState.savedScrollOffset}")
    if (uiState.savedScrollIndex > 0) {
        gridState.scrollToItem(
            index = uiState.savedScrollIndex,
            scrollOffset = uiState.savedScrollOffset
        )
    }
}
```

## CategoryScreen.kt

Mesma coisa no `DisposableEffect`:

```kotlin
DisposableEffect(Unit) {
    onDispose {
        android.util.Log.d("ScrollDebug",
            "CategoryScreen onDispose — index=${listState.firstVisibleItemIndex}, offset=${listState.firstVisibleItemScrollOffset}")
        viewModel.saveScrollPosition(
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
        )
    }
}
```

E no `LaunchedEffect(uiState.savedScrollIndex)`:

```kotlin
LaunchedEffect(uiState.savedScrollIndex) {
    android.util.Log.d("ScrollDebug",
        "CategoryScreen LaunchedEffect — savedIndex=${uiState.savedScrollIndex}, savedOffset=${uiState.savedScrollOffset}")
    if (uiState.savedScrollIndex > 0) {
        listState.scrollToItem(
            index = uiState.savedScrollIndex,
            scrollOffset = uiState.savedScrollOffset
        )
    }
}
```

---

## Não mexa em mais nada. Build, instale e aguarde instrução de teste.
