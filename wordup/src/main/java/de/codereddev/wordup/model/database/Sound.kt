package de.codereddev.wordup.model.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "Sounds",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["name"],
        childColumns = ["category"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["category"])]
)
data class Sound(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var name: String,
    var path: String,
    var isNetworkResource: Boolean = false,
    var isFavorite: Boolean = false,
    var isNew: Boolean = false,
    var category: Category? = null
)
