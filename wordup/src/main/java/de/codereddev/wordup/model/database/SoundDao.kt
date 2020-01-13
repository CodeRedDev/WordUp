package de.codereddev.wordup.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SoundDao {

    @Insert
    suspend fun insert(sound: Sound)

    @Query("DELETE FROM Sounds")
    suspend fun deleteAll()

    @Query("DELETE FROM Sounds WHERE id = :id")
    suspend fun delete(id: Int)

    @Update
    suspend fun update(sound: Sound)

    @Query("SELECT * FROM Sounds ORDER BY name ASC")
    fun getAllSounds(): LiveData<List<Sound>>

    @Query("SELECT * FROM Sounds WHERE category = :category ORDER BY name ASC")
    fun getSoundsFromCategory(category: Category): LiveData<List<Sound>>

    @Query("SELECT * FROM Sounds WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteSounds(): LiveData<List<Sound>>
}
