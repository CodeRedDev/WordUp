package de.codereddev.wordup.database

interface WordUpDatabase {
    fun getWordDao(): WordDao
    fun getCategoryDao(): CategoryDao
}
