package de.codereddev.wordup.player

import android.content.Context
import de.codereddev.wordup.model.database.Sound

interface WordUpPlayer {

    /**
     * Prepares the player for sound playback and starts it.
     *
     * @param context Context for resource gathering
     * @param sound The sound to play
     */
    fun play(context: Context, sound: Sound)

    fun pause()

    fun resume()

    fun stop()

    /**
     * Releases any memory associated with the player.
     *
     * This should be called when the player is not needed anymore to prevent memory leaks.
     */
    fun release()
}
