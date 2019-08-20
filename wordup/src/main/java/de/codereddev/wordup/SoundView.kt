package de.codereddev.wordup

import de.codereddev.wordup.dao.model.Sound

interface SoundView {
    fun onReceiveSoundList(soundList: List<Sound>)
}