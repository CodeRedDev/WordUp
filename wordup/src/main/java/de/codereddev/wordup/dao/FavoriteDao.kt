package de.codereddev.wordup.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface FavoriteDao {
    @Query("DELETE FROM ${SoundDatabase.FAVORITE_TABLE}")
    fun nukeTable()
}
