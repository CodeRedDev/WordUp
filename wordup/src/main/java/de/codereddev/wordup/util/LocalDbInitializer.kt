package de.codereddev.wordup.util

import android.content.Context
import de.codereddev.wordup.ErrorConstants
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.model.database.Category
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordup.model.database.WordUpDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This helps to initialize the [WordUpDatabase] with locally stored sounds from assets.
 * This will also update the database based on the files
 * you delete or add to the assets/wordup folder.
 *
 * The initializing process uses the [WordUpConfig] given to [WordUp.init].
 * If non is given it will throw an [IllegalStateException].
 *
 * If [WordUpConfig.categoriesEnabled] is false sounds should be
 * organized matching the following path:
 * assets/wordup/sound.mp3
 *
 * Renamed sounds will be processed as new sounds i.e. the old entry will
 * be deleted and the sound will be re-added with the new name.
 *
 * If [WordUpConfig.categoriesEnabled] is true sounds should instead be
 * organized matching the following path:
 * assets/wordup/category-subfolder/sound.mp3
 *
 * Renamed categories will be processed as new categories i.e the old category
 * and all sounds referencing this category will be deleted and the category
 * plus all its sounds will be re-added with the new name.
 *
 * Sounds in categories that were not edited will behave like non-category sounds.
 *
 * If [WordUpConfig.newSoundsEnabled] is true newly added sounds will be marked as such.
 */
class LocalDbInitializer(database: WordUpDatabase) {
    private val soundDao = database.soundDao()
    private val categoryDao = database.categoryDao()

    /**
     * Initialize the database from locally stored asset files.
     *
     * This should be called from a coroutine as it does a lot of IO work.
     */
    suspend fun initialize(context: Context) = withContext(Dispatchers.IO) {
        if (!WordUp.isConfigInitialized())
            throw IllegalStateException(ErrorConstants.CONFIG_NOT_DEFINED)
        val config = WordUp.config

        if (config.categoriesEnabled) {
            initializeWithCategory(context.applicationContext, config)
        } else {
            initializeWithoutCategory(context.applicationContext, config)
        }
    }

    private suspend fun initializeWithoutCategory(context: Context, config: WordUpConfig) =
        withContext(Dispatchers.IO) {
            val curSoundNames = soundDao.getAllSounds().map { it.name }
            val assetList = context.assets.list(PATH_WORDUP)!!
            if (assetList.any { !it.endsWith(".mp3") })
                throw IllegalArgumentException(ErrorConstants.INITIALIZER_NO_CATEGORY_SUBFOLDER)

            val fileList = assetList.map { it.replace(".mp3", "") }

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

    private suspend fun initializeWithCategory(context: Context, config: WordUpConfig) =
        withContext(Dispatchers.IO) {
            val curCategories = categoryDao.getAllCategories().map { it.name }

            val assetList = context.assets.list(PATH_WORDUP)
            if (assetList == null || assetList.any { it.endsWith(".mp3") })
                throw IllegalArgumentException(ErrorConstants.INITIALIZER_CATEGORY_ROOT_MP3)

            val dirList = assetList.toList()

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
                val assetFileList = context.assets.list("$PATH_WORDUP/$newCategory")!!
                if (assetFileList.any { !it.endsWith(".mp3") })
                    throw IllegalArgumentException(ErrorConstants.INITIALIZER_CATEGORY_SUBFOLDER)

                val fileList = assetFileList.map { it.replace(".mp3", "") }
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

                val assetFileList = context.assets.list("$PATH_WORDUP/$oldCategory")!!
                if (assetFileList.any { !it.endsWith(".mp3") })
                    throw IllegalArgumentException(ErrorConstants.INITIALIZER_CATEGORY_SUBFOLDER)

                val fileList = assetFileList.map { it.replace(".mp3", "") }

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
