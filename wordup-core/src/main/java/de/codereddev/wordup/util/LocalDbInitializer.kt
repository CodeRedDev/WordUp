package de.codereddev.wordup.util

import android.content.Context
import de.codereddev.wordup.ErrorConstants
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.database.Category
import de.codereddev.wordup.database.Word
import de.codereddev.wordup.database.WordUpDatabase

/**
 * This helps to initialize the [WordUpDatabase] with locally stored words from assets.
 * This will also update the database based on the files
 * you delete or add to the assets/wordup folder.
 *
 * The initializing process uses the [WordUpConfig] given to [WordUp.init].
 * If non is given it will throw an [IllegalStateException].
 *
 * If [WordUpConfig.categoriesEnabled] is false words should be
 * organized matching the following path:
 * assets/wordup/word.mp3
 *
 * Renamed words will be processed as new words i.e. the old entry will
 * be deleted and the word will be re-added with the new name.
 *
 * If [WordUpConfig.categoriesEnabled] is true words should instead be
 * organized matching the following path:
 * assets/wordup/category-subfolder/word.mp3
 *
 * Renamed categories will be processed as new categories i.e the old category
 * and all words referencing this category will be deleted and the category
 * plus all its words will be re-added with the new name.
 *
 * Words in categories that were not edited will behave like non-category words.
 *
 * If [WordUpConfig.newWordsEnabled] is true newly added words will be marked as such.
 */
class LocalDbInitializer(database: WordUpDatabase) {
    private val wordDao = database.getWordDao()
    private val categoryDao = database.getCategoryDao()

    /**
     * Initialize the database from locally stored asset files.
     *
     * As this function does a lot of I/O work it should be called asynchronously.
     */
    fun initialize(context: Context) {
        if (!WordUp.isConfigInitialized())
            throw IllegalStateException(ErrorConstants.CONFIG_NOT_DEFINED)
        val config = WordUp.config

        if (config.categoriesEnabled) {
            initializeWithCategory(context.applicationContext, config)
        } else {
            initializeWithoutCategory(context.applicationContext, config)
        }
    }

    private fun initializeWithoutCategory(context: Context, config: WordUpConfig) {
        val curWordNames = wordDao.getAllWords().map { it.name }
        val assetList = getWordUpAssets(context)
        if (assetList.any { !it.endsWith(".mp3") })
            throw IllegalArgumentException(ErrorConstants.INITIALIZER_NO_CATEGORY_SUBFOLDER)

        val fileList = assetList.map { it.replace(".mp3", "") }

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

    private fun initializeWithCategory(context: Context, config: WordUpConfig) {
        val curCategories = categoryDao.getAllCategories().map { it.name }

        val assetList = getWordUpAssets(context)
        if (assetList.any { it.endsWith(".mp3") })
            throw IllegalArgumentException(ErrorConstants.INITIALIZER_CATEGORY_ROOT_MP3)

        val dirList = assetList.toList()

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
            val assetFileList = getWordUpCategoryAssets(context, newCategory)
            if (assetFileList.any { !it.endsWith(".mp3") })
                throw IllegalArgumentException(ErrorConstants.INITIALIZER_CATEGORY_SUBFOLDER)

            val fileList = assetFileList.map { it.replace(".mp3", "") }
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

            val assetFileList = getWordUpCategoryAssets(context, oldCategory)
            if (assetFileList.any { !it.endsWith(".mp3") })
                throw IllegalArgumentException(ErrorConstants.INITIALIZER_CATEGORY_SUBFOLDER)

            val fileList = assetFileList.map { it.replace(".mp3", "") }

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

    private fun getWordUpAssets(context: Context): Array<String> {
        return context.assets.list(PATH_WORDUP)
            ?: throw RuntimeException(ErrorConstants.INITIALIZER_WORDUP_ASSET)
    }

    private fun getWordUpCategoryAssets(context: Context, category: String): Array<String> {
        return context.assets.list("$PATH_WORDUP/$category")
            ?: throw RuntimeException(ErrorConstants.INITIALIZER_WORDUP_ASSET)
    }

    companion object {
        private const val PATH_WORDUP = "wordup"
    }
}
