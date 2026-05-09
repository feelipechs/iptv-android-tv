# Plano de Refatoração — IPTV Android TV

## Visão geral

Refatoração completa do layout e navegação do app, mantendo a lógica de negócio existente (login, player, favoritos, histórico).

### Fluxo alvo
```
HomeScreen (3 cards: TV ao Vivo | Filmes | Séries + ícone Config)
→ CategoryScreen(type)
  Lista vertical de categorias + Favoritos (fixo topo) + Recentes (fixo topo)
  Campo de busca para categorias
→ StreamScreen(categoryId, type)
  Grid de streams
  → Player (Live)
  → DetailScreen (VOD) → Player
  → SeriesDetailScreen (Série) → Player
  → SettingsScreen (reaproveitada)
```

### Princípios
- Um arquivo = uma responsabilidade
- Componentes desacoplados em `ui/components/`
- Sem botão "Voltar" na UI — Back via D-pad/controle
- Sem painéis simultâneos — uma tela de cada vez
- Cards diferentes por tipo: Live (nome + favoritar) vs VOD/Série (poster + nome)
- Favoritos e Recentes separados por tipo (LIVE / VOD / SERIES)

---

## FASE 0 — Concluída ✅
Remoção de código morto e correção de bugs críticos (clearCredentials, onPlayEpisode, race condition DelegatingContentRepository).

---

## FASE 1 — Concluída ✅
Componentes reutilizáveis criados em `ui/components/`:
- TvTextField, TvSearchField, LiveChannelCard, PosterCard, CategoryItem, ProgressBar, FavoriteButton
- LoginScreen e EditCredentialsScreen atualizados para usar TvTextField

---

## FASE 2 — Concluída ✅
HomeScreen substituída por 3 cards grandes (TV ao Vivo, Filmes, Séries) + ícone Config.
HomeViewModel simplificado (só verifica login via CredentialsRepository).

---

## FASE 3 — Concluída ✅
CategoryScreen criada com lista vertical de categorias, Favoritos/Recentes fixos, campo de busca.
CategoryViewModel com GetCategoriesUseCase + RefreshContentUseCase.

---

## FASE 4 — Concluída ✅
StreamScreen criada com grid de streams diferenciado por tipo.
StreamViewModel com GetStreamsUseCase, RefreshStreamsUseCase, FavoritesRepository, WatchHistoryRepository.
Suporte a categorias especiais (Favoritos, Recentes).

---

## FASE 5 — Concluída ✅
Limpeza final:
- NavigationUtils.kt criado com encodeUrl()/decodeUrl()
- Rotas órfãs removidas (ContentScreen, CategoryScreen antiga)
- PlayerViewModel.onCleared() corrigido para salvar progresso de SERIES
- Build passando

---

## FASE 6 — Concluída ✅
Polimento UI:
- CategoryItem com ícone opcional (Favoritos = coração, Recentes = histórico)
- Divider entre itens fixos e dinâmicos na CategoryScreen
- Todos os estados de loading/error com CircularProgressIndicator e mensagem PT-BR
- Nenhum Dialog em nenhuma tela
- Textos em português

---

## Restrições globais (consultar sempre)

- **Nunca** usar `androidx.compose.material3` para UI — só `androidx.tv.material3`
- **Nunca** adicionar botão "Voltar" na UI
- **Nunca** expor DTOs na camada UI — apenas modelos de domínio
- **Nunca** injetar `XtreamApiService` diretamente em ViewModels
- **Sempre** usar `Screen.X.route(...)` para navegação
- **Sempre** usar `.encodeUrl()` em parâmetros de URL
- **Sempre** `singleLine = true` em BasicTextField
- **Sempre** 4 cores no `ClickableSurfaceDefaults`
