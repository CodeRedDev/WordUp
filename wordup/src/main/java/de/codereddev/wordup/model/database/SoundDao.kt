package de.codereddev.wordup.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface SoundDao {

    /*
     * Insert methods
     */

    @Insert
    suspend fun insert(sound: Sound)

    @Insert
    suspend fun insertBatch(sounds: List<Sound>)

    /*
     * Delete methods
     */

    @Query("DELETE FROM Sounds")
    suspend fun deleteAll()

    @Query("DELETE FROM Sounds WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM Sounds WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM Sounds WHERE category = :category")
    suspend fun deleteByCategory(category: Category)

    @Query("DELETE FROM Sounds WHERE name = :name AND category = :category")
    suspend fun deleteByNameAndCategory(name: String, category: Category)

    @Transaction
    suspend fun deleteBatch(soundNames: List<String>) {
        soundNames.forEach {
            deleteByName(it)
        }
    }

    @Transaction
    suspend fun deleteBatchByCategory(soundNames: List<String>, category: Category) {
        soundNames.forEach {
            deleteByNameAndCategory(it, category)
        }
    }

    /*
     * Update methods
     */

    @Update
    suspend fun update(sound: Sound)

    /*
     * Getter functions
     */

    @Query("SELECT * FROM Sounds WHERE id = :id")
    fun getSoundById(id: Int): Sound

    @Query("SELECT * FROM Sounds ORDER BY name ASC")
    fun getAllSoundsLive(): LiveData<List<Sound>>

    @Query("SELECT * FROM Sounds ORDER BY name ASC")
    fun getAllSounds(): List<Sound>

    @Query("SELECT * FROM Sounds WHERE category = :category ORDER BY name ASC")
    fun getSoundsFromCategoryLive(category: Category): LiveData<List<Sound>>

    @Query("SELECT * FROM Sounds WHERE category = :category ORDER BY name ASC")
    fun getSoundsFromCategory(category: Category): List<Sound>

    @Query("SELECT * FROM Sounds WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteSounds(): LiveData<List<Sound>>
}
