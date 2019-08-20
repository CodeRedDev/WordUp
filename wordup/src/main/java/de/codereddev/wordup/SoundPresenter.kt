package de.codereddev.wordup

interface SoundPresenter {
    /**
     * This method should retrieve a list of [Sound] and send it to [SoundView.displaySoundList].
     */
    fun retrieveSoundList()
}