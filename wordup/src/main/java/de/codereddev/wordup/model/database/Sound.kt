package de.codereddev.wordup.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Sounds")
data class Sound(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    var path: String,
    var isNetworkResource: Boolean = false,
    var isFavorite: Boolean = false,
    var category: Category? = null
)
