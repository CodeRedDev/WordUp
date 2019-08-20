package de.codereddev.wordup.dao.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.codereddev.wordup.dao.SoundDatabase

@Entity(tableName = SoundDatabase.SOUND_TABLE, indices = [Index(value = ["name", "category"], unique = true)])
open class Sound(
    @PrimaryKey(autoGenerate = true)
    var uid: Int? = null,
    val name: String,
    val category: Category,
    var status: Int,
    var rootDir: String
)