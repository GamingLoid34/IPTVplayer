package com.valladares.iptvplayer.core.player

import android.content.Context
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.text.TextRenderer
import com.valladares.iptvplayer.core.common.AppConstants
import com.valladares.iptvplayer.data.xtream.WatchHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
private class LegacySubtitleRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    override fun buildTextRenderers(
        context: Context,
        output: TextOutput,
        outputLooper: Looper,
        extensionRendererMode: Int,
        out: ArrayList<Renderer>
    ) {
        super.buildTextRenderers(context, output, outputLooper, extensionRendererMode, out)
        out.filterIsInstance<TextRenderer>().forEach { renderer ->
            renderer.experimentalSetLegacyDecodingEnabled(true)
        }
    }
}

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
        get() = _player ?: createPlayer(
            userAgent = AppConstants.DEFAULT_USER_AGENT,
            referer = null
        ).also { _player = it }

    private fun createPlayer(userAgent: String, referer: String?): ExoPlayer {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(userAgent)
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30_000)
            .setReadTimeoutMs(30_000)
        val originalHttpFactory = httpDataSourceFactory
        val loggingHttpFactory = DataSource.Factory {
            val source = originalHttpFactory.createDataSource()
            object : DataSource by source {
                override fun open(dataSpec: DataSpec): Long {
                    android.util.Log.d("SubDiag-Http", "HTTP OPEN: ${dataSpec.uri}")
                    return try {
                        val result = source.open(dataSpec)
                        android.util.Log.d("SubDiag-Http", "HTTP OK: ${dataSpec.uri} bytes=$result")
                        result
                    } catch (e: Exception) {
                        android.util.Log.e("SubDiag-Http", "HTTP FAIL: ${dataSpec.uri} error=${e.message}", e)
                        throw e
                    }
                }
            }
        }
        if (!referer.isNullOrBlank()) {
            httpDataSourceFactory.setDefaultRequestProperties(
                mapOf("Referer" to referer)
            )
        }
        val dataSourceFactory = DefaultDataSource.Factory(
            context,
            loggingHttpFactory
        )
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(dataSourceFactory)
            .experimentalParseSubtitlesDuringExtraction(false)
        val renderersFactory = LegacySubtitleRenderersFactory(context)
            .setEnableDecoderFallback(true)
        android.util.Log.d("SubDiag", "Renderers factory created with legacy decoding ENABLED (via TextRenderer subclass)")
        return ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
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
        liveChannelId: Long?,
        subtitles: List<androidx.media3.common.MediaItem.SubtitleConfiguration>? = null
    ) {
        android.util.Log.d("PlayerManager", "play() called with url=$url, subtitles=${subtitles?.size ?: 0}")
        android.util.Log.d("SubDiag", "=== PlayerManager.play START ===")
        android.util.Log.d("SubDiag", "Stream URL: $url")
        android.util.Log.d("SubDiag", "User-Agent: $userAgent")
        android.util.Log.d("SubDiag", "Referer: $referer")
        android.util.Log.d("SubDiag", "Subtitle count: ${subtitles?.size ?: 0}")
        subtitles?.forEachIndexed { idx, sub ->
            android.util.Log.d(
                "SubDiag",
                "  Sub[$idx]: id=${sub.id} label=${sub.label} lang=${sub.language} mime=${sub.mimeType} uri=${sub.uri}"
            )
        }
        recordCurrentWatch()
        _player?.release()
        val p = createPlayer(
            userAgent = userAgent,
            referer = referer
        ).also { _player = it }

        val mediaItemBuilder = MediaItem.Builder().setUri(url)
        if (!subtitles.isNullOrEmpty()) {
            mediaItemBuilder.setSubtitleConfigurations(subtitles)
        }
        p.setMediaItem(mediaItemBuilder.build())
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
