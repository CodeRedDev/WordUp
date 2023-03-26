package de.codereddev.wordupexample.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import de.codereddev.wordup.database.WordUpDatabase
import de.codereddev.wordup.util.LocalDbInitializer
import de.codereddev.wordupexample.R
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class LoadingScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var database: WordUpDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading_screen)

        GlobalScope.launch(Dispatchers.IO) {
            if (isAppUpdated()) {
                LocalDbInitializer(database).initialize(this@LoadingScreenActivity)
            }

            delay(2000)

            withContext(Dispatchers.Main) {
                startActivity(Intent(this@LoadingScreenActivity, MainActivity::class.java))
                finish()
            }
        }
    }

    /**
     * Checks if the app was updated from last versionCode.
     *
     * This is just an optional thing.
     * DbInitializer is doing a lot of IO work so this
     * little check can save you some time at app start.
     */
    private fun isAppUpdated(): Boolean {
        val currentVersionCode = packageManager.getPackageInfo(packageName, 0).versionCode
        val prefs = getPreferences(Context.MODE_PRIVATE)
        val savedVersionCode = prefs.getInt(PREF_KEY_VERSION_CODE, PREF_VERSION_CODE_DEF)

        if (currentVersionCode > savedVersionCode) {
            val editor = prefs.edit()
            editor.putInt(PREF_KEY_VERSION_CODE, currentVersionCode)
            editor.apply()
            return true
        }
        return false
    }

    companion object {
        private const val PREF_KEY_VERSION_CODE = "version_code"
        private const val PREF_VERSION_CODE_DEF = -1
    }
}
