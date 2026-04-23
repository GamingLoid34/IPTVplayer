package com.valladares.iptvplayer.core.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.valladares.iptvplayer.data.xtream.WatchHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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
    @ApplicationContext private val context: Context,
    private val watchHistoryRepository: WatchHistoryRepository
) {
    @Volatile
    private var _player: ExoPlayer? = null
    private val trackingScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentTrackingChannelId: Long? = null
    private var currentTrackingPlaylistId: String? = null
    private var currentTrackingStartMs: Long = 0L

    /**
     * The active [ExoPlayer], created lazily on first use.
     *
     * Each call can return a new instance after a prior [release]; callers must not
     * retain references to a released player.
     */
    val player: ExoPlayer
        get() = _player ?: error("Player has not been prepared yet. Call play(...) first.")

    private fun createPlayer(userAgent: String, referer: String?): ExoPlayer {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30_000)
            .setReadTimeoutMs(30_000)
        if (!referer.isNullOrBlank()) {
            httpDataSourceFactory.setDefaultRequestProperties(
                mapOf("Referer" to referer)
            )
        }
        val dataSourceFactory = DefaultDataSource.Factory(
            context,
            httpDataSourceFactory
        )
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
        return ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    /**
     * Starts playback of the given [url] (HLS, progressive, etc.).
     *
     * Creates the underlying player on first use.
     */
    suspend fun play(
        url: String,
        userAgent: String,
        referer: String?,
        playlistId: String?,
        liveChannelId: Long?
    ) {
        android.util.Log.d("PlayerManager", "play() called with url=$url")
        recordCurrentWatch()
        _player?.release()
        val p = createPlayer(
            userAgent = userAgent,
            referer = referer
        ).also { _player = it }
        p.setMediaItem(MediaItem.fromUri(url))
        p.prepare()
        p.playWhenReady = true
        currentTrackingPlaylistId = playlistId
        currentTrackingChannelId = liveChannelId
        currentTrackingStartMs = System.currentTimeMillis()
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
        recordCurrentWatch()
        _player?.stop()
    }

    /**
     * Releases the current [ExoPlayer] and clears the reference so the next
     * [play] or [player] access creates a new instance. Safe to call multiple times
     * (idempotent, does not throw).
     */
    fun release() {
        recordCurrentWatch()
        _player?.release()
        _player = null
    }

    private fun recordCurrentWatch() {
        val channelId = currentTrackingChannelId
        val playlistId = currentTrackingPlaylistId
        val durationMs = System.currentTimeMillis() - currentTrackingStartMs
        if (durationMs >= 30_000L && channelId != null && playlistId != null) {
            trackingScope.launch {
                watchHistoryRepository.recordWatch(
                    playlistId = playlistId,
                    liveChannelId = channelId,
                    durationMs = durationMs
                )
            }
        }
        currentTrackingChannelId = null
        currentTrackingPlaylistId = null
        currentTrackingStartMs = 0L
    }
}
