package de.codereddev.wordup.util

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.model.database.Category
import de.codereddev.wordup.model.database.CategoryDao
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordup.model.database.SoundDao
import de.codereddev.wordup.model.database.WordUpDatabase
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
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class LocalDbInitializerTest {

    private lateinit var db: WordUpDatabase
    private lateinit var soundDao: SoundDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WordUpDatabase::class.java).build()
        soundDao = db.soundDao()
        categoryDao = db.categoryDao()
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
        val soundsPerCategory = mutableMapOf<String, List<String>>().apply {
            put(categoryList[0], mutableListOf("Sound 1.mp3"))
            put(categoryList[1], mutableListOf("Sound 2.mp3"))
            put(categoryList[2], mutableListOf("Sound 3.mp3"))
        }

        initializeWithCategory(config, categoryList, soundsPerCategory)

        assertEquals(soundDao.getAllSounds().size, 3)
        assertEquals(categoryDao.getAllCategories().size, 3)

        categoryList.removeAt(1)

        initializeWithCategory(config, categoryList, soundsPerCategory)

        assertEquals(soundDao.getAllSounds().size, 2)
        assertEquals(categoryDao.getAllCategories().size, 2)
        assertFalse(soundDao.getAllSounds().map { it.name }.contains("Sound 2"))
    }

    @Test
    fun testCategorySoundRename() = runBlocking {
        val config = WordUpConfig().apply {
            categoriesEnabled = true
        }
        val categoryList = mutableListOf("Cat 1", "Cat 2", "Cat 3")
        val soundsPerCategory = mutableMapOf<String, List<String>>().apply {
            put(categoryList[0], mutableListOf("Sound 1.mp3"))
            put(categoryList[1], mutableListOf("Sound 2.mp3"))
            put(categoryList[2], mutableListOf("Sound 3.mp3"))
        }

        initializeWithCategory(config, categoryList, soundsPerCategory)

        soundsPerCategory.replace(categoryList[1], mutableListOf("Sound 2-1.mp3"))

        initializeWithCategory(config, categoryList, soundsPerCategory)

        val sounds = soundDao.getAllSounds().map { it.name }
        assertEquals(sounds.size, 3)
        assertTrue(sounds.contains("Sound 2-1"))
        assertFalse(sounds.contains("Sound 2"))
    }

    @Test
    fun testSoundRename() = runBlocking {
        val config = WordUpConfig().apply {
            categoriesEnabled = true
        }
        val soundList = mutableListOf("Sound 1.mp3", "Sound 2.mp3", "Sound 3.mp3")

        initializeWithoutCategory(config, soundList)

        soundList.removeAt(1)
        soundList.add("Sound 2-1.mp3")

        initializeWithoutCategory(config, soundList)

        val sounds = soundDao.getAllSounds().map { it.name }
        assertEquals(sounds.size, 3)
        assertTrue(sounds.contains("Sound 2-1"))
        assertFalse(sounds.contains("Sound 2"))
    }

    private suspend fun initializeWithoutCategory(config: WordUpConfig, sounds: List<String>) =
        withContext(Dispatchers.IO) {
            val curSoundNames = soundDao.getAllSounds().map { it.name }

            val fileList = sounds.map { it.replace(".mp3", "") }

            curSoundNames.subtract(fileList).toList().let {
                if (it.isNotEmpty()) {
                    soundDao.deleteBatch(it)
                }
            }

            val soundList = mutableListOf<Sound>()
            fileList.subtract(curSoundNames).forEach { new ->
                soundList.add(
                    Sound(
                        name = new,
                        path = "$PATH_WORDUP/$new.mp3",
                        isNew = config.newSoundsEnabled
                    )
                )
            }
            if (soundList.isNotEmpty()) {
                soundDao.insertBatch(soundList)
            }
        }

    private suspend fun initializeWithCategory(
        config: WordUpConfig,
        categoryList: List<String>,
        soundsPerCategory: Map<String, List<String>>
    ) = withContext(Dispatchers.IO) {
        val curCategories = categoryDao.getAllCategories().map { it.name }
        val dirList = categoryList

        /*
         * Category was deleted or renamed.
         * All sounds that have a reference to this category are not valid anymore
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
         * All sounds of this category are new.
         */
        dirList.subtract(curCategories).forEach { newCategory ->
            processedDirs.add(newCategory)
            val category = Category(newCategory)
            categoryDao.insert(category)

            val fileList = soundsPerCategory[newCategory]!!
                .map { it.replace(".mp3", "") }

            val soundList = mutableListOf<Sound>()
            fileList.forEach { name ->
                soundList.add(
                    Sound(
                        name = name,
                        path = "$PATH_WORDUP/$newCategory/$name.mp3",
                        isNew = config.newSoundsEnabled,
                        category = category
                    )
                )
            }
            if (soundList.isNotEmpty()) {
                soundDao.insertBatch(soundList)
            }
        }

        /*
         * Checking old categories
         * Sounds can be deleted, renamed or added.
         * Renamed sounds are equal to added sounds.
         */
        dirList.subtract(processedDirs).forEach { oldCategory ->
            val category = Category(oldCategory)
            val curSounds = soundDao.getSoundsFromCategory(category).map { it.name }

            val fileList = soundsPerCategory[oldCategory]!!
                .map { it.replace(".mp3", "") }

            // Deleted sounds
            curSounds.subtract(fileList).toList().let {
                if (it.isNotEmpty()) {
                    soundDao.deleteBatchByCategory(it, category)
                }
            }

            // New sounds
            val soundList = mutableListOf<Sound>()
            fileList.subtract(curSounds).forEach { newSound ->
                soundList.add(
                    Sound(
                        name = newSound,
                        path = "$PATH_WORDUP/$oldCategory/$newSound.mp3",
                        isNew = config.newSoundsEnabled,
                        category = category
                    )
                )
            }
            if (soundList.isNotEmpty()) {
                soundDao.insertBatch(soundList)
            }
        }
    }

    companion object {
        private const val PATH_WORDUP = "wordup"
    }
}
