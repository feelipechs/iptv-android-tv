package com.iptv.tv.data.local.dao

import androidx.room.*
import com.iptv.tv.data.local.entity.CategoryEntity
import com.iptv.tv.data.local.entity.StreamEntity
import com.iptv.tv.data.local.entity.FavoriteEntity
import com.iptv.tv.data.local.entity.WatchHistoryEntity
import com.iptv.tv.domain.model.ContentType
import kotlinx.coroutines.flow.Flow

data class CategoryCount(
    val categoryId: String,
    val count: Int
)

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategories(type: ContentType): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE type = :type")
    suspend fun deleteByType(type: ContentType)

    @Transaction
    suspend fun replaceAll(type: ContentType, categories: List<CategoryEntity>) {
        deleteByType(type)
        insertAll(categories)
    }
}

@Dao
interface StreamDao {

    @Query("SELECT * FROM streams WHERE categoryId = :categoryId AND type = :type ORDER BY name ASC")
    fun getStreamsByCategory(categoryId: String, type: ContentType): Flow<List<StreamEntity>>

    @Query("SELECT * FROM streams WHERE type = :type ORDER BY name ASC")
    fun getStreamsByType(type: ContentType): Flow<List<StreamEntity>>

    @Query("SELECT categoryId, COUNT(*) as count FROM streams WHERE type = :type GROUP BY categoryId")
    fun getStreamCountsByType(type: ContentType): Flow<List<CategoryCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(streams: List<StreamEntity>)

    @Query("DELETE FROM streams WHERE categoryId = :categoryId AND type = :type")
    suspend fun deleteByCategory(categoryId: String, type: ContentType)

    @Transaction
    suspend fun replaceByCategory(categoryId: String, type: ContentType, streams: List<StreamEntity>) {
        deleteByCategory(categoryId, type)
        insertAll(streams)
    }
}

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE type = :type ORDER BY addedAt DESC")
    fun getFavoritesByType(type: ContentType): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE streamId = :streamId)")
    fun isFavorite(streamId: String): Flow<Boolean>

    @Query("SELECT COUNT(*) FROM favorites WHERE type = :type")
    fun getFavoriteCountByType(type: ContentType): Flow<Int>

    @Query("SELECT COUNT(*) FROM favorites")
    fun getTotalFavoriteCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE streamId = :streamId")
    suspend fun delete(streamId: String)

    @Query("SELECT COUNT(*) FROM favorites WHERE streamId = :streamId")
    suspend fun getFavoriteCountSync(streamId: String): Int

    @Transaction
    suspend fun toggle(streamId: String, name: String, categoryId: String, type: ContentType, posterUrl: String?, streamUrl: String) {
        val exists = getFavoriteCountSync(streamId)
        if (exists > 0) {
            delete(streamId)
        } else {
            insert(FavoriteEntity(streamId, name, categoryId, type, posterUrl, streamUrl))
        }
    }
}

@Dao
interface WatchHistoryDao {

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE type = :type ORDER BY lastWatchedAt DESC")
    fun getHistoryByType(type: ContentType): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY lastWatchedAt DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): Flow<List<WatchHistoryEntity>>

    @Query("SELECT COUNT(*) FROM watch_history")
    fun getHistoryCount(): Flow<Int>

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId LIMIT 1")
    suspend fun getById(streamId: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE streamId = :streamId LIMIT 1")
    fun observeById(streamId: String): Flow<WatchHistoryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE streamId = :streamId")
    suspend fun delete(streamId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()

    @Query("UPDATE watch_history SET lastWatchedAt = :timestamp, progress = :progress WHERE streamId = :streamId")
    suspend fun updateProgress(streamId: String, progress: Float, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun addToHistory(
        streamId: String,
        name: String,
        categoryId: String,
        type: ContentType,
        posterUrl: String?,
        streamUrl: String,
        progress: Float = 0f,
        lastEpisodeNum: Int? = null,
        lastEpisodeTitle: String? = null,
        lastSeason: String? = null,
        lastEpisodeUrl: String? = null
    ) {
        val existing = getById(streamId)
        val entity = WatchHistoryEntity(
            streamId = streamId,
            name = name,
            categoryId = categoryId,
            type = type,
            posterUrl = posterUrl,
            streamUrl = streamUrl,
            lastWatchedAt = System.currentTimeMillis(),
            progress = progress,
            lastEpisodeNum = lastEpisodeNum ?: existing?.lastEpisodeNum,
            lastEpisodeTitle = lastEpisodeTitle ?: existing?.lastEpisodeTitle,
            lastSeason = lastSeason ?: existing?.lastSeason,
            lastEpisodeUrl = lastEpisodeUrl ?: existing?.lastEpisodeUrl
        )
        insert(entity)
    }
}