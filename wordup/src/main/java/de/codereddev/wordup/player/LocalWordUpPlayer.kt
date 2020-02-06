package de.codereddev.wordup.player

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import de.codereddev.wordup.ErrorConstants
import de.codereddev.wordup.model.database.Sound

class LocalWordUpPlayer(context: Context) : WordUpPlayer {
    private val player = SimpleExoPlayer.Builder(context).build()

    override fun play(context: Context, sound: Sound) {
        if (sound.isNetworkResource)
            throw IllegalArgumentException(ErrorConstants.PLAYER_LOCAL_NO_LOCAL)

        val mediaSource = buildMediaSource(context, sound)
        player.playWhenReady = true
        player.prepare(mediaSource)
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

    private fun buildMediaSource(context: Context, sound: Sound): MediaSource {
        return ProgressiveMediaSource.Factory(
            DefaultDataSourceFactory(context, USER_AGENT)
        ).createMediaSource(Uri.parse("asset:///${sound.path}"))
    }

    companion object {
        const val USER_AGENT = "WordUpPlayer"
    }
}
