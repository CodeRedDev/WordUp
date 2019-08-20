package de.codereddev.wordup.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SoundDao {
    @Query("DELETE FROM ${SoundDatabase.SOUND_TABLE}")
    fun nukeTable()
}
