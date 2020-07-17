package de.codereddev.wordup

import android.content.Context
import android.content.pm.PackageManager

object WordUp {
    lateinit var config: WordUpConfig
        private set

    fun isConfigInitialized() = this::config.isInitialized

    const val PROVIDER_AUTHORITY = "wordup.fileprovider"

    fun init(context: Context, config: WordUpConfig) {
        this.config = config

        if (config.sharingEnabled) {
            checkForProvider(context.applicationContext)
        }
    }

    private fun checkForProvider(context: Context) {
        context.packageManager.resolveContentProvider(
            "${context.packageName}.$PROVIDER_AUTHORITY",
            PackageManager.GET_META_DATA
        ) ?: throw IllegalArgumentException(
            ErrorConstants.FILE_PROVIDER_MISSING
        )
    }
}
