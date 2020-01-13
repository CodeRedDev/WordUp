package de.codereddev.wordup.model.database

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun categoryFromString(value: String?): Category? {
        return value?.let { Category(value) }
    }

    @TypeConverter
    fun categoryToString(category: Category?): String? {
        return category?.name
    }
}
