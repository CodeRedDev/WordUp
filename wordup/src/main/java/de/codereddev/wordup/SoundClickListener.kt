package de.codereddev.wordup

import de.codereddev.wordup.dao.model.Sound

interface SoundClickListener {
    fun onSoundClick(sound: Sound)

    fun onSoundLongClick(sound: Sound)
}