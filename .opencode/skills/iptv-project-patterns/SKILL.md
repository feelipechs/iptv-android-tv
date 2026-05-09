---
name: iptv-project-patterns
description: Padrões de arquitetura, navegação e estado do app IPTV Android TV — Clean Architecture, Hilt, Room, ViewModels
---

# Arquitetura e Padrões do Projeto IPTV

## Descrição
Padrões obrigatórios de arquitetura, navegação e estado para o app IPTV Android TV.

## Arquitetura
Clean Architecture: `domain → data → di → ui`

```
app/src/main/kotlin/com/iptv/tv/
├── data/
│   ├── local/dao/        # Room DAOs
│   ├── local/entity/     # Room Entities
│   ├── remote/api/       # XtreamApiService (Retrofit)
│   ├── remote/dto/       # DTOs de resposta da API
│   ├── ContentRepositoryImpl.kt    # Xtream
│   ├── M3uContentRepository.kt    # M3U
│   ├── FavoritesRepositoryImpl.kt
│   ├── WatchHistoryRepositoryImpl.kt
│   └── CredentialsRepositoryImpl.kt
├── di/
│   ├── AppModules.kt      # Hilt modules
│   └── ServerUrlInterceptor.kt
├── domain/
│   ├── model/Models.kt    # Category, Stream, Credentials, VodInfo, SeriesInfo, etc
│   ├── repository/        # Interfaces dos repositórios
│   └── usecase/UseCases.kt
├── ui/
│   ├── components/        # Componentes reutilizáveis (um arquivo = uma responsabilidade)
│   ├── screens/           # Uma pasta por tela
│   └── theme/Theme.kt
```

## Provedores de conteúdo
O app suporta dois tipos — `ProviderType.XTREAM` e `ProviderType.M3U_LIST`.
- `DelegatingContentRepository` em `AppModules.kt` roteia para o repositório correto
- Sempre leia `credentials.providerType` antes de fazer chamadas de rede

## Estado e ViewModel
Padrão obrigatório para todos os ViewModels:
```kotlin
private val _campo = MutableStateFlow(valorInicial)

val uiState: StateFlow<XyzUiState> = combine(
    _campo1, _campo2, ...
) { values -> XyzUiState(...) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), XyzUiState())
```

## Favoritos e Histórico
Implementado via Room, separado por tipo (LIVE / VOD / SERIES):
- `FavoritesRepository.toggleFavorite(stream)` — adiciona/remove
- `FavoritesRepository.getFavoritesByType(type)` — Flow<List<FavoriteEntry>>
- `WatchHistoryRepository.addToHistory(stream, progress, episodeNum?, episodTitle?, season?, episodeUrl?)` — grava ao selecionar stream

## Categorias especiais
```kotlin
const val FAVORITES_CATEGORY_ID = "favorites_special"
const val RECENTS_CATEGORY_ID   = "recents_special"
```
Essas IDs são tratadas especialmente no ViewModel de cada tipo — não fazem chamada de rede.
São sempre exibidas como itens fixos no **topo** da lista de categorias.

## Fluxo de navegação
```
HomeScreen
    → CategoryScreen(type)      # type = LIVE | VOD | SERIES
        → StreamScreen(categoryId, type)
            → Player             # Live
            → DetailScreen       # VOD
                → Player
            → SeriesDetailScreen # Series
                → Player
    → SettingsScreen
```

## Rotas em IPTVNavHost.kt
```kotlin
sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object LoginEdit     : Screen("login_edit")
    object Home          : Screen("home")
    object Category      : Screen("category/{type}") {
        fun route(type: ContentType) = "category/${type.name}"
    }
    object Stream        : Screen("stream/{categoryId}/{type}") {
        fun route(categoryId: String, type: ContentType) =
            "stream/${categoryId.encodeUrl()}/${type.name}"
    }
    object Detail        : Screen("detail/{streamId}/{streamUrl}/{streamName}/{posterUrl}/{categoryId}/{contentType}") {
        fun route(stream: Stream) = "detail/${stream.id}/${stream.streamUrl.encodeUrl()}/${stream.name.encodeUrl()}/${(stream.posterUrl ?: "").encodeUrl()}/${stream.categoryId}/${stream.type.name}"
    }
    object SeriesDetail  : Screen("series_detail/{streamId}/{streamUrl}/{streamName}/{posterUrl}/{categoryId}") {
        fun route(stream: Stream) = "series_detail/${stream.id}/${stream.streamUrl.encodeUrl()}/${stream.name.encodeUrl()}/${(stream.posterUrl ?: "").encodeUrl()}/${stream.categoryId}"
    }
    object Player        : Screen("player/{streamId}/{streamUrl}/{streamName}/{streamType}/{startPosition}") {
        fun route(streamId: String, streamUrl: String, streamName: String, streamType: ContentType, startPosition: Long = 0L) =
            "player/${streamId}/${streamUrl.encodeUrl()}/${streamName.encodeUrl()}/${streamType.name}/$startPosition"
    }
    object Favorites     : Screen("favorites")
    object Settings      : Screen("settings")
    object EditCredentials: Screen("edit_credentials")
}

// Utilitário — use sempre este, nunca inline
fun String.encodeUrl(): String =
    URLEncoder.encode(this, "UTF-8").replace("+", "%20")
```

## Componentes reutilizáveis obrigatórios
Cada arquivo em `ui/components/` tem uma única responsabilidade:

| Arquivo | Conteúdo |
|---|---|
| `TvSearchField.kt` | Campo de busca com ícone, foco, teclado |
| `LiveChannelCard.kt` | Card de canal ao vivo (nome + favoritar) |
| `PosterCard.kt` | Card de VOD/Série (poster + nome) |
| `CategoryItem.kt` | Item de categoria na lista vertical |
| `TvTextField.kt` | Campo de texto estilizado (login, edição) |
| `ProgressBar.kt` | Barra de progresso reutilizável |
| `FavoriteButton.kt` | Ícone de favorito com toggle |

## Temas
```kotlin
enum class AppTheme { LIGHT, DARK }
// Salvo no DataStore via CredentialsRepositoryImpl.saveTheme()
// clearCredentials() NÃO limpa o tema — usa chaves individuais
// Lido em MainViewModel e passado para IPTVTheme(theme = theme)
```

## Clean Architecture — regras de camada
- **DTOs** ficam em `data/remote/dto/` — nunca expostos para `ui/` ou `domain/`
- **Modelos de domínio** ficam em `domain/model/` — inclui `VodInfo`, `SeriesInfo`, `Episode`
- **ViewModels** nunca injetam `XtreamApiService` diretamente — sempre via `ContentRepository`
- **UiState** usa apenas modelos de domínio — nunca DTOs

## Commits
- Branch de desenvolvimento: `dev`
- Branch de release: `main` (merge via `--squash`)
- Nunca commitar a pasta `app/build/`
