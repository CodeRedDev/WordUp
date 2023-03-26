package de.codereddev.wordup.util

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.database.Category
import de.codereddev.wordup.database.CategoryDao
import de.codereddev.wordup.database.StandardWordUpDatabase
import de.codereddev.wordup.database.Word
import de.codereddev.wordup.database.WordDao
import java.io.IOException
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDbInitializerTest {

    private lateinit var db: StandardWordUpDatabase
    private lateinit var wordDao: WordDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, StandardWordUpDatabase::class.java).build()
        wordDao = db.getWordDao()
        categoryDao = db.getCategoryDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testCategoryCascading() = runBlocking {
        val config = WordUpConfig().apply {
            categoriesEnabled = true
        }
        val categoryList = mutableListOf("Cat 1", "Cat 2", "Cat 3")
        val wordsPerCategory = mutableMapOf<String, List<String>>().apply {
            put(categoryList[0], mutableListOf("Word 1.mp3"))
            put(categoryList[1], mutableListOf("Word 2.mp3"))
            put(categoryList[2], mutableListOf("Word 3.mp3"))
        }

        initializeWithCategory(config, categoryList, wordsPerCategory)

        assertEquals(wordDao.getAllWords().size, 3)
        assertEquals(categoryDao.getAllCategories().size, 3)

        categoryList.removeAt(1)

        initializeWithCategory(config, categoryList, wordsPerCategory)

        assertEquals(wordDao.getAllWords().size, 2)
        assertEquals(categoryDao.getAllCategories().size, 2)
        assertFalse(wordDao.getAllWords().map { it.name }.contains("Word 2"))
    }

    @Test
    fun testCategoryWordRename() = runBlocking {
        val config = WordUpConfig().apply {
            categoriesEnabled = true
        }
        val categoryList = mutableListOf("Cat 1", "Cat 2", "Cat 3")
        val wordsPerCategory = mutableMapOf<String, List<String>>().apply {
            put(categoryList[0], mutableListOf("Word 1.mp3"))
            put(categoryList[1], mutableListOf("Word 2.mp3"))
            put(categoryList[2], mutableListOf("Word 3.mp3"))
        }

        initializeWithCategory(config, categoryList, wordsPerCategory)

        wordsPerCategory.replace(categoryList[1], mutableListOf("Word 2-1.mp3"))

        initializeWithCategory(config, categoryList, wordsPerCategory)

        val words = wordDao.getAllWords().map { it.name }
        assertEquals(words.size, 3)
        assertTrue(words.contains("Word 2-1"))
        assertFalse(words.contains("Word 2"))
    }

    @Test
    fun testWordRename() = runBlocking {
        val config = WordUpConfig().apply {
            categoriesEnabled = true
        }
        val wordList = mutableListOf("Word 1.mp3", "Word 2.mp3", "Word 3.mp3")

        initializeWithoutCategory(config, wordList)

        wordList.removeAt(1)
        wordList.add("Word 2-1.mp3")

        initializeWithoutCategory(config, wordList)

        val words = wordDao.getAllWords().map { it.name }
        assertEquals(words.size, 3)
        assertTrue(words.contains("Word 2-1"))
        assertFalse(words.contains("Word 2"))
    }

    private suspend fun initializeWithoutCategory(config: WordUpConfig, words: List<String>) =
        withContext(Dispatchers.IO) {
            val curWordNames = wordDao.getAllWords().map { it.name }

            val fileList = words.map { it.replace(".mp3", "") }

            curWordNames.subtract(fileList).toList().let {
                if (it.isNotEmpty()) {
                    wordDao.deleteBatch(it)
                }
            }

            val wordList = mutableListOf<Word>()
            fileList.subtract(curWordNames).forEach { new ->
                wordList.add(
                    Word(
                        name = new,
                        path = "$PATH_WORDUP/$new.mp3",
                        isNew = config.newWordsEnabled
                    )
                )
            }
            if (wordList.isNotEmpty()) {
                wordDao.insertBatch(wordList)
            }
        }

    private suspend fun initializeWithCategory(
        config: WordUpConfig,
        categoryList: List<String>,
        wordsPerCategory: Map<String, List<String>>
    ) = withContext(Dispatchers.IO) {
        val curCategories = categoryDao.getAllCategories().map { it.name }
        val dirList = categoryList

        /*
         * Category was deleted or renamed.
         * All words that have a reference to this category are not valid anymore
         * and will be deleted in a cascade by the database.
         */
        curCategories.subtract(dirList).toList().let {
            if (it.isNotEmpty()) {
                categoryDao.deleteBatch(it)
            }
        }

        val processedDirs = mutableListOf<String>()

        /*
         * Category was added.
         * All words of this category are new.
         */
        dirList.subtract(curCategories).forEach { newCategory ->
            processedDirs.add(newCategory)
            val category = Category(newCategory)
            categoryDao.insert(category)

            val fileList = wordsPerCategory[newCategory]!!
                .map { it.replace(".mp3", "") }

            val wordList = mutableListOf<Word>()
            fileList.forEach { name ->
                wordList.add(
                    Word(
                        name = name,
                        path = "$PATH_WORDUP/$newCategory/$name.mp3",
                        isNew = config.newWordsEnabled,
                        category = category
                    )
                )
            }
            if (wordList.isNotEmpty()) {
                wordDao.insertBatch(wordList)
            }
        }

        /*
         * Checking old categories
         * Words can be deleted, renamed or added.
         * Renamed words are equal to added words.
         */
        dirList.subtract(processedDirs).forEach { oldCategory ->
            val category = Category(oldCategory)
            val curWords = wordDao.getWordsFromCategory(category).map { it.name }

            val fileList = wordsPerCategory[oldCategory]!!
                .map { it.replace(".mp3", "") }

            // Deleted words
            curWords.subtract(fileList).toList().let {
                if (it.isNotEmpty()) {
                    wordDao.deleteBatchByCategory(it, category)
                }
            }

            // New words
            val wordList = mutableListOf<Word>()
            fileList.subtract(curWords).forEach { newWord ->
                wordList.add(
                    Word(
                        name = newWord,
                        path = "$PATH_WORDUP/$oldCategory/$newWord.mp3",
                        isNew = config.newWordsEnabled,
                        category = category
                    )
                )
            }
            if (wordList.isNotEmpty()) {
                wordDao.insertBatch(wordList)
            }
        }
    }

    companion object {
        private const val PATH_WORDUP = "wordup"
    }
}
