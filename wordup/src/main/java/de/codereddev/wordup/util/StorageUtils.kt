package de.codereddev.wordup.util

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import de.codereddev.wordup.ErrorConstants
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.database.Word
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * A utility class that provides easy to use functions for saving
 * word files to a custom public directory, set words as system
 * sound or retrieve file handles for internal or external files
 */
@Suppress("DEPRECATION")
object StorageUtils {

    const val WORDUP_DIRECTORY = "wordup"

    /**
     * An array of all available options to set a word as system sound.
     */
    val SYSTEM_SOUND_OPTIONS = arrayOf(
        MediaStore.Audio.Media.IS_RINGTONE,
        MediaStore.Audio.Media.IS_NOTIFICATION,
        MediaStore.Audio.Media.IS_ALARM
    )

    /**
     * Stores a word to the directory specified by [WordUpConfig.directory]
     * inside the public music directory.
     *
     * Be sure to grant [Manifest.permission.WRITE_EXTERNAL_STORAGE] for build
     * versions prior to [Build.VERSION_CODES.Q].
     *
     * As this function does I/O work it should be called asynchronously.
     *
     * @throws IllegalArgumentException if directory was not defined in WordUpConfig
     */
    fun storeWord(context: Context, word: Word) {
        if (!WordUp.isConfigInitialized())
            throw IllegalStateException(ErrorConstants.CONFIG_NOT_DEFINED)
        val config = WordUp.config
        checkConfigForDirectory(config)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storeWordLegacy(context, config, word)
        } else {
            storeWordQ(context, config, word)
        }
    }

    fun storeWordInCache(context: Context, word: Word) {
        val dir = File(context.cacheDir, WORDUP_DIRECTORY)
        dir.mkdirs()
        val file = File(dir, "${word.name}.mp3")
        if (file.exists())
            return

        FileOutputStream(file).use { output ->
            getAssetInputStream(context, word).use { input ->
                input.copyTo(output)
            }
        }
    }

    /**
     * Sets a word as standard system sound for the defined options.
     *
     * If the word isn't stored yet it will be stored.
     *
     * Be sure to check and request [Manifest.permission.WRITE_SETTINGS]
     * before calling this method!
     *
     * Be sure to grant [Manifest.permission.WRITE_EXTERNAL_STORAGE] for build
     * versions prior to [Build.VERSION_CODES.Q].
     *
     * As this function does I/O work it should be called asynchronously.
     *
     * @throws IllegalArgumentException if directory was not defined in WordUpConfig
     * @see [storeWord]
     */
    fun setAsSystemSound(
        context: Context,
        word: Word,
        soundOptions: Array<String>
    ) {
        if (!WordUp.isConfigInitialized())
            throw IllegalStateException(ErrorConstants.CONFIG_NOT_DEFINED)
        val config = WordUp.config
        checkConfigForDirectory(config)

        if (soundOptions.isEmpty())
            throw IllegalArgumentException(ErrorConstants.STORAGE_SYSTEM_SOUND_OPTION_EMPTY)
        soundOptions.forEach {
            if (!SYSTEM_SOUND_OPTIONS.contains(it))
                throw IllegalArgumentException(ErrorConstants.STORAGE_SYSTEM_SOUND_OPTION)
        }

        val contentUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storeWordLegacy(context, config, word)
        } else {
            storeWordQ(context, config, word)
        }

        val details = ContentValues()
        soundOptions.forEach {
            details.put(it, true)
        }
        context.contentResolver.update(contentUri, details, null, null)

        soundOptions.forEach {
            val type =
                when (it) {
                    MediaStore.Audio.Media.IS_RINGTONE -> RingtoneManager.TYPE_RINGTONE
                    MediaStore.Audio.Media.IS_NOTIFICATION -> RingtoneManager.TYPE_NOTIFICATION
                    else -> RingtoneManager.TYPE_ALARM
                }
            RingtoneManager.setActualDefaultRingtoneUri(context, type, contentUri)
        }
    }

    /**
     * @param word A locally stored word.
     * @return An open [AssetFileDescriptor] that has to be closed after use.
     *
     * @throws IllegalArgumentException If a word is not locally stored.
     */
    fun getAssetInputStream(context: Context, word: Word): InputStream {
        if (word.isNetworkResource)
            throw IllegalArgumentException(ErrorConstants.STORAGE_ASSET_INPUT_STREAM)

        return context.assets.open(word.path)
    }

    /**
     * Function to retrieve an [AssetFileDescriptor] that can be used e.g. for playing words.
     *
     * @param word A locally stored word.
     * @return An open [AssetFileDescriptor] that has to be closed after use.
     *
     * @throws IllegalArgumentException If a word is not locally stored.
     */
    fun getAssetFd(context: Context, word: Word): AssetFileDescriptor {
        if (word.isNetworkResource)
            throw IllegalArgumentException(ErrorConstants.STORAGE_ASSET_FD)

        return context.assets.openFd(word.path)
    }

    private fun checkConfigForDirectory(config: WordUpConfig) {
        if (config.directory == null)
            throw IllegalArgumentException(ErrorConstants.CONFIG_DIRECTORY_NOT_DEFINED)
    }

    /**
     * Stores a word to [Environment.DIRECTORY_MUSIC] and returns its content URI.
     *
     * @return The word's content URI on external storage
     */
    private fun storeWordLegacy(context: Context, config: WordUpConfig, word: Word): Uri {
        val dir = File(getWordDirectoryPath(config, word))
        dir.mkdirs()
        val file = File(dir, "${word.name}.mp3")

        FileOutputStream(file).use { output ->
            getAssetInputStream(context, word).use { input ->
                input.copyTo(output)
            }
        }

        var contentUri = getExistingMediaUriLegacy(context, config, word)
        if (contentUri != null)
            return contentUri

        val resolver = context.contentResolver
        val details = ContentValues()
        val audioCollection =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        details.apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "${word.name}.mp3")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.TITLE, word.name)
            put(MediaStore.Audio.Media.DATA, file.absolutePath)
            put(MediaStore.Audio.Media.ARTIST, config.directory)
            put(MediaStore.Audio.Media.ALBUM, config.directory)
            put(MediaStore.Audio.Media.IS_MUSIC, true)
        }

        contentUri = resolver.insert(audioCollection, details)
            ?: throw IOException(ErrorConstants.STORAGE_MEDIASTORE_INSERTING)

        return contentUri
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun storeWordQ(context: Context, config: WordUpConfig, word: Word): Uri {
        val resolver = context.contentResolver
        var contentUri = getExistingMediaUriQ(context, config, word)
        val details = ContentValues()
        details.put(MediaStore.Audio.Media.IS_PENDING, 1)
        if (contentUri == null) {
            val audioCollection =
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            details.apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "${word.name}.mp3")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
                put(MediaStore.Audio.Media.TITLE, word.name)
                put(MediaStore.Audio.Media.RELATIVE_PATH, getRelativePathQ(config, word))
                put(MediaStore.Audio.Media.ARTIST, config.directory)
                put(MediaStore.Audio.Media.ALBUM, config.directory)
                put(MediaStore.Audio.Media.IS_MUSIC, true)
            }

            contentUri = resolver.insert(audioCollection, details)
                ?: throw IOException(ErrorConstants.STORAGE_MEDIASTORE_INSERTING)
        } else {
            resolver.update(contentUri, details, null, null)
        }

        resolver.openFileDescriptor(contentUri, "w", null)!!.use { mediaFd ->
            FileOutputStream(mediaFd.fileDescriptor).use { output ->
                getAssetInputStream(context, word).use { input ->
                    input.copyTo(output)
                }
            }
        }

        details.clear()
        details.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(contentUri, details, null, null)

        return contentUri
    }

    private fun getWordDirectoryPath(config: WordUpConfig, word: Word): String {
        val subDirPath = if (config.categoriesEnabled) "/${word.category!!.name}" else ""
        return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}" +
                "/${config.directory}$subDirPath"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getRelativePathQ(config: WordUpConfig, word: Word): String {
        val subDirPath = if (config.categoriesEnabled) "/${word.category!!.name}" else ""
        return "${Environment.DIRECTORY_MUSIC}/${config.directory}$subDirPath"
    }

    private fun getExistingMediaUriLegacy(
        context: Context,
        config: WordUpConfig,
        word: Word
    ): Uri? {
        val resolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA
        )

        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val expectedPath = "${getWordDirectoryPath(config, word)}/${word.name}.mp3"

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val path = cursor.getString(pathCol)

                if (name == "${word.name}.mp3" && path == expectedPath) {
                    return ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                }
            }
        }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getExistingMediaUriQ(context: Context, config: WordUpConfig, word: Word): Uri? {
        val resolver = context.contentResolver

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.RELATIVE_PATH
        )

        resolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.RELATIVE_PATH)
            val expectedPath = getRelativePathQ(config, word)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val path = cursor.getString(pathCol)

                if (name == "${word.name}.mp3" && path == expectedPath) {
                    return ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                }
            }
        }

        return null
    }
}
