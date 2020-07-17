package de.codereddev.wordup.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.model.database.Word
import java.io.File

object UriUtils {

    /**
     * Stores the word to the app's cache directory and returns the content URI to this file.
     *
     * As this function might do some I/O action it should be called asynchronously.
     */
    fun getUriForWord(context: Context, word: Word): Uri {
        StorageUtils.storeWordInCache(context, word)

        val file = File("${context.cacheDir}/${StorageUtils.WORDUP_DIRECTORY}", "${word.name}.mp3")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.${WordUp.PROVIDER_AUTHORITY}",
            file
        )
    }
}
