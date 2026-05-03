# IPTV Player — Android TV

Player IPTV focado em um único fornecedor **Xtream Codes**, construído com Kotlin + Jetpack Compose + Android TV.

## Stack

| Camada | Tecnologia |
|---|---|
| UI | Jetpack Compose + `androidx.tv` (TV Material 3) |
| Navegação | Navigation Compose |
| Player | Media3 / ExoPlayer (HLS nativo) |
| Injeção | Hilt |
| API | Retrofit 2 + Gson + OkHttp |
| Cache | Room 2 |
| Credenciais | DataStore Preferences |
| Imagens | Coil 2 |

## Estrutura do projeto

```
app/src/main/kotlin/com/iptv/tv/
├── data/
│   ├── local/
│   │   ├── dao/           # CategoryDao, StreamDao
│   │   ├── entity/        # Room entities
│   │   └── AppDatabase.kt
│   ├── remote/
│   │   ├── api/           # XtreamApiService (Retrofit)
│   │   └── dto/           # DTOs de resposta da API
│   ├── ContentRepositoryImpl.kt
│   └── CredentialsRepositoryImpl.kt
├── di/
│   ├── AppModules.kt      # NetworkModule, DatabaseModule, RepositoryModule
│   └── ServerUrlInterceptor.kt
├── domain/
│   ├── model/             # Models.kt (Category, Stream, Credentials)
│   ├── repository/        # Interfaces dos repositórios
│   └── usecase/           # GetCategories, GetStreams, RefreshContent
├── player/
│   ├── PlaybackService.kt # MediaSessionService (Media3)
│   └── PlayerManager.kt   # Singleton do ExoPlayer
├── ui/
│   ├── screens/
│   │   ├── login/         # LoginViewModel + LoginScreen
│   │   ├── category/      # CategoryViewModel + CategoryScreen
│   │   ├── content/       # ContentViewModel + ContentScreen
│   │   └── player/        # PlayerViewModel + PlayerScreen
│   ├── theme/             # IPTVTheme (escuro, alto contraste)
│   └── IPTVNavHost.kt     # Navegação principal
├── IPTVApp.kt             # Application (@HiltAndroidApp)
└── MainActivity.kt
```

## Como compilar

### Pré-requisitos

- Android Studio Hedgehog ou superior
- JDK 17 (`sdk use java 17` via sdkman)
- Android SDK API 35

### Build

```bash
# Debug (para TV stick via ADB)
./gradlew assembleDebug

# Release (APK assinado)
./gradlew assembleRelease
```

### Instalar via ADB no TV Stick (MITV-AESP0)

```bash
# Conectar via Wi-Fi
adb connect <IP_DA_TV>:5555

# Instalar APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Verificar logs
adb logcat -s "IPTVPlayer"
```

## Normalização de categorias

O `CategoryNormalizer` (em `UseCases.kt`) limpa automaticamente os prefixos do fornecedor:

```
"〽️ Series | Ação"     →  "Ação"
"〽️ Filmes | Drama"    →  "Drama"
"〽️ SÉRIES | animação" →  "Animação"
```

A deduplicação agrupa por nome em lowercase para eliminar duplicatas com capitalização diferente.

## Fluxo de dados

```
API Xtream → Retrofit → RepositoryImpl → Room (cache)
                                       ↓
                          GetCategoriesUseCase (normaliza)
                                       ↓
                          CategoryViewModel → CategoryScreen
```

O app segue o padrão **offline-first**: exibe dados do cache Room imediatamente e atualiza em background via `refreshCategories()`.

## Endpoints usados

| Ação | Endpoint |
|---|---|
| Validar login | `player_api.php?username=&password=` |
| Categorias LIVE | `?action=get_live_categories` |
| Streams LIVE | `?action=get_live_streams&category_id=` |
| Categorias VOD | `?action=get_vod_categories` |
| Streams VOD | `?action=get_vod_streams&category_id=` |
| Categorias Séries | `?action=get_series_categories` |
| URL stream | `{server}/live/{user}/{pass}/{id}.m3u8` |

## Decisões de design

- **URL dinâmica**: `ServerUrlInterceptor` injeta a URL do servidor salva no DataStore em cada request Retrofit, sem precisar recriar o client.
- **LIVE como lista, VOD como grade**: navegação mais rápida no D-pad para canais ao vivo; thumbnails de poster para VOD.
- **Cache 1 nível**: Room como única camada de persistência. TTL não implementado propositalmente — o usuário usa o gesto de refresh ou o app atualiza no início de cada sessão.
- **Sem EPG**: removido do escopo para manter a performance no hardware limitado do MITV-AESP0.

## Próximos passos (opcionais)

- [ ] Tela de detalhes do VOD (sinopse, ano, rating)
- [ ] Busca global com `SearchView` TV-friendly
- [ ] Favoritos (tabela local Room)
- [ ] TTL de cache configurável
- [ ] Retry automático com backoff no ExoPlayer
