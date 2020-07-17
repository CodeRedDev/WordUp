package de.codereddev.wordup.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface WordDao {

    /*
     * Insert methods
     */

    @Insert
    fun insert(word: Word)

    @Insert
    fun insertBatch(words: List<Word>)

    /*
     * Delete methods
     */

    @Query("DELETE FROM Words")
    fun deleteAll()

    @Query("DELETE FROM Words WHERE id = :id")
    fun delete(id: Int)

    @Query("DELETE FROM Words WHERE name = :name")
    fun deleteByName(name: String)

    @Query("DELETE FROM Words WHERE category = :category")
    fun deleteByCategory(category: Category)

    @Query("DELETE FROM Words WHERE name = :name AND category = :category")
    fun deleteByNameAndCategory(name: String, category: Category)

    @Transaction
    fun deleteBatch(wordNames: List<String>) {
        wordNames.forEach {
            deleteByName(it)
        }
    }

    @Transaction
    fun deleteBatchByCategory(wordNames: List<String>, category: Category) {
        wordNames.forEach {
            deleteByNameAndCategory(it, category)
        }
    }

    /*
     * Update methods
     */

    @Update
    fun update(word: Word)

    /*
     * Getter functions
     */

    @Query("SELECT * FROM Words WHERE id = :id")
    fun getWordById(id: Int): Word

    @Query("SELECT * FROM Words ORDER BY name ASC")
    fun getAllWordsLive(): LiveData<List<Word>>

    @Query("SELECT * FROM Words ORDER BY name ASC")
    fun getAllWords(): List<Word>

    @Query("SELECT * FROM Words WHERE category = :category ORDER BY name ASC")
    fun getWordsFromCategoryLive(category: Category): LiveData<List<Word>>

    @Query("SELECT * FROM Words WHERE category = :category ORDER BY name ASC")
    fun getWordsFromCategory(category: Category): List<Word>

    @Query("SELECT * FROM Words WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteWords(): LiveData<List<Word>>
}
