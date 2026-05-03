package com.iptv.tv.domain.usecase

import java.text.Normalizer
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import com.iptv.tv.domain.repository.ContentRepository
import com.iptv.tv.domain.repository.FavoritesRepository
import com.iptv.tv.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Normaliza categorias vindas do fornecedor Xtream.
 * Remove prefixos, emojis, entidades HTML e caracteres especiais.
 * NÃO remove acentos (isso é feito apenas em dedup() para comparação).
 */
object CategoryNormalizer {

    fun normalize(rawName: String): String {
        var name = rawName.trim()

        // Remove entidades HTML
        name = name.replace("&gt;", "")
        name = name.replace("&lt;", "")
        name = name.replace("&amp;", "")

        // Remove caractere > diretamente
        name = name.replace(">", "")

        // Remove tudo antes do primeiro "|" (incluindo o "|")
        val pipeIndex = name.indexOf('|')
        if (pipeIndex >= 0) {
            name = name.substring(pipeIndex + 1).trim()
        }

        // Remove emojis (apenas símbolos, NÃO letras acentuadas)
        // \p{So} = Symbol, Other (emojis, símbolos matemáticos, etc.)
        name = name.replace(Regex("\\p{So}+"), "").trim()

        // Remove pipes restantes no início
        name = name.replace(Regex("^\\|\\s*"), "").trim()

        // Capitaliza primeira letra (mantém acentos originais)
        return if (name.isNotEmpty()) {
            name.replaceFirstChar { it.uppercase() }
        } else {
            rawName.trim()
        }
    }

    fun dedup(categories: List<Category>): List<Category> {
        val filtered = categories.filter { it.name.isNotBlank() }
        return filtered
            .groupBy { category ->
                // Remove diacríticos APENAS para comparação (mantém nome original com acentos)
                val normalized = Normalizer.normalize(category.name, Normalizer.Form.NFD)
                val withoutDiacritics = normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                withoutDiacritics.lowercase().trim()
            }
            .values
            .map { it.first() } // mantém o primeiro objeto original (com acentos)
            .sortedBy { it.name }
    }
}

class GetCategoriesUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(type: ContentType): Flow<List<Category>> =
        repository.getCategories(type).map { categories ->
            val normalized = categories.map { cat ->
                cat.copy(name = CategoryNormalizer.normalize(cat.name))
            }
            CategoryNormalizer.dedup(normalized)
        }
}

class GetStreamsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    operator fun invoke(categoryId: String, type: ContentType): Flow<List<Stream>> =
        repository.getStreams(categoryId, type)
}

class RefreshContentUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(type: ContentType) =
        repository.refreshCategories(type)
}

class RefreshStreamsUseCase @Inject constructor(
    private val repository: ContentRepository
) {
    suspend operator fun invoke(categoryId: String, type: ContentType) =
        repository.refreshStreams(categoryId, type)
}

class GetFavoritesUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    operator fun invoke(type: ContentType? = null) = if (type != null) {
        repository.getFavoritesByType(type)
    } else {
        repository.getAllFavorites()
    }
}

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: FavoritesRepository
) {
    suspend operator fun invoke(stream: Stream) =
        repository.toggleFavorite(stream)
}

class GetWatchHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    operator fun invoke(type: ContentType? = null, limit: Int? = null) = when {
        limit != null -> repository.getRecentHistory(limit)
        type != null -> repository.getHistoryByType(type)
        else -> repository.getAllHistory()
    }
}

class AddToHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke(stream: Stream, progress: Float = 0f) =
        repository.addToHistory(stream, progress)
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: WatchHistoryRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}
