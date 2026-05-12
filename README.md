# IPTV Player — Android TV

Player IPTV para Android TV construído com Kotlin + Jetpack Compose TV. Suporta fontes **Xtream Codes** e **playlists M3U**.

---

## Stack

| Camada | Tecnologia |
|---|---|
| UI | Jetpack Compose + `androidx.tv` (TV Material 3) |
| Navegação | Navigation Compose |
| Player | Media3 / ExoPlayer (HLS, DASH) |
| Injeção de dependência | Hilt |
| API | Retrofit 2 + Gson + OkHttp |
| Cache local | Room 2 |
| Credenciais | DataStore Preferences |
| Imagens | Coil 2 |

---

## Funcionalidades

- **TV ao Vivo** — lista de canais por categoria, reprodução direta
- **Filmes (VOD)** — grade de posters, detalhe com sinopse, rating, elenco
- **Séries** — navegação por temporadas e episódios
- **Continuar assistindo** — progresso salvo automaticamente ao sair do player
  - Filmes: retoma do ponto exato com indicador de percentual
  - Séries: retoma no episódio e temporada corretos
- **Favoritos** — salvo localmente no Room
- **Recentes** — histórico de reprodução por tipo de conteúdo
- **Busca** — filtragem local de categorias e streams
- **Temas** — modo claro e escuro, persistido entre sessões

---

## Arquitetura

MVVM + Clean Architecture com 3 camadas:

```
domain/     → modelos puros, interfaces de repositório, use cases
data/       → Room, Retrofit, DataStore, M3UParser, implementações
ui/         → ViewModels, Screens (Composable), componentes, tema
```

Roteamento de fonte de dados via `DelegatingContentRepository` — seleciona entre `ContentRepositoryImpl` (Xtream) e `M3uContentRepository` (M3U) com base no tipo de credencial salvo.

---

## Estrutura do projeto

```
app/src/main/kotlin/com/iptv/tv/
├── data/
│   ├── local/
│   │   ├── dao/                    # CategoryDao, StreamDao, FavoriteDao, WatchHistoryDao
│   │   ├── entity/                 # Entities Room (Category, Stream, Favorite, WatchHistory)
│   │   └── AppDatabase.kt
│   ├── remote/
│   │   ├── api/                    # XtreamApiService (Retrofit)
│   │   └── dto/                    # DTOs da API Xtream Codes
│   ├── ContentRepositoryImpl.kt    # Repositório Xtream Codes
│   ├── M3uContentRepository.kt     # Repositório M3U
│   ├── M3UParser.kt                # Parser de playlists M3U
│   ├── FavoritesRepositoryImpl.kt  # Favoritos + Histórico de reprodução
│   └── CredentialsRepositoryImpl.kt
├── di/
│   ├── AppModules.kt               # 5 módulos Hilt
│   └── ServerUrlInterceptor.kt     # Injeta URL do servidor nos requests Retrofit
├── domain/
│   ├── model/Models.kt             # Category, Stream, Credentials, WatchHistoryEntry, FavoriteEntry
│   ├── repository/Repositories.kt  # Interfaces de repositório
│   └── usecase/UseCases.kt         # 10 use cases
├── player/
│   ├── PlayerManager.kt            # Singleton wrapper do ExoPlayer
│   └── PlaybackService.kt          # MediaSessionService para background playback
├── ui/
│   ├── components/                 # CategoryItem, PosterCard, LiveChannelCard, TvSearchField...
│   ├── screens/
│   │   ├── category/               # Listagem de categorias
│   │   ├── detail/                 # Detalhe de filme e série
│   │   ├── favorites/              # Favoritos
│   │   ├── home/                   # Tela inicial
│   │   ├── login/                  # Login Xtream / M3U
│   │   ├── player/                 # Player de vídeo
│   │   ├── settings/               # Configurações e credenciais
│   │   └── stream/                 # Lista de streams por categoria
│   ├── theme/Theme.kt              # IPTVTheme, AppTheme, ColorSchemes
│   ├── IPTVNavHost.kt              # Grafo de navegação
│   └── NavigationUtils.kt          # encodeUrl / decodeUrl
├── IPTVApp.kt
└── MainActivity.kt
```

---

## Como compilar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17
- Android SDK API 35

### Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

### Instalar via ADB

```bash
# Conectar via Wi-Fi
adb connect <IP_DA_TV>:5555

# Instalar
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Logs
adb logcat
```

---

## Fluxo de dados

```
Xtream API / M3U
      ↓
  Repository (busca + mapeia DTOs → entities)
      ↓
    Room (cache local)
      ↓
  Use Cases
      ↓
  ViewModel (StateFlow<UiState>)
      ↓
  Screen (Composable)
```

**Offline-first:** exibe dados do cache Room imediatamente e atualiza em background via `refreshCategories()` / `refreshStreams()`.

---

## Decisões de design

**URL dinâmica:** `ServerUrlInterceptor` injeta a URL do servidor salva no DataStore em cada request Retrofit, sem precisar recriar o client.

**Cache por categoria:** `replaceByCategory()` no Room é atômico — apaga e reinsere os streams de uma categoria sem afetar as demais.

**Histórico com chave de série:** para séries, o `streamId` salvo no histórico é sempre o `seriesId` (não o `episodeId`), com campos separados para `lastSeason`, `lastEpisodeNum` e `lastEpisodeUrl`.

**Player sem controles no LIVE:** canais ao vivo não exibem overlay de controles — o D-pad e o botão back do controle remoto são suficientes.

**Tema persistido:** `AppTheme` (LIGHT/DARK) salvo no DataStore junto com as credenciais. `MainViewModel` lê e repassa ao `IPTVTheme`.

---

## Limitações conhecidas

- Séries via M3U não são suportadas (`getSeriesInfo` não implementado para M3U)
- EPG não implementado (campo `epgChannelId` existe no modelo mas não é usado)
- Sem testes automatizados
- Scroll position não é preservada ao voltar pelo back stack (limitação do Compose Navigation com Hilt)
- Controles avançados do player ausentes (legendas, faixa de áudio, velocidade)
