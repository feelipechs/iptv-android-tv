package com.iptv.tv.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iptv.tv.domain.model.ContentType

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: ContentType,
    val streamCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "streams")
data class StreamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String,
    val type: ContentType,
    val streamUrl: String,
    val posterUrl: String?,
    val epgChannelId: String?,
    val containerExtension: String = "m3u8",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val streamId: String,
    val name: String,
    val categoryId: String,
    val type: ContentType,
    val posterUrl: String?,
    val streamUrl: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val streamId: String,
    val name: String,
    val categoryId: String,
    val type: ContentType,
    val posterUrl: String?,
    val streamUrl: String,
    val lastWatchedAt: Long = System.currentTimeMillis(),
    val progress: Float = 0f
)
