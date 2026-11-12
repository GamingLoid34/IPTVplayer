package com.valladares.iptvplayer.data.playlist.model

/**
 * Effective HTTP playback headers for one playlist.
 *
 * [userAgent] is always populated (playlist override or app default), while [referer] is optional.
 */
data class PlaybackHeaders(
    val userAgent: String,
    val referer: String?
)
