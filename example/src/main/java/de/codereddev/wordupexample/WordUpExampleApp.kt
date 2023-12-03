package de.codereddev.wordupexample

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.WordUpConfig

@HiltAndroidApp
class WordUpExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()

        WordUp.init(
            this,
            WordUpConfig().apply {
                categoriesEnabled = true
                newWordsEnabled = true
                directory = resources.getString(R.string.app_name)
            }
        )
    }
}
