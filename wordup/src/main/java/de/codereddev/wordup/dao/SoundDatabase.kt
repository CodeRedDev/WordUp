package de.codereddev.wordup.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.codereddev.wordup.dao.model.Category
import de.codereddev.wordup.dao.model.FavoriteSound
import de.codereddev.wordup.dao.model.Sound

@Database(entities = [Sound::class, FavoriteSound::class, Category::class], version = 1)
abstract class SoundDatabase : RoomDatabase() {
    abstract fun soundDao(): SoundDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "soundboard.db"
        const val SOUND_TABLE = "sounds"
        const val FAVORITE_TABLE = "favorites"
        const val CATEGORY_TABLE = "categories"

        private var instance: SoundDatabase? = null

        fun getInstance(context: Context): SoundDatabase {
            if (instance == null) {
                synchronized(SoundDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SoundDatabase::class.java,
                        DATABASE_NAME
                    ).build()
                }
            }
            return instance!!
        }
    }
}