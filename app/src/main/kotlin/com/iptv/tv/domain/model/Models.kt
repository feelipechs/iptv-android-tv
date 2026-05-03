package com.iptv.tv.domain.model

// Tipo de conteúdo suportado
enum class ContentType { LIVE, VOD, SERIES }

// Tipo de provedor
enum class ProviderType { XTREAM, M3U_LIST }

data class Category(
    val id: String,
    val name: String,          // já normalizado (sem emojis, sem prefixo)
    val type: ContentType,
    val streamCount: Int = 0,
    val isPinned: Boolean = false
)

data class Stream(
    val id: String,
    val name: String,
    val categoryId: String,
    val type: ContentType,
    val streamUrl: String,
    val posterUrl: String?,
    val epgChannelId: String? = null,
    val containerExtension: String = "m3u8",
    val isFavorite: Boolean = false,
    val progress: Float = 0f
)

data class WatchHistoryEntry(
    val streamId: String,
    val name: String,
    val categoryId: String,
    val type: ContentType,
    val posterUrl: String?,
    val streamUrl: String,
    val lastWatchedAt: Long,
    val progress: Float = 0f
)

data class FavoriteEntry(
    val streamId: String,
    val name: String,
    val categoryId: String,
    val type: ContentType,
    val posterUrl: String?,
    val streamUrl: String,
    val addedAt: Long
)

data class Credentials(
    val server: String,        // ex: http://host:port
    val username: String,
    val password: String,
    val providerType: ProviderType = ProviderType.XTREAM,
    val m3uSource: String? = null  // URL ou caminho do arquivo M3U (para M3U_LIST)
) {
    /** URL base da API */
    val apiBase: String get() = "$server/player_api.php?username=$username&password=$password"

    /** URL de stream live */
    fun liveUrl(streamId: String) = "$server/live/$username/$password/$streamId.m3u8"

    /** URL de stream VOD - ext pode ser nulo, usa mp4 como padrão */
    fun vodUrl(streamId: String, ext: String?): String {
        val extension = ext ?: "mp4"
        return "$server/movie/$username/$password/$streamId.$extension"
    }

    /** URL de stream Series */
    fun seriesUrl(seriesId: String, ext: String?): String {
        val extension = ext ?: "mp4"
        return "$server/series/$username/$password/$seriesId.$extension"
    }
}
