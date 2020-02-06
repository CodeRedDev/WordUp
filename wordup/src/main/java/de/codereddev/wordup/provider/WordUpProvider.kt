package de.codereddev.wordup.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordup.model.database.SoundDao
import de.codereddev.wordup.model.database.WordUpDatabase
import de.codereddev.wordup.util.StorageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * A [ContentProvider] for WordUp audio files (mp3).
 *
 * This provider stores files to the internal storage as most apps seem to
 * have no access to asset files that are used for locally stored sounds.
 */
class WordUpProvider : ContentProvider() {

    private lateinit var database: WordUpDatabase
    private lateinit var soundDao: SoundDao
    private lateinit var authority: String
    private lateinit var uriMatcher: UriMatcher

    override fun onCreate(): Boolean {
        database = WordUpDatabase.getInstance(context!!)
        soundDao = database.soundDao()
        authority = "${context!!.packageName}.$PROVIDER_AUTHORITY"
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(authority, URI_MATCH_SOUND_ID, URI_MATCH_SOUND_ID_CODE)
        }
        return true
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        return when (uriMatcher.match(uri)) {
            URI_MATCH_SOUND_ID_CODE -> {
                val soundId = uri.lastPathSegment!!.toInt()
                val sound = soundDao.getSoundById(soundId)
                var length = 0L
                StorageUtils.getAssetFd(context!!, sound).use {
                    length = it.length
                }

                return MatrixCursor(COLUMNS, 1).apply {
                    addRow(arrayOf(sound.name, length))
                }
            }
            else -> null
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    override fun getType(uri: Uri): String? {
        return "audio/mp3"
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        return when (uriMatcher.match(uri)) {
            URI_MATCH_SOUND_ID_CODE -> {
                val soundId = uri.lastPathSegment!!.toInt()
                val sound = soundDao.getSoundById(soundId)
                val categoryStr = if (sound.category != null) "_${sound.category}" else ""
                val fileName = "${sound.name}$categoryStr.mp3"

                /*
                 * Check if the file already exists in internal storage.
                 * If not create it. This part is synchronized so that
                 * different threads won't write a file multiple times.
                 */
                synchronized(this) {
                    val file = File(context!!.filesDir, fileName)
                    if (!file.exists()) {
                        var inputStream: InputStream? = null
                        var outputStream: OutputStream? = null

                        try {
                            inputStream = StorageUtils.getAssetInputStream(context!!, sound)
                            outputStream = FileOutputStream(file)
                            inputStream.copyTo(outputStream)
                        } catch (ex: IOException) {
                            Log.e(
                                LOG_TAG,
                                "Failed to save the sound file to internal storage!"
                            )
                        } finally {
                            inputStream?.close()
                            outputStream?.close()
                        }
                    }
                }

                val file = File(context!!.filesDir, fileName)
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }
            else -> null
        }
    }

    companion object {
        private val LOG_TAG = WordUpProvider::class.java.simpleName

        const val PROVIDER_AUTHORITY = "wordup.fileprovider"
        private const val URI_MATCH_SOUND_ID = "/#"
        private const val URI_MATCH_SOUND_ID_CODE = 1

        private val COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)

        /**
         * Creates a content uri for WordUp sounds.
         */
        fun getUriForSound(context: Context, sound: Sound): Uri {
            return Uri.Builder()
                .scheme("content")
                .authority("${context.applicationContext.packageName}.$PROVIDER_AUTHORITY")
                .encodedPath("/${sound.id}")
                .build()
        }
    }
}
