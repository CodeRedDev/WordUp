package de.codereddev.wordup

import android.content.Context
import de.codereddev.wordup.dao.model.Sound

interface SoundPlayer {
    /**
     * @param context Context for retrieving resources or the like.
     * @param sound Sound to play.
     */
    fun play(context: Context, sound: Sound)

    fun pause()

    fun resume()

    fun stop()

    /**
     * If your implementation depends on any kind of resource release be sure
     * to implement and call this if your [SoundPlayer] instance is no longer needed.
     */
    fun release()
}