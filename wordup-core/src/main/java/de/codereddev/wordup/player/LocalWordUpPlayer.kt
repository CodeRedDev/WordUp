package de.codereddev.wordup.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import de.codereddev.wordup.ErrorConstants
import de.codereddev.wordup.database.Word

class LocalWordUpPlayer(context: Context) : WordUpPlayer {
    private val player = ExoPlayer.Builder(context).build()

    override fun play(context: Context, word: Word) {
        if (word.isNetworkResource) {
            throw IllegalArgumentException(ErrorConstants.PLAYER_LOCAL_NO_LOCAL)
        }

        val mediaItem = buildMediaItem(word)
        player.setMediaItem(mediaItem)
        player.playWhenReady = true
        player.prepare()
    }

    override fun pause() {
        player.playWhenReady = false
    }

    override fun resume() {
        player.playWhenReady = true
    }

    override fun stop() {
        player.stop()
    }

    override fun release() {
        player.release()
    }

    private fun buildMediaItem(word: Word): MediaItem {
        return MediaItem.fromUri(Uri.parse("asset:///${word.path}"))
    }
}
