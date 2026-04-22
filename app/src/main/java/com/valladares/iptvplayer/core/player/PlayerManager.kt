package com.valladares.iptvplayer.core.player

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager that owns a single [ExoPlayer] instance for playback operations.
 */
@Singleton
class PlayerManager @Inject constructor(
    private val exoPlayer: ExoPlayer
) {
    /**
     * Exposes the underlying player for UI binding in [androidx.media3.ui.PlayerView].
     */
    val player: ExoPlayer
        get() = exoPlayer

    /**
     * Loads a media item from [url], prepares the player and starts playback.
     */
    fun play(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    /**
     * Pauses ongoing playback while retaining current state.
     */
    fun pause() {
        exoPlayer.pause()
    }

    /**
     * Stops playback and clears buffered state.
     */
    fun stop() {
        exoPlayer.stop()
    }

    /**
     * Releases player resources and underlying decoders.
     */
    fun release() {
        exoPlayer.release()
    }
}
