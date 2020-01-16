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
import de.codereddev.wordup.WordUpConfig
import de.codereddev.wordup.model.database.Sound
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * A utility class that provides easy to use functions for saving
 * sound files to a custom public directory, set sounds as system
 * sound or retrieve file handles for internal or external files
 */
@Suppress("DEPRECATION")
object StorageUtils {

    /**
     * An array of all available options to set a sound as system sound.
     */
    val SYSTEM_SOUND_OPTIONS = arrayOf(
        MediaStore.Audio.Media.IS_RINGTONE,
        MediaStore.Audio.Media.IS_NOTIFICATION,
        MediaStore.Audio.Media.IS_ALARM
    )

    /**
     * Stores a sound to the directory specified by [WordUpConfig.directory]
     * inside the public music directory.
     *
     * Be sure to grant [Manifest.permission.WRITE_EXTERNAL_STORAGE] for build
     * versions prior to [Build.VERSION_CODES.Q].
     *
     * @throws IllegalArgumentException if directory was not defined in WordUpConfig
     */
    suspend fun storeSound(context: Context, config: WordUpConfig, sound: Sound) =
        withContext(Dispatchers.IO) {
            checkConfigForDirectory(config)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                storeSoundLegacy(context, config, sound)
            } else {
                storeSoundQ(context, config, sound)
            }
        }

    /**
     * Sets a sound as standard system sound for the defined options.
     *
     * If the sound isn't stored yet it will be stored.
     *
     * Be sure to check and request [Manifest.permission.WRITE_SETTINGS]
     * before calling this method!
     *
     * Be sure to grant [Manifest.permission.WRITE_EXTERNAL_STORAGE] for build
     * versions prior to [Build.VERSION_CODES.Q].
     *
     * @throws IllegalArgumentException if directory was not defined in WordUpConfig
     * @see [storeSound]
     */
    suspend fun setAsSystemSound(
        context: Context,
        config: WordUpConfig,
        sound: Sound,
        soundOptions: Array<String>
    ) = withContext(Dispatchers.IO) {
        checkConfigForDirectory(config)

        if (soundOptions.isEmpty())
            throw IllegalArgumentException(ErrorConstants.STORAGE_SYSTEM_SOUND_OPTION_EMPTY)
        soundOptions.forEach {
            if (!SYSTEM_SOUND_OPTIONS.contains(it))
                throw IllegalArgumentException(ErrorConstants.STORAGE_SYSTEM_SOUND_OPTION)
        }

        val contentUri = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            storeSoundLegacy(context, config, sound)
        } else {
            storeSoundQ(context, config, sound)
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
     * @param sound A locally stored sound.
     * @return An open [AssetFileDescriptor] that has to be closed after use.
     *
     * @throws IllegalArgumentException If a sound is not locally stored.
     */
    fun getAssetInputStream(context: Context, sound: Sound): InputStream {
        if (sound.isNetworkResource)
            throw IllegalArgumentException(ErrorConstants.STORAGE_ASSET_INPUT_STREAM)

        return context.assets.open(sound.path)
    }

    /**
     * Function to retrieve an [AssetFileDescriptor] that can be used e.g. for playing sounds.
     *
     * @param sound A locally stored sound.
     * @return An open [AssetFileDescriptor] that has to be closed after use.
     *
     * @throws IllegalArgumentException If a sound is not locally stored.
     */
    fun getAssetFd(context: Context, sound: Sound): AssetFileDescriptor {
        if (sound.isNetworkResource)
            throw IllegalArgumentException(ErrorConstants.STORAGE_ASSET_FD)

        return context.assets.openFd(sound.path)
    }

    private fun checkConfigForDirectory(config: WordUpConfig) {
        if (config.directory == null)
            throw IllegalArgumentException(ErrorConstants.CONFIG_DIRECTORY_NOT_DEFINED)
    }

    /**
     * Stores a sound to [Environment.DIRECTORY_MUSIC] and returns its content URI.
     *
     * @return The sounds content URI on external storage
     */
    private fun storeSoundLegacy(context: Context, config: WordUpConfig, sound: Sound): Uri {
        val dir = File(getSoundDirectoryPath(config, sound))
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, "${sound.name}.mp3")

        FileOutputStream(file).use { output ->
            getAssetInputStream(context, sound).use { input ->
                input.copyTo(output)
            }
        }

        var contentUri = getExistingMediaUriLegacy(context, config, sound)
        if (contentUri != null)
            return contentUri

        val resolver = context.contentResolver
        val details = ContentValues()
        val audioCollection =
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        details.apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "${sound.name}.mp3")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.TITLE, sound.name)
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
    private fun storeSoundQ(context: Context, config: WordUpConfig, sound: Sound): Uri {
        val resolver = context.contentResolver
        var contentUri = getExistingMediaUriQ(context, config, sound)
        val details = ContentValues()
        details.put(MediaStore.Audio.Media.IS_PENDING, 1)
        if (contentUri == null) {
            val audioCollection =
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            details.apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, "${sound.name}.mp3")
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
                put(MediaStore.Audio.Media.TITLE, sound.name)
                put(MediaStore.Audio.Media.RELATIVE_PATH, getRelativePathQ(config, sound))
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
                getAssetInputStream(context, sound).use { input ->
                    input.copyTo(output)
                }
            }
        }

        details.clear()
        details.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(contentUri, details, null, null)

        return contentUri
    }

    private fun getSoundDirectoryPath(config: WordUpConfig, sound: Sound): String {
        val subDirPath = if (config.categoriesEnabled) "/${sound.category!!.name}" else ""
        return "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)}" +
                "/${config.directory}$subDirPath"
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getRelativePathQ(config: WordUpConfig, sound: Sound): String {
        val subDirPath = if (config.categoriesEnabled) "/${sound.category!!.name}" else ""
        return "${Environment.DIRECTORY_MUSIC}/${config.directory}$subDirPath"
    }

    private fun getExistingMediaUriLegacy(
        context: Context,
        config: WordUpConfig,
        sound: Sound
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
            val expectedPath = "${getSoundDirectoryPath(config, sound)}/${sound.name}.mp3"

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val path = cursor.getString(pathCol)

                if (name == "${sound.name}.mp3" && path == expectedPath) {
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
    private fun getExistingMediaUriQ(context: Context, config: WordUpConfig, sound: Sound): Uri? {
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
            val expectedPath = getRelativePathQ(config, sound)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val name = cursor.getString(nameCol)
                val path = cursor.getString(pathCol)

                if (name == "${sound.name}.mp3" && path == expectedPath) {
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
