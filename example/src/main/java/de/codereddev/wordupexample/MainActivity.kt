package de.codereddev.wordupexample

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import de.codereddev.wordup.WordUp
import de.codereddev.wordupexample.ui.WordListScreen
import de.codereddev.wordupexample.ui.WordListViewModel
import de.codereddev.wordupexample.ui.theme.WordUpExampleTheme

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: WordListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WordUpExampleTheme {
                WordListContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun WordListContent(viewModel: WordListViewModel) {
    val context = LocalContext.current
    val resources = LocalResources.current

    var pendingRequest by remember {
        mutableStateOf<WordListViewModel.PermissionRequest?>(null)
    }

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        pendingRequest?.let { request ->
            viewModel.onPermissionResult(context, request, granted)
            pendingRequest = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                WordListViewModel.Event.PERMISSION_WRITE_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = "package:${context.packageName}".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                WordListViewModel.Event.SYSTEM_SOUND_SET -> {
                    Toast.makeText(context, R.string.system_sound_set, Toast.LENGTH_SHORT).show()
                }
                WordListViewModel.Event.WORD_SAVED -> {
                    val text = resources.getString(R.string.word_saved, WordUp.config.directory)
                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.intents.collect { shareIntent ->
            context.startActivity(
                Intent.createChooser(shareIntent, resources.getString(R.string.share_word_via))
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.permissionRequests.collect { request ->
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context, request.permission
            ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {
                viewModel.onPermissionResult(context, request, true)
            } else {
                pendingRequest = request
                permissionLauncher.launch(request.permission)
            }
        }
    }

    WordListScreen(viewModel)
}
