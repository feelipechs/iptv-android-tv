---
name: xtream-api
description: Referência dos endpoints Xtream Codes, DTOs, URLs de stream e servidor mock de testes
---

# Xtream Codes API

## Descrição
Referência completa da API Xtream Codes usada no app IPTV.

## Endpoints implementados

### Autenticação / User Info
```
GET /player_api.php?username=&password=
```
Resposta: `UserInfoResponse` — verifica `userInfo.status == "Active"`

### Categorias
```
GET /player_api.php?username=&password=&action=get_live_categories
GET /player_api.php?username=&password=&action=get_vod_categories
GET /player_api.php?username=&password=&action=get_series_categories
```
Resposta: `List<CategoryDto>`

### Streams
```
GET /player_api.php?username=&password=&action=get_live_streams&category_id=
GET /player_api.php?username=&password=&action=get_vod_streams&category_id=
GET /player_api.php?username=&password=&action=get_series&category_id=
```
Resposta: `List<LiveStreamDto>`, `List<VodStreamDto>`, `List<SeriesStreamDto>`

### Detalhes de VOD
```
GET /player_api.php?username=&password=&action=get_vod_info&vod_id=
```
Resposta: `VodInfoDto` com `info` (descrição, elenco, diretor, etc) e `movie_data`

## URLs de stream
```kotlin
// Live
"{server}/live/{username}/{password}/{streamId}.m3u8"

// VOD
"{server}/movie/{username}/{password}/{streamId}.{containerExtension}"

// Series
"{server}/series/{username}/{password}/{streamId}.{containerExtension}"
```
Esses são gerados em `Credentials.liveUrl()`, `vodUrl()`, `seriesUrl()` em `Models.kt`.

## DTOs principais

### CategoryDto
```kotlin
category_id, category_name, parent_id, num (stream_count)
```

### VodStreamDto
```kotlin
stream_id, name, stream_icon, category_id, container_extension, direct_source, rating
```

### VodInfoDto
```kotlin
info: VodInfo (name, cover_big, description, plot, cast, director, genre, release_date, rating, duration)
movie_data: VodMovieData (stream_id, name, container_extension)
```

## Servidor de testes (mock Node.js)
Para desenvolvimento sem servidor Xtream real:
- Servidor em `~/workspace/teste/api-test/server.js`
- Porta: 3000
- Usuário: `test` / Senha: `test`
- Vídeo de teste gerado com ffmpeg:
  ```bash
  ffmpeg -f lavfi -i color=c=blue:size=1280x720:rate=25 -t 5 video.mp4
  ```
- Iniciar: `node server.js`
- URL no app: `http://SEU_IP_LOCAL:3000`

## Observações importantes
- O endpoint `get_user_info` sem `action` retorna o mesmo que com `action=get_user_info`
- O `ServerUrlInterceptor` substitui o `baseUrl` do Retrofit pela URL do servidor salva no DataStore
- Para M3U, o interceptor deve retornar `chain.proceed(chain.request())` sem modificar a URL
