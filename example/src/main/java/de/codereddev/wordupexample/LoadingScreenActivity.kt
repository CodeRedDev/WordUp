package de.codereddev.wordupexample

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import de.codereddev.wordup.database.WordUpDatabase
import de.codereddev.wordup.util.LocalDbInitializer
import de.codereddev.wordupexample.ui.LoadingScreen
import de.codereddev.wordupexample.ui.theme.WordUpExampleTheme
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LoadingScreenActivity : AppCompatActivity() {

    @Inject
    lateinit var database: WordUpDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordUpExampleTheme {
                LoadingScreen()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
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
        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val currentVersionCode = PackageInfoCompat.getLongVersionCode(packageInfo)
        val prefs = getPreferences(MODE_PRIVATE)
        val savedVersionCode = prefs.getLong(PREF_KEY_VERSION_CODE, PREF_VERSION_CODE_DEF)

        if (currentVersionCode > savedVersionCode) {
            prefs.edit {
                putLong(PREF_KEY_VERSION_CODE, currentVersionCode)
            }
            return true
        }
        return false
    }

    companion object {
        private const val PREF_KEY_VERSION_CODE = "version_code"
        private const val PREF_VERSION_CODE_DEF = -1L
    }
}
