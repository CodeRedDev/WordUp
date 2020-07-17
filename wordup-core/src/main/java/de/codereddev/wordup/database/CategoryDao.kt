package de.codereddev.wordup.database

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
    fun insert(category: Category)

    /*
     * Delete methods
     */

    @Query("DELETE FROM Categories")
    fun deleteAll()

    @Query("DELETE FROM Categories WHERE name = :name")
    fun delete(name: String)

    @Transaction
    fun deleteBatch(categoryNames: List<String>) {
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
