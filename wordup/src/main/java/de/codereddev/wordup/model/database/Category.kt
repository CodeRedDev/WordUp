package de.codereddev.wordup.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Categories")
data class Category(
    @PrimaryKey
    val name: String
)
