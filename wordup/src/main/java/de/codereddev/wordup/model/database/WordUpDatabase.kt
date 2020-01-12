package de.codereddev.wordup.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sound::class, Category::class], version = 1)
abstract class WordUpDatabase : RoomDatabase() {

    abstract fun soundDao(): SoundDao
    abstract fun categoryDao(): Category

    companion object {
        @Volatile
        private var INSTANCE: WordUpDatabase? = null

        fun getInstance(context: Context): WordUpDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WordUpDatabase::class.java,
                    "wordup_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
