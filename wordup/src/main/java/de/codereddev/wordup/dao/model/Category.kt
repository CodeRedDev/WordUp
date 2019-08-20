package de.codereddev.wordup.dao.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.codereddev.wordup.dao.SoundDatabase

@Entity(tableName = SoundDatabase.CATEGORY_TABLE, indices = [Index(value = ["name"], unique = true)])
class Category(
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null,
    val name: String
) {
    companion object {
        const val DEFAULT_CATEGORY = "default"
    }
}