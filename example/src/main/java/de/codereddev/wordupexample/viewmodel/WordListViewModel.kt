package de.codereddev.wordupexample.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.codereddev.wordup.WordUp
import de.codereddev.wordup.model.database.Word
import de.codereddev.wordup.model.database.WordDao
import de.codereddev.wordup.player.LocalWordUpPlayer
import de.codereddev.wordup.util.StorageUtils
import de.codereddev.wordup.util.UriUtils
import de.codereddev.wordupexample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class WordListViewModel : ViewModel() {
    abstract val wordList: MutableLiveData<List<Word>>
    abstract val events: MutableLiveData<Event>
    abstract val intents: MutableLiveData<Intent>
    abstract val permissionRequests: MutableLiveData<PermissionRequest>

    abstract fun onWordClick(word: Word)

    abstract fun onMenuItemSelected(word: Word, itemId: Int)

    abstract fun onPermissionResult(permissionRequest: PermissionRequest, granted: Boolean)

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

class WordListViewModelImpl(
    private val context: Context,
    private val wordDao: WordDao
) : WordListViewModel() {
    override val wordList: MutableLiveData<List<Word>> = MutableLiveData()
    private val wordListObserver: Observer<List<Word>> = Observer {
        wordList.postValue(it)
    }
    override val events: MutableLiveData<Event> = MutableLiveData()
    override val intents: MutableLiveData<Intent> = MutableLiveData()
    override val permissionRequests: MutableLiveData<PermissionRequest> = MutableLiveData()

    private val wordupPlayer = LocalWordUpPlayer(context)

    init {
        wordDao.getAllWordsLive().observeForever(wordListObserver)
    }

    override fun onMenuItemSelected(word: Word, itemId: Int) {
        when (itemId) {
            R.id.action_share -> {
                viewModelScope.launch {
                    val uri = UriUtils.getUriForWord(context, word)
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uri)
                        type = "audio/mp3"
                    }
                    intents.postValue(shareIntent)
                }
            }
            R.id.action_save -> {
                permissionRequests.postValue(
                    PermissionRequest(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ACTION_SAVE,
                        word
                    )
                )
            }
            else -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    Settings.System.canWrite(context)
                ) {
                    val option = when (itemId) {
                        R.id.action_set_ringtone -> MediaStore.Audio.Media.IS_RINGTONE
                        R.id.action_set_notification -> MediaStore.Audio.Media.IS_NOTIFICATION
                        else -> MediaStore.Audio.Media.IS_ALARM
                    }
                    permissionRequests.postValue(
                        PermissionRequest(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            ACTION_SET_SYSTEM_SOUND,
                            word,
                            option
                        )
                    )
                } else {
                    events.postValue(Event.PERMISSION_WRITE_SETTINGS)
                }
            }
        }
    }

    override fun onWordClick(word: Word) {
        wordupPlayer.play(context, word)
    }

    @SuppressLint("MissingPermission")
    override fun onPermissionResult(permissionRequest: PermissionRequest, granted: Boolean) {
        when (permissionRequest.action) {
            ACTION_SAVE -> {
                if (granted) {
                    viewModelScope.launch(Dispatchers.IO) {
                        StorageUtils.storeWord(context, WordUp.config, permissionRequest.word!!)
                        events.postValue(Event.WORD_SAVED)
                    }
                }
            }
            ACTION_SET_SYSTEM_SOUND -> {
                if (granted) {
                    viewModelScope.launch(Dispatchers.IO) {
                        StorageUtils.setAsSystemSound(
                            context,
                            WordUp.config,
                            permissionRequest.word!!,
                            arrayOf(permissionRequest.systemSoundOption!!)
                        )
                        events.postValue(Event.SYSTEM_SOUND_SET)
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        wordDao.getAllWordsLive().removeObserver(wordListObserver)
        wordupPlayer.release()
    }
}
