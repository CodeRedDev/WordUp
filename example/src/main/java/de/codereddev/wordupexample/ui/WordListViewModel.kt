package de.codereddev.wordupexample.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.codereddev.wordup.database.Word
import de.codereddev.wordup.database.WordDao
import de.codereddev.wordup.player.LocalWordUpPlayer
import de.codereddev.wordup.util.StorageUtils
import de.codereddev.wordup.util.UriUtils
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel
class WordListViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val wordDao: WordDao
) : ViewModel() {

    private val _wordList = MutableStateFlow<List<Word>>(emptyList())
    val wordList: StateFlow<List<Word>> = _wordList.asStateFlow()

    private val _events = Channel<Event>(Channel.Factory.BUFFERED)
    val events: Flow<Event> = _events.receiveAsFlow()

    private val _intents = Channel<Intent>(Channel.Factory.BUFFERED)
    val intents: Flow<Intent> = _intents.receiveAsFlow()

    private val _permissionRequests = Channel<PermissionRequest>(Channel.Factory.BUFFERED)
    val permissionRequests: Flow<PermissionRequest> = _permissionRequests.receiveAsFlow()

    private val wordupPlayer = LocalWordUpPlayer(context)

    init {
        viewModelScope.launch {
            wordDao.getAllWordsLive().collect { _wordList.value = it }
        }
    }

    fun onMenuItemSelected(context: Context, word: Word, action: WordAction) {
        when (action) {
            WordAction.SHARE -> {
                viewModelScope.launch {
                    val uri = UriUtils.getUriForWord(context, word)
                    val shareIntent = Intent().apply {
                        this.action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "audio/mp3"
                    }
                    _intents.send(shareIntent)
                }
            }
            WordAction.SAVE -> {
                viewModelScope.launch {
                    _permissionRequests.send(
                        PermissionRequest(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ACTION_SAVE,
                            word
                        )
                    )
                }
            }
            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    Settings.System.canWrite(context)
                ) {
                    val option = when (action) {
                        WordAction.SET_RINGTONE -> MediaStore.Audio.Media.IS_RINGTONE
                        WordAction.SET_NOTIFICATION -> MediaStore.Audio.Media.IS_NOTIFICATION
                        else -> MediaStore.Audio.Media.IS_ALARM
                    }
                    viewModelScope.launch {
                        _permissionRequests.send(
                            PermissionRequest(
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                ACTION_SET_SYSTEM_SOUND,
                                word,
                                option
                            )
                        )
                    }
                } else {
                    viewModelScope.launch {
                        _events.send(Event.PERMISSION_WRITE_SETTINGS)
                    }
                }
            }
        }
    }

    fun onWordClick(context: Context, word: Word) {
        wordupPlayer.play(context, word)
    }

    @SuppressLint("MissingPermission")
    fun onPermissionResult(
        context: Context,
        permissionRequest: PermissionRequest,
        granted: Boolean
    ) {
        when (permissionRequest.action) {
            ACTION_SAVE -> {
                if (granted) {
                    viewModelScope.launch(Dispatchers.IO) {
                        StorageUtils.storeWord(context, permissionRequest.word!!)
                        _events.send(Event.WORD_SAVED)
                    }
                }
            }
            ACTION_SET_SYSTEM_SOUND -> {
                if (granted) {
                    viewModelScope.launch(Dispatchers.IO) {
                        StorageUtils.setAsSystemSound(
                            context,
                            permissionRequest.word!!,
                            arrayOf(permissionRequest.systemSoundOption!!)
                        )
                        _events.send(Event.SYSTEM_SOUND_SET)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wordupPlayer.release()
    }

    enum class Event {
        PERMISSION_WRITE_SETTINGS,
        SYSTEM_SOUND_SET,
        WORD_SAVED
    }

    data class PermissionRequest(
        val permission: String,
        val action: Int,
        val word: Word? = null,
        val systemSoundOption: String? = null
    )

    companion object {
        const val ACTION_SAVE = 0
        const val ACTION_SET_SYSTEM_SOUND = 1
    }
}