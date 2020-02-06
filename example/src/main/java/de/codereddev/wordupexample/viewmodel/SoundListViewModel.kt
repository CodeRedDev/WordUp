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
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordup.model.database.SoundDao
import de.codereddev.wordup.player.LocalWordUpPlayer
import de.codereddev.wordup.provider.WordUpProvider
import de.codereddev.wordup.util.StorageUtils
import de.codereddev.wordupexample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class SoundListViewModel : ViewModel() {
    abstract val soundList: MutableLiveData<List<Sound>>
    abstract val events: MutableLiveData<Event>
    abstract val intents: MutableLiveData<Intent>
    abstract val permissionRequests: MutableLiveData<PermissionRequest>

    abstract fun onSoundClick(sound: Sound)

    abstract fun onMenuItemSelected(sound: Sound, itemId: Int)

    abstract fun onPermissionResult(permissionRequest: PermissionRequest, granted: Boolean)

    enum class Event {
        PERMISSION_WRITE_SETTINGS,
        SYSTEM_SOUND_SET,
        SOUND_SAVED
    }

    data class PermissionRequest(
        val permission: String,
        val action: Int,
        val sound: Sound? = null,
        val systemSoundOption: String? = null
    )

    companion object {
        const val ACTION_SAVE = 0
        const val ACTION_SET_SYSTEM_SOUND = 1
    }
}

class SoundListViewModelImpl(
    private val context: Context,
    private val soundDao: SoundDao
) : SoundListViewModel() {
    override val soundList: MutableLiveData<List<Sound>> = MutableLiveData()
    private val soundListObserver: Observer<List<Sound>> = Observer {
        soundList.postValue(it)
    }
    override val events: MutableLiveData<Event> = MutableLiveData()
    override val intents: MutableLiveData<Intent> = MutableLiveData()
    override val permissionRequests: MutableLiveData<PermissionRequest> = MutableLiveData()

    private val wordupPlayer = LocalWordUpPlayer(context)

    init {
        soundDao.getAllSoundsLive().observeForever(soundListObserver)
    }

    override fun onMenuItemSelected(sound: Sound, itemId: Int) {
        when (itemId) {
            R.id.action_share -> {
                val uri = WordUpProvider.getUriForSound(context, sound)
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "audio/mp3"
                }
                intents.postValue(shareIntent)
            }
            R.id.action_save -> {
                permissionRequests.postValue(
                    PermissionRequest(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ACTION_SAVE,
                        sound
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
                            sound,
                            option
                        )
                    )
                } else {
                    events.postValue(Event.PERMISSION_WRITE_SETTINGS)
                }
            }
        }
    }

    override fun onSoundClick(sound: Sound) {
        wordupPlayer.play(context, sound)
    }

    @SuppressLint("MissingPermission")
    override fun onPermissionResult(permissionRequest: PermissionRequest, granted: Boolean) {
        when (permissionRequest.action) {
            ACTION_SAVE -> {
                if (granted) {
                    viewModelScope.launch(Dispatchers.Main) {
                        StorageUtils.storeSound(context, WordUp.config, permissionRequest.sound!!)
                        events.postValue(Event.SOUND_SAVED)
                    }
                }
            }
            ACTION_SET_SYSTEM_SOUND -> {
                if (granted) {
                    viewModelScope.launch(Dispatchers.Main) {
                        StorageUtils.setAsSystemSound(
                            context,
                            WordUp.config,
                            permissionRequest.sound!!,
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
        soundDao.getAllSoundsLive().removeObserver(soundListObserver)
        wordupPlayer.release()
    }
}
