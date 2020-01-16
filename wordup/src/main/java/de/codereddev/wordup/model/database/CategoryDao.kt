package de.codereddev.wordup.model.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(category: Category)

    @Query("DELETE FROM Categories")
    suspend fun deleteAll()

    @Query("DELETE FROM Categories WHERE name = :name")
    suspend fun delete(name: String)

    @Query("SELECT * FROM Categories")
    fun getAllCategories(): LiveData<List<Category>>
}
