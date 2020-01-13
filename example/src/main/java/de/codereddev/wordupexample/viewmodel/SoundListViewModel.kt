package de.codereddev.wordupexample.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import de.codereddev.wordup.model.database.Sound
import de.codereddev.wordup.model.database.SoundDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class SoundListViewModel : ViewModel() {
    abstract val soundList: MutableLiveData<List<Sound>>
    abstract val events: MutableLiveData<Event>

    abstract fun addSound()
    abstract fun deleteSound(sound: Sound)

    enum class Event {
        INSERT,
        DELETE
    }
}

class SoundListViewModelImpl(
    private val soundDao: SoundDao
) : SoundListViewModel() {
    override val soundList: MutableLiveData<List<Sound>> = MutableLiveData()
    private val soundListObserver: Observer<List<Sound>> = Observer {
        soundList.postValue(it)
    }
    override val events: MutableLiveData<Event> = MutableLiveData()

    init {
        soundDao.getAllSounds().observeForever(soundListObserver)
    }

    override fun addSound() {
        GlobalScope.launch(Dispatchers.IO) {
            soundDao.insert(Sound(name = "Sound name cool", path = "path://somePath"))
            events.postValue(Event.INSERT)
        }
    }

    override fun deleteSound(sound: Sound) {
        GlobalScope.launch(Dispatchers.IO) {
            soundDao.delete(sound.id)
            events.postValue(Event.DELETE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundDao.getAllSounds().removeObserver(soundListObserver)
    }
}
