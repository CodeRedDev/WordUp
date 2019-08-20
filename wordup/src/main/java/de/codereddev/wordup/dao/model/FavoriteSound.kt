package de.codereddev.wordup.dao.model

import androidx.room.Entity
import androidx.room.Index
import de.codereddev.wordup.dao.SoundDatabase

@Entity(tableName = SoundDatabase.FAVORITE_TABLE, indices = [Index(value = ["name", "category"], unique = true)])
class FavoriteSound(
    uid: Int? = null,
    name: String,
    category: Category,
    status: Int,
    rootDir: String
) : Sound(uid, name, category, status, rootDir)