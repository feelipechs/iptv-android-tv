# Prompt — Ajustes UI + Bug Série Duplicada

Consulte o AGENTS.md e as skills antes de começar.
Execute tudo em sequência sem pausar. Pare apenas em erro de compilação.

---

## BUG 1 — Série duplicada no histórico

**Arquivo:** `SeriesDetailViewModel.kt`

O problema: `addToHistory()` salva o episódio com `stream.id` (series_id)
mas em algum ponto também está sendo salvo com `episode.id` como streamId,
gerando entrada duplicada no Room.

Localize TODOS os pontos onde `watchHistoryRepository.addToHistory()` é chamado
para séries — no SeriesDetailViewModel e em qualquer outro arquivo.

Regra obrigatória:
- Para séries, o `streamId` salvo no histórico deve ser SEMPRE `stream.id` (series_id)
- NUNCA usar `episode.id` como streamId
- O `name` salvo deve ser o nome da série (`stream.name`), não o título do episódio
- `episodeNum`, `episodeTitle`, `season`, `episodeUrl` são campos separados

Em `addToHistory()` no SeriesDetailViewModel, adicione verificação de duplicata:
```kotlin
val existing = watchHistoryRepository.getHistoryEntry(s.id)
if (existing != null &&
    existing.lastSeason == season &&
    existing.lastEpisodeNum == episode.episodeNum.toIntOrNull()) {
    return@launch
}
```

---

## AJUSTE 1 — LiveChannelCard: 1 card por linha

**Arquivo:** `StreamScreen.kt`

Quando `type == ContentType.LIVE`, mude o grid para:
```kotlin
GridCells.Fixed(1)
```

E no `LiveChannelCard.kt`, adicione `Modifier.fillMaxWidth()` como modificador
padrão para que o card ocupe a largura total da tela.

---

## AJUSTE 2 — LoginScreen: ícone no lugar do título

**Arquivo:** `LoginScreen.kt`

Remova o texto "IPTV Player" (ou "KotlinTV") do topo.
Substitua por:
```kotlin
Image(
    painter = painterResource(id = R.drawable.ic_app_logo),
    contentDescription = "KotlinTV",
    modifier = Modifier.size(96.dp)
)
```
Centralizado no topo do formulário, acima dos botões Xtream/M3U.
Importe: `androidx.compose.foundation.Image` e `androidx.compose.ui.res.painterResource`

---

## AJUSTE 3 — HomeScreen: novo layout

**Arquivo:** `HomeScreen.kt` e `HomeViewModel.kt`

### HomeViewModel.kt
Não precisa de alteração — os cards já navegam corretamente.

### HomeScreen.kt
Use um `Box` ocupando a tela toda (`fillMaxSize`):

**Canto superior esquerdo** (`Alignment.TopStart`, padding 24dp):
```kotlin
Row(verticalAlignment = Alignment.CenterVertically) {
    Image(
        painter = painterResource(id = R.drawable.ic_app_logo),
        contentDescription = "KotlinTV",
        modifier = Modifier.size(48.dp)
    )
    Spacer(Modifier.width(12.dp))
    Text(
        text = "KotlinTV",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}
```

**Canto superior direito** (`Alignment.TopEnd`, padding 24dp):
Ícone de configurações — já existe, apenas reposicione dentro do Box.

**Centro** (`Alignment.Center`):
Os 3 cards em Row — sem título acima deles.
Remova qualquer Text de título ("IPTV" ou similar) que esteja acima dos cards.

**Canto inferior esquerdo** (`Alignment.BottomStart`, padding 24dp):
```kotlin
Text(
    text = "developed by chagas",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

**Canto inferior direito** (`Alignment.BottomEnd`, padding 24dp):
```kotlin
Text(
    text = "version ${BuildConfig.VERSION_NAME}",
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```
Importe: `com.iptv.tv.BuildConfig`

---

## Verificação final
- Build deve passar sem erros
- Não mexa em nenhum outro arquivo além dos citados
