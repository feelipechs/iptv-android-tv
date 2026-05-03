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
│   └── CredentialsRepositoryImpl.kt
├── di/
│   ├── AppModules.kt      # Hilt modules
│   └── ServerUrlInterceptor.kt
├── domain/
│   ├── model/Models.kt    # Category, Stream, Credentials, etc
│   ├── repository/        # Interfaces dos repositórios
│   └── usecase/UseCases.kt
├── ui/
│   ├── screens/           # Uma pasta por tela
│   └── theme/Theme.kt
```

## Provedores de conteúdo
O app suporta dois tipos — `ProviderType.XTREAM` e `ProviderType.M3U_LIST`.
- `ContentRepositoryFactory` em `AppModules.kt` roteia para o repositório correto
- Sempre leia `credentials.providerType` antes de fazer chamadas de rede

## Estado e ViewModel
Padrão obrigatório para todos os ViewModels:
```kotlin
// StateFlow para UI
private val _campo = MutableStateFlow(valorInicial)

// Exposto via combine() no uiState
val uiState: StateFlow<XyzUiState> = combine(
    _campo1, _campo2, ...
) { values -> XyzUiState(...) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), XyzUiState())
```

## Favoritos e Histórico
Já implementado via Room:
- `FavoritesRepository.toggleFavorite(stream)` — adiciona/remove
- `FavoritesRepository.isFavorite(streamId)` — Flow<Boolean>
- `WatchHistoryRepository.addToHistory(stream, progress)` — grava ao selecionar stream

## Categorias especiais (HomeScreen)
```kotlin
const val FAVORITES_CATEGORY_ID = "favorites_special"
const val RECENTS_CATEGORY_ID = "recents_special"
```
Essas IDs são tratadas especialmente no `HomeViewModel` — não fazem chamada de rede.

## Navegação
Rotas em `IPTVNavHost.kt`:
- `login` — LoginScreen (startDestination)
- `login_edit` — LoginScreen com botão voltar (editar provedor)
- `home` — HomeScreen
- `player/{streamUrl}` — PlayerScreen
- `favorites` — FavoritesScreen
- `settings` — SettingsScreen
- `edit_credentials` — EditCredentialsScreen
- `detail/{...}` — DetailScreen (VOD)

Ao navegar com dados de Stream, encode todos os parâmetros:
```kotlin
val encodedUrl = URLEncoder.encode(stream.streamUrl, "UTF-8")
navController.navigate(Screen.Player.route(encodedUrl))
```

## Temas
```kotlin
enum class AppTheme { LIGHT, DARK }
// Salvo no DataStore via CredentialsRepositoryImpl.saveTheme()
// Lido em MainViewModel e passado para IPTVTheme(theme = theme)
```

## Commits
- Branch de desenvolvimento: `dev`
- Branch de release: `main` (merge via `--squash`)
- Nunca commitar a pasta `app/build/`
