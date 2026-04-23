package com.valladares.iptvplayer.navigation

import android.net.Uri

/**
 * Type-safe app destinations for Jetpack Navigation.
 */
sealed class NavDestination(val route: String) {
    /**
     * Start screen: playlist overview.
     */
    data object Home : NavDestination("home")

    /**
     * Channel list for a single playlist. Path parameter: [playlistId].
     */
    data object Channels : NavDestination("channels/{playlistId}") {
        /**
         * Builds a concrete route for the given [playlistId].
         */
        fun createRoute(playlistId: String): String = "channels/$playlistId"
    }

    /**
     * Xtream/M3U playlist details with tabs. Path parameter: [ARG_PLAYLIST_ID].
     */
    data object PlaylistDetail : NavDestination("playlist_detail/{playlistId}") {
        const val ARG_PLAYLIST_ID: String = "playlistId"

        /**
         * Builds a concrete route for the given [playlistId].
         */
        fun createRoute(playlistId: String): String = "playlist_detail/$playlistId"
    }

    /**
     * Per-playlist playback header settings.
     */
    data object PlaylistSettings : NavDestination("playlist_settings/{playlistId}") {
        const val ARG_PLAYLIST_ID: String = "playlistId"

        /**
         * Builds a concrete route for the given [playlistId].
         */
        fun createRoute(playlistId: String): String = "playlist_settings/$playlistId"
    }

    /**
     * Full-screen playback with encoded URL and optional header overrides.
     */
    data object Player : NavDestination(
        "player/{streamUrl}?userAgent={userAgent}&referer={referer}&playlistId={playlistId}&liveChannelId={liveChannelId}"
    ) {
        const val ARG_STREAM_URL: String = "streamUrl"
        const val ARG_USER_AGENT: String = "userAgent"
        const val ARG_REFERER: String = "referer"
        const val ARG_PLAYLIST_ID: String = "playlistId"
        const val ARG_LIVE_CHANNEL_ID: String = "liveChannelId"

        /**
         * Builds a route with URL-encoded arguments for safe navigation transport.
         */
        fun createRoute(
            streamUrl: String,
            userAgent: String = "",
            referer: String? = null,
            playlistId: String? = null,
            liveChannelId: Long? = null
        ): String {
            val encodedUrl = Uri.encode(streamUrl)
            val encodedUa = Uri.encode(userAgent)
            val encodedReferer = Uri.encode(referer.orEmpty())
            val encodedPlaylistId = Uri.encode(playlistId.orEmpty())
            val encodedChannelId = liveChannelId?.toString().orEmpty()
            return "player/$encodedUrl?userAgent=$encodedUa&referer=$encodedReferer&playlistId=$encodedPlaylistId&liveChannelId=$encodedChannelId"
        }
    }

    /**
     * Import screen for adding a playlist via URL or file.
     */
    data object Import : NavDestination("import")
}
