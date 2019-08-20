package de.codereddev.wordup

import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import de.codereddev.wordup.dao.model.Category
import de.codereddev.wordup.dao.model.Sound
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// TODO: Throw own exceptions or log more detailled messages.
object StorageHelper {

    private val LOG_TAG = StorageHelper::class.java.simpleName

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Sets up a output/target file to save the given sound to.
     *
     * @param name Name of the sound to save.
     * @return The output/target file to save the sound to or NULL if something went wrong.
     */
    fun getSharedTargetFile(name: String): File? {
        if (!isExternalStorageWritable())
            return null

        val externalStorage = Environment.getExternalStorageDirectory()
        val dir = File("${externalStorage.absolutePath}/${WordUp.config.storagePublicDir}")

        try {
            dir.mkdirs()
        } catch (ex: SecurityException) {
            ex.printStackTrace()
            return null
        }

        return File(dir, "$name.mp3")
    }

    private fun getPrivateTargetFile(context: Context, name: String): File? {
        val dir = File("${context.filesDir}/sounds")

        try {
            dir.mkdirs()
        } catch (ex: SecurityException) {
            ex.printStackTrace()
            return null
        }

        return File(dir, "$name.mp3")
    }

    fun writeToPrivateStorage(context: Context, sound: Sound): File? {
        val targetFile = getPrivateTargetFile(context, sound.name)

        try {
            context.assets.open(getAssetPathFromSound(sound)).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (ex: IOException) {
            Log.e(LOG_TAG, "Failed to save file: ${ex.message}")
        }

        return targetFile
    }

    /**
     * Writes a sound to a target with the given name to the shared/external storage.
     *
     * @param context Is needed to access the assets.
     * @param sound The sound to write to the shared storage.
     * @return True if writing was successful, false otherwise.
     *
     */
    fun writeSoundToSharedStorage(context: Context, sound: Sound): Boolean {
        val targetFile = getSharedTargetFile(sound.name) ?: return false

        try {
            context.assets.open(getAssetPathFromSound(sound)).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (ex: IOException) {
            Log.e(LOG_TAG, "Failed to save file: ${ex.message}")
            return false
        }

        return true
    }

    // TODO: Outsource
//    fun shareSound(context: Context, sound: Sound): Boolean {
//        if (!writeSoundToSharedStorage(context, sound)) {
//            return false
//        }
//
//        val file = getSharedTargetFile(sound.name) ?: return false
//        val mediaUri = FileProvider.getUriForFile(context,
//            "${context.packageName}.fileprovider",
//            file)
//
//        val shareIntent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_STREAM, mediaUri)
//            type = "audio/mp3"
//        }
//        context.startActivity(Intent.createChooser(shareIntent, context.resources.getString(R.string.shareSound)))
//        return true
//    }

    /**
     *
     * @throws IOException
     */
    fun getAssetFdFromSound(context: Context, sound: Sound): AssetFileDescriptor {
        return context.assets.openFd(getAssetPathFromSound(sound))
    }

    /**
     * Builds the path to the wanted asset.
     */
    private fun getAssetPathFromSound(sound: Sound): String {
        return "${sound.rootDir}/${sound.category.name}/${sound.name}.mp3"
    }

    fun getPictureUri(category: Category): Uri {
        return Uri.parse("file:///android_asset/menu_pics/${category.name}.jpg")
    }

}