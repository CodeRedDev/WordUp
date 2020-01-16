package de.codereddev.wordup.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface CategoryDao {

    /*
     * Insert methods
     */

    @Insert
    suspend fun insert(category: Category)

    /*
     * Delete methods
     */

    @Query("DELETE FROM Categories")
    suspend fun deleteAll()

    @Query("DELETE FROM Categories WHERE name = :name")
    suspend fun delete(name: String)

    @Transaction
    suspend fun deleteBatch(categoryNames: List<String>) {
        categoryNames.forEach {
            delete(it)
        }
    }

    /*
     * Getter functions
     */

    @Query("SELECT * FROM Categories")
    fun getAllCategoriesLive(): LiveData<List<Category>>

    @Query("SELECT * FROM Categories")
    fun getAllCategories(): List<Category>
}
