package com.iptv.tv.data.local

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iptv.tv.data.local.dao.CategoryDao
import com.iptv.tv.data.local.dao.StreamDao
import com.iptv.tv.data.local.dao.FavoriteDao
import com.iptv.tv.data.local.dao.WatchHistoryDao
import com.iptv.tv.data.local.entity.CategoryEntity
import com.iptv.tv.data.local.entity.StreamEntity
import com.iptv.tv.data.local.entity.FavoriteEntity
import com.iptv.tv.data.local.entity.WatchHistoryEntity
import com.iptv.tv.domain.model.ContentType

class ContentTypeConverters {
    @TypeConverter
    fun fromContentType(type: ContentType): String = type.name

    @TypeConverter
    fun toContentType(value: String): ContentType = ContentType.valueOf(value)
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS favorites (
                streamId TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                categoryId TEXT NOT NULL,
                type TEXT NOT NULL,
                posterUrl TEXT,
                streamUrl TEXT NOT NULL,
                addedAt INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS watch_history (
                streamId TEXT PRIMARY KEY NOT NULL,
                name TEXT NOT NULL,
                categoryId TEXT NOT NULL,
                type TEXT NOT NULL,
                posterUrl TEXT,
                streamUrl TEXT NOT NULL,
                lastWatchedAt INTEGER NOT NULL,
                progress REAL NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE watch_history ADD COLUMN lastEpisodeNum INTEGER")
        db.execSQL("ALTER TABLE watch_history ADD COLUMN lastEpisodeTitle TEXT")
        db.execSQL("ALTER TABLE watch_history ADD COLUMN lastSeason TEXT")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE watch_history ADD COLUMN lastEpisodeUrl TEXT")
    }
}

@Database(
    entities = [
        CategoryEntity::class,
        StreamEntity::class,
        FavoriteEntity::class,
        WatchHistoryEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(ContentTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun streamDao(): StreamDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao

    suspend fun logWatchHistorySchema() {
        val db = openHelper.readableDatabase
        Log.d("DBDebug", "Database version: ${db.version}")
        val cursor = db.query("PRAGMA table_info(watch_history)")
        val columns = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val nameIndex = it.getColumnIndex("name")
                if (nameIndex >= 0) columns.add(it.getString(nameIndex))
            }
        }
        Log.d("DBDebug", "Colunas reais: $columns")
    }
}
