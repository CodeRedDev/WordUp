package de.codereddev.wordup

import android.content.Context
import android.content.pm.PackageManager
import de.codereddev.wordup.provider.WordUpProvider

object WordUp {
    lateinit var config: WordUpConfig
        private set

    fun isConfigInitialized() = this::config.isInitialized

    fun init(context: Context, config: WordUpConfig) {
        this.config = config

        if (config.sharingEnabled) {
            checkForProvider(context.applicationContext)
        }
    }

    private fun checkForProvider(context: Context) {
        context.packageManager.resolveContentProvider(
            "${context.packageName}.${WordUpProvider.PROVIDER_AUTHORITY}",
            PackageManager.GET_META_DATA
        ) ?: throw IllegalArgumentException(
            "WordUpProvider definition in manifest missing or incorrect!"
        )
    }
}
