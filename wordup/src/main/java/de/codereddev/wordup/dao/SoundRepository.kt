package de.codereddev.wordup.dao

import android.app.Application

class SoundRepository(application: Application) {
    private val database = SoundDatabase.getInstance(application)
    private val soundDao = database.soundDao()
    private val favoriteDao = database.favoriteDao()
}