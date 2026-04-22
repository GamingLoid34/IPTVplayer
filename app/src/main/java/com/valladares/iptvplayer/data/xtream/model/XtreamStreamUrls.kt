package com.valladares.iptvplayer.data.xtream.model

/**
 * Builds direct stream URLs for Xtream resources.
 */
object XtreamStreamUrls {
    /**
     * Creates live stream URL.
     */
    fun liveStreamUrl(
        credentials: XtreamCredentials,
        streamId: Int,
        extension: String = "m3u8"
    ): String = "${credentials.serverUrl}/live/${credentials.username}/${credentials.password}/$streamId.$extension"

    /**
     * Creates VOD stream URL.
     */
    fun vodStreamUrl(
        credentials: XtreamCredentials,
        streamId: Int,
        extension: String
    ): String = "${credentials.serverUrl}/movie/${credentials.username}/${credentials.password}/$streamId.$extension"

    /**
     * Creates series episode stream URL.
     */
    fun episodeStreamUrl(
        credentials: XtreamCredentials,
        episodeId: String,
        extension: String
    ): String = "${credentials.serverUrl}/series/${credentials.username}/${credentials.password}/$episodeId.$extension"
}
