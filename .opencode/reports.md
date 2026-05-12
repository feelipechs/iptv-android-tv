# Relatório do Projeto — IPTV Android TV

---

## 1. Arquitetura geral

**1.1 Padrão arquitetural:** MVVM + Clean Architecture (camadas domain → data → di → ui)

**1.2 Camadas e organização em pacotes:**
```
com.iptv.tv/
├── data/
│   ├── local/dao/          # Room DAOs (CategoryDao, StreamDao, FavoriteDao, WatchHistoryDao)
│   ├── local/entity/       # Room Entities (CategoryEntity, StreamEntity, FavoriteEntity, WatchHistoryEntity)
│   ├── remote/api/         # XtreamApiService (Retrofit)
│   ├── remote/dto/         # DTOs da API (CategoryDto, LiveStreamDto, VodStreamDto, etc.)
│   ├── ContentRepositoryImpl.kt    # Repositório Xtream Codes
│   ├── M3uContentRepository.kt     # Repositório M3U
│   ├── M3UParser.kt                # Parser de playlists M3U
│   ├── FavoritesRepositoryImpl.kt  # Favoritos + Histórico (ambos neste arquivo)
│   └── CredentialsRepositoryImpl.kt # Credenciais via DataStore
├── di/
│   ├── AppModules.kt               # 5 módulos Hilt (Network, Database, Repository, M3u, CoroutineScope)
│   └── ServerUrlInterceptor.kt     # OkHttp Interceptor para URL dinâmica
├── domain/
│   ├── model/Models.kt             # Category, Stream, Credentials, WatchHistoryEntry, FavoriteEntry
│   ├── repository/Repositories.kt  # Interfaces ContentRepository, FavoritesRepository, WatchHistoryRepository
│   └── usecase/UseCases.kt         # 10 use cases (GetCategories, GetStreams, RefreshContent, etc.)
├── player/
│   ├── PlayerManager.kt            # Singleton wrapper do ExoPlayer
│   └── PlaybackService.kt          # MediaSessionService para background playback
└── ui/
    ├── IPTVNavHost.kt              # Grafo de navegação (sealed class Screen + NavHost)
    ├── MainViewModel.kt            # Fornece AppTheme via CredentialsRepository
    ├── NavigationUtils.kt          # encodeUrl()/decodeUrl()
    ├── components/                 # 7 componentes reutilizáveis
    ├── screens/                    # 8 pastas (category, detail, favorites, home, login, player, settings, stream)
    └── theme/Theme.kt              # IPTVTheme, AppTheme enum, ColorSchemes, Typography
```

**1.3 Injeção de dependência:** Hilt (Dagger) sobre `SingletonComponent`. Configurado em `AppModules.kt` com 5 módulos: `CoroutineScopeModule`, `NetworkModule` (OkHttp + Retrofit + XtreamApiService), `DatabaseModule` (Room + DAOs + DataStore), `M3uModule`, `RepositoryModule` (@Binds) e `ContentRepositoryModule` (DelegatingContentRepository). Qualifier customizado `@ApplicationScope`.

**1.4 Separação entre camadas:**
- **Domain:** Modelos puros (Models.kt), interfaces de repositório (Repositories.kt, CredentialsRepository.kt), use cases (UseCases.kt). Zero dependência de Android ou framework.
- **Data:** Implementações de repositório (Room, Retrofit, DataStore), entidades, DTOs, DAOs. Faz mapeamento entity↔domain e dto↔entity.
- **UI:** ViewModels (consomem use cases/repositórios), Screens (Composable), componentes reutilizáveis, tema.

---

## 2. Tecnologias e bibliotecas

**2.1 Versões:**
- Kotlin: **2.0.0**
- minSdk: **23** | targetSdk: **35** | compileSdk: **35**
- JVM target: **17**

**2.2 Principais dependências:**

| Biblioteca | Versão |
|---|---|
| Compose BOM | 2024.06.00 |
| Compose TV (tv-foundation, tv-material) | 1.0.0-rc01 |
| Lifecycle (viewmodel-compose, runtime-compose) | 2.8.2 |
| Navigation Compose | 2.8.0-beta04 |
| Hilt Android | 2.51.1 |
| Hilt Navigation Compose | 1.2.0 |
| Room (runtime, ktx, compiler) | 2.6.1 |
| Retrofit + converter-gson | 2.11.0 |
| OkHttp logging-interceptor | 4.12.0 |
| DataStore Preferences | 1.1.1 |
| Media3 (exoplayer, hls, ui, session) | 1.3.1 |
| Kotlinx Coroutines Android | 1.8.1 |
| Coil Compose | 2.6.0 |
| Leanback | 1.0.0 |

**2.3 Jetpack Compose TV (`androidx.tv`):** Sim, extensivamente. `androidx.tv.material3` é usado em **16+ arquivos** — todos os Screens, Cards, Theme. Import opt-in: `ExperimentalTvMaterial3Api`. Nenhum `compose.material3` é usado para UI (apenas `CircularProgressIndicator`).

**2.4 Player:** Media3/ExoPlayer (1.3.1). `PlayerManager` é `@Singleton`, expõe `ExoPlayer` + `StateFlow<PlayerState>`. `PlaybackService` é `MediaSessionService` para background playback com audio focus. Suporta HLS, DASH e formatos padrão.

**2.5 Carregamento de imagens:** **Coil** 2.6.0 (`io.coil-kt:coil-compose`), usado via `AsyncImage` em `DetailScreen`, `SeriesDetailScreen`, `PosterCard`.

---

## 3. Navegação

**3.1 Implementação:** Navigation Compose (2.8.0-beta04). Rotas definidas em `sealed class Screen` em `IPTVNavHost.kt`. Parâmetros passados como path arguments (não SafeArgs). Codificação via `URLEncoder.encode()` com `replace("+", "%20")`.

**3.2 Telas (11 rotas):** Login, LoginEdit, Home, Category, Stream, Detail, SeriesDetail, Player, Favorites, Settings, EditCredentials.

**3.3 Parâmetros entre telas:** URL encoding manual via `String.encodeUrl()` / `String.decodeUrl()` em `NavigationUtils.kt`. Decodificação com `URLDecoder.decode(value, "UTF-8")`. A rota Player tem 10 argumentos, a maioria com defaultValue vazio.

**3.4 Limitações conhecidas:**
- Rotas com muitos parâmetros na URL (Player tem 10 args) — frágil e difícil de manter
- Sem SafeArgs — sem type-safety em tempo de compilação
- `FavoritesScreen` não é acessível via HomeScreen (apenas via rota `"favorites"`, sem botão visível)

---

## 4. Fontes de dados

**4.1 Tipos de fonte:** 2 — `ProviderType.XTREAM` (Xtream Codes API) e `ProviderType.M3U_LIST` (playlist M3U). Roteamento via `DelegatingContentRepository` em `AppModules.kt`.

**4.2 Armazenamento de credenciais:** DataStore Preferences. Chaves: `server`, `username`, `password`, `provider_type`, `m3u_source`, `theme`. `clearCredentials()` remove apenas chaves de credencial, preserva `theme`.

**4.3 Cache local:** Room database (`iptv_cache.db`, versão 4). Estratégia: cache-first — leitura do Room via `Flow`, refresh em background via API/M3U que faz `replaceAll()` / `replaceByCategory()` atômico. 3 migrações implementadas (1→2, 2→3, 3→4).

**4.4 Entidades e campos:**

| Entidade | Tabela | Campos principais |
|---|---|---|
| CategoryEntity | `categories` | id (PK), name, type, streamCount, updatedAt |
| StreamEntity | `streams` | id (PK), name, categoryId, type, streamUrl, posterUrl?, epgChannelId?, containerExtension, updatedAt |
| FavoriteEntity | `favorites` | streamId (PK), name, categoryId, type, posterUrl?, streamUrl, addedAt |
| WatchHistoryEntity | `watch_history` | streamId (PK), name, categoryId, type, posterUrl?, streamUrl, lastWatchedAt, progress, lastEpisodeNum?, lastEpisodeTitle?, lastSeason?, lastEpisodeUrl? |

**4.5 Refresh de categorias/streams:** `refreshCategories(type)` busca da API, mapeia DTOs → entities, chama `categoryDao.replaceAll()`. `refreshStreams(categoryId, type)` busca da API, chama `streamDao.replaceByCategory()`. Ambos rodam em background após a leitura inicial do cache. Para M3U, parseia a fonte e popula o Room.

---

## 5. Funcionalidades principais

**5.1 Tipos de conteúdo:** LIVE, VOD, SERIES — definidos em `ContentType` enum.

**5.2 Histórico de reprodução:** Salvo em Room (`watch_history`). Dados: streamId, name, categoryId, type, posterUrl, streamUrl, progress (0f–1f), lastWatchedAt, lastEpisodeNum, lastEpisodeTitle, lastSeason, lastEpisodeUrl. Gravado ao sair do Player (`onCleared`) via `applicationScope`. Para SERIES, usa `seriesId` como chave (não episodeId).

**5.3 Favoritos:** Room-backed. `toggleFavorite()` insere/remove atomicamente. Exibidos como categoria especial no topo (`FAVORITES_CATEGORY_ID`). Contagem por tipo disponível via DAO.

**5.4 Progresso de reprodução:** Salvo como `Float` (0f–1f) no `WatchHistoryEntity`. Restaurado no Player: se `startPosition == -1L`, busca `watchHistoryRepository.getHistoryEntry()` e faz `seekTo(progress * duration)`. Tenta por até 30 iterações (500ms cada) aguardando duration > 0.

**5.5 Busca:** Local, client-side. `StreamViewModel` filtra a lista em memória via `streams.filter { it.name.contains(query, ignoreCase = true) }`. `CategoryViewModel` filtra categorias similarly. Campo de busca via `TvSearchField` componente.

---

## 6. Player

**6.1 Arquitetura:** `PlayerManager` (@Singleton) → `PlayerViewModel` → `PlayerScreen`. PlayerManager encapsula ExoPlayer + StateFlow<PlayerState>. PlayerViewModel consome PlayerManager e salva histórico. PlayerScreen é Composable que renderiza o player UI.

**6.2 Tipos de stream:** LIVE usa URL direta do servidor (`/live/...`). VOD usa `/movie/...` com `containerExtension`. SERIES usa `/series/...` com `containerExtension`. URLs geradas por `Credentials.liveUrl()`, `vodUrl()`, `seriesUrl()`.

**6.3 Salvamento do histórico:** Em `PlayerViewModel.onCleared()` — se `streamId.isNotBlank() && duration > 0L`, calcula `progress = currentPosition / duration`, cria `Stream` e chama `watchHistoryRepository.addToHistory()` via `applicationScope` (sobrevive à destruição do ViewModel).

**6.4 Controles UI:** Play/Pause (togglePlayPause), Seek (seekTo), D-pad back. Overlay com nome do stream e posição. Sem controles avançados (sem subtitle, audio track, speed).

**6.5 MediaSession/PlaybackService:** `PlaybackService` é `MediaSessionService` com `foregroundServiceType="mediaPlayback"`. Configura `AudioAttributes` (USAGE_MEDIA, CONTENT_TYPE_MOVIE), audio focus, e `MediaSession` para integração com sistema.

---

## 7. Componentes UI reutilizáveis

**7.1 Componentes em `ui/components/`:**

| Arquivo | Descrição |
|---|---|
| `CategoryItem.kt` | Item de categoria na lista vertical — nome + contagem + ícone, focável |
| `FavoriteButton.kt` | Ícone de coração com toggle favorito/não-favorito |
| `LiveChannelCard.kt` | Card de canal ao vivo — nome centralizado + botão favoritar |
| `PosterCard.kt` | Card de VOD/Série — poster (AsyncImage) + nome abaixo, favoritar |
| `ProgressBar.kt` | Barra de progresso horizontal reutilizável |
| `TvSearchField.kt` | Campo de busca com ícone, foco D-pad, teclado virtual |
| `TvTextField.kt` | Campo de texto estilizado para login/edição de credenciais |

**7.3 Modo claro e escuro:** Sim. `AppTheme` enum com `LIGHT` e `DARK`. Salvo em DataStore via `CredentialsRepositoryImpl.saveTheme()`. `IPTVTheme` composable aplica `darkColorScheme` ou `lightColorScheme` conforme o tema. `MainViewModel` lê o tema e repassa.

---

## 8. Estado atual e pendências

**8.1 TODOs/FIXMEs:** Nenhum. Todos foram removidos na limpeza.

**8.2 Funcionalidades incompletas:**
- Scroll position preservation foi removida (não funcionava)
- EPG não implementado (campo `epgChannelId` existe em Stream mas não é usado)
- SERIES em M3U não suportado (`getSeriesInfo` lança erro)
- Controles avançados do player ausentes (subtitle, audio track, speed)
- `FavoritesScreen` não tem ponto de entrada na UI (rota existe mas sem navegação)

**8.3 Código morto:**
- `extractEpisodeIdFromUrl()` em `StreamScreen.kt` — função privada não chamada
- `addFavorite()` e `removeFavorite()` em `FavoritesRepository` — expostos mas não usados (tudo usa `toggleFavorite`)

**8.4 Lógica duplicada:**
- `HomeScreen` e `StreamScreen` têm lógica similar de navegação por tipo (LIVE→Player, VOD→Detail, SERIES→SeriesDetail)
- `IPTVNavHost` repete a mesma lógica de navegação 3 vezes (StreamScreen callback, FavoritesScreen callback)
- `DetailViewModel` usa `XtreamApiService` diretamente em vez de `ContentRepository` (viola Clean Architecture)

---

## 9. Qualidade e padrões

**9.1 Testes:** Nenhum. Zero arquivos de teste unitário ou instrumentado.

**9.2 Lint/análise estática:** Nenhuma configurada. Sem `lint.xml`, `.detekt.yml` ou similar.

**9.3 Convenção de nomenclatura:** Geralmente consistente. Inconsistências:
- `WatchHistoryRepositoryImpl` está no mesmo arquivo que `FavoritesRepositoryImpl.kt` (deveria ter arquivo próprio)
- DTOs e entities em arquivos únicos grandes (`Dtos.kt`, `Entities.kt`, `Daos.kt`) — poderia ser separados
- `AppModules.kt` contém 5+ módulos em um único arquivo

**9.4 Tratamento de erros:** Parcialmente consistente. `runCatching` + `onFailure` em ViewModels e repositórios. Erros expostos via `error: String?` nos UiState. `Log.e` em alguns catch blocks (DetailViewModel, LoginViewModel). Porém:
- `ContentRepositoryImpl.validateCredentials()` cria Retrofit temporário sem tratamento de timeout
- Erros de rede não são classificados (sem distinção timeout/conexão/HTTP)

---

## 10. Build e distribuição

**10.1 Configuração:** Gradle Kotlin DSL, AGP 8.13.2, KSP 2.0.0-1.0.21. Version catalog em `gradle/libs.versions.toml`.

**10.2 Flavors/build types:** Debug (com `applicationIdSuffix = ".debug"`) e Release (com `isMinifyEnabled = true`, `isShrinkResources = true`). Sem flavors adicionais.

**10.3 applicationId:** `com.iptv.tv` | versionName: `1.0.0` | versionCode: `1`

**10.4 ProGuard/R8:** Configurado em `app/proguard-rules.pro`. Mantém DTOs, entities, classes Gson, Media3. Suprime warnings do OkHttp/OkIO. Release build com R8 full mode.
