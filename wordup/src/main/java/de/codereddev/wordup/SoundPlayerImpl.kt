package de.codereddev.wordup

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import de.codereddev.wordup.dao.model.Sound
import java.io.IOException

// TODO: Control options outside?
/**
 * An implementation of [SoundPlayer] that uses [MediaPlayer] for playback.
 */
class SoundPlayerImpl : SoundPlayer {

    private val LOG_TAG = SoundPlayerImpl::class.java.simpleName
    private var mediaPlayer: MediaPlayer? = null
    private var currentSound: Sound? = null

    private fun initMediaPlayer() {
        if (mediaPlayer != null)
            return

        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnPreparedListener { it.start() }
        mediaPlayer?.setOnCompletionListener { currentSound = null }
    }

    override fun play(context: Context, sound: Sound) {
        initMediaPlayer()

        if (currentSound != sound) {
            try {
                currentSound = sound
                mediaPlayer?.reset()
                StorageHelper.getAssetFdFromSound(context, sound).use {
                    mediaPlayer?.setDataSource(it.fileDescriptor, it.startOffset, it.length)
                }
                mediaPlayer?.prepareAsync()
            } catch (ex: IOException) {
                Log.e(LOG_TAG, "Failed to play sound ${sound.name}: ${ex.message}")
            }
        } else {
            currentSound = null
            mediaPlayer?.stop()
            mediaPlayer?.reset()
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun resume() {
        mediaPlayer?.start()
    }

    override fun stop() {
        mediaPlayer?.stop()
    }

    override fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}