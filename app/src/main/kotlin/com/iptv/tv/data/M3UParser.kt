package com.iptv.tv.data

import android.content.Context
import com.iptv.tv.domain.model.Category
import com.iptv.tv.domain.model.ContentType
import com.iptv.tv.domain.model.Stream
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class M3UParser @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class M3UEntry(
        val name: String,
        val streamUrl: String,
        val tvgName: String?,
        val tvgLogo: String?,
        val groupTitle: String?,
        val isExternal: Boolean = false,
        val episodeNum: Int? = null,
        val seasonNum: Int? = null,
        val duration: Int? = null
    )

    data class ParseResult(
        val categories: List<Category>,
        val streams: List<Stream>
    )

    suspend fun parseFromUrl(url: String): ParseResult = withContext(Dispatchers.IO) {
        android.util.Log.d("M3UParser", "parseFromUrl: received url='$url'")
        val content = fetchUrl(url)
        android.util.Log.d("M3UParser", "parseFromUrl: fetched ${content.length} chars")
        parseContent(content)
    }

    suspend fun parseFromFile(path: String): ParseResult = withContext(Dispatchers.IO) {
        android.util.Log.d("M3UParser", "parseFromFile: path='$path'")
        val file = File(path)
        val content = file.readText()
        parseContent(content)
    }

    private suspend fun fetchUrl(url: String): String {
        return withContext(Dispatchers.IO) {
            android.util.Log.w("M3UParser", "fetchUrl: START url='$url'")
            try {
                val connection = java.net.URL(url).openConnection()
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android TV) AppleWebKit/537.36")
                val inputStream = connection.getInputStream()
                val content = inputStream.bufferedReader().readText()
                android.util.Log.w("M3UParser", "fetchUrl: SUCCESS got ${content.length} chars, first 100: ${content.take(100)}")
                content
            } catch (e: Exception) {
                android.util.Log.e("M3UParser", "fetchUrl: FAILED ${e.javaClass.simpleName}: ${e.message}")
                throw e
            }
        }
    }

    private fun parseContent(content: String): ParseResult {
        val lines = content.lines()
        val entries = mutableListOf<M3UEntry>()

        var currentTvgName: String? = null
        var currentTvgLogo: String? = null
        var currentGroupTitle: String? = null
        var currentDuration: Int? = null
        var currentTitle: String? = null
        var currentEpisodeNum: Int? = null
        var currentSeasonNum: Int? = null

        for (line in lines) {
            val trimmed = line.trim()
            when {
                trimmed.startsWith("#EXTINF:") -> {
                    val attrs = parseExtInf(trimmed)
                    currentDuration = attrs["duration"] as? Int
                    currentTvgName = attrs["tvg-name"] as? String
                    currentTvgLogo = attrs["tvg-logo"] as? String
                    currentGroupTitle = attrs["group-title"] as? String
                    currentTitle = attrs["title"] as? String
                    currentEpisodeNum = attrs["episode-num"] as? Int
                    currentSeasonNum = attrs["season-num"] as? Int
                }
                trimmed.startsWith("#EXTEXTVLCOPT:") -> {
                }
                trimmed.isNotEmpty() && !trimmed.startsWith("#") -> {
                    val name = currentTvgName
                        ?: currentTitle
                        ?: extractNameFromUrl(trimmed)
                        ?: "Canal Desconhecido"
                    entries.add(
                        M3UEntry(
                            name = name,
                            streamUrl = trimmed,
                            tvgName = currentTvgName,
                            tvgLogo = currentTvgLogo,
                            groupTitle = currentGroupTitle,
                            duration = currentDuration,
                            episodeNum = currentEpisodeNum,
                            seasonNum = currentSeasonNum
                        )
                    )
                    currentTvgName = null
                    currentTvgLogo = null
                    currentGroupTitle = null
                    currentDuration = null
                    currentTitle = null
                    currentEpisodeNum = null
                    currentSeasonNum = null
                }
            }
        }

        return buildResult(entries)
    }

    private fun parseExtInf(line: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        val mainPart = line.removePrefix("#EXTINF:").trim()

        val commaIdx = mainPart.lastIndexOf(',')
        if (commaIdx > 0) {
            val durationStr = mainPart.substring(0, commaIdx).trim()
            result["duration"] = durationStr.toIntOrNull() ?: -1

            val attrsStr = mainPart.substring(commaIdx + 1).trim()
            result["title"] = attrsStr

            val attrPattern = Regex("""([\w-]+)="([^"]*)"""")
            attrPattern.findAll(mainPart.substring(0, commaIdx)).forEach { match ->
                val key = match.groupValues[1]
                val value = match.groupValues[2]
                when (key) {
                    "tvg-name" -> result["tvg-name"] = value
                    "tvg-logo" -> result["tvg-logo"] = value
                    "group-title" -> result["group-title"] = value
                    "tvg-id" -> result["tvg-id"] = value
                    "episode-num" -> result["episode-num"] = value.toIntOrNull() as Any
                    "season-num" -> result["season-num"] = value.toIntOrNull() as Any
                }
            }
        }

        return result
    }

    private fun extractNameFromUrl(url: String): String? {
        return try {
            val path = java.net.URL(url).path
            val fileName = path.substringAfterLast('/').substringBeforeLast('.')
            if (fileName.isNotEmpty()) fileName else null
        } catch (e: Exception) {
            null
        }
    }

    private fun buildResult(entries: List<M3UEntry>): ParseResult {
        val categoryMap = mutableMapOf<String, MutableList<M3UEntry>>()

        for (entry in entries) {
            val catName = entry.groupTitle ?: "Sem Categoria"
            categoryMap.getOrPut(catName) { mutableListOf() }.add(entry)
        }

        val categories = categoryMap.map { (name, catEntries) ->
            Category(
                id = UUID.nameUUIDFromBytes(name.toByteArray()).toString(),
                name = name,
                type = ContentType.LIVE,
                streamCount = catEntries.size
            )
        }.sortedBy { it.name }

        val streams = entries.mapIndexed { idx, entry ->
            val categoryName = entry.groupTitle ?: "Sem Categoria"
            Stream(
                id = UUID.nameUUIDFromBytes(entry.streamUrl.toByteArray()).toString(),
                name = entry.name,
                categoryId = UUID.nameUUIDFromBytes(categoryName.toByteArray()).toString(),
                type = ContentType.LIVE,
                streamUrl = entry.streamUrl,
                posterUrl = entry.tvgLogo,
                containerExtension = "m3u8"
            )
        }

        return ParseResult(categories, streams)
    }
}