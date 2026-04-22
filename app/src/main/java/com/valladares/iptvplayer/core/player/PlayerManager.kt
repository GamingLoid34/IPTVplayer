package com.valladares.iptvplayer.core.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that owns the current [ExoPlayer] and its lifecycle.
 *
 * The player instance is **not** fixed for the app process lifetime: [ExoPlayer] is
 * created on demand when [play] is called or [player] is first read, and is torn
 * down by [release]. After [release], a **new** [ExoPlayer] is created on the next
 * [play] or [player] access.
 *
 * All public methods must be called from the main (UI) thread, which matches
 * [ExoPlayer] usage requirements.
 */
@Singleton
class PlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @Volatile
    private var _player: ExoPlayer? = null

    /**
     * The active [ExoPlayer], created lazily on first use.
     *
     * Each call can return a new instance after a prior [release]; callers must not
     * retain references to a released player.
     */
    val player: ExoPlayer
        get() = _player ?: createPlayer().also { _player = it }

    private fun createPlayer(): ExoPlayer = ExoPlayer.Builder(context).build()

    /**
     * Starts playback of the given [url] (HLS, progressive, etc.).
     *
     * Creates the underlying player on first use.
     */
    fun play(url: String) {
        val p = player
        p.setMediaItem(MediaItem.fromUri(url))
        p.prepare()
        p.playWhenReady = true
    }

    /**
     * Pauses playback. No-op if no player has been created yet.
     */
    fun pause() {
        _player?.pause()
    }

    /**
     * Stops playback. No-op if no player has been created yet.
     */
    fun stop() {
        _player?.stop()
    }

    /**
     * Releases the current [ExoPlayer] and clears the reference so the next
     * [play] or [player] access creates a new instance. Safe to call multiple times
     * (idempotent, does not throw).
     */
    fun release() {
        _player?.release()
        _player = null
    }
}
