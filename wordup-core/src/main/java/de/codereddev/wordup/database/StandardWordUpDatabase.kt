package de.codereddev.wordup.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Word::class, Category::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class StandardWordUpDatabase : RoomDatabase(), WordUpDatabase {

    companion object {
        @Volatile
        private var INSTANCE: StandardWordUpDatabase? = null

        fun getInstance(context: Context): StandardWordUpDatabase {
            val tempInstance =
                INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StandardWordUpDatabase::class.java,
                    "wordup_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
