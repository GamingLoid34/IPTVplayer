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
     * Full-screen playback. Path parameter: percent-encoded [streamUrl].
     */
    data object Player : NavDestination("player/{streamUrl}") {
        /**
         * Builds a route with [Uri.encode] so slashes and colons in the URL are safe in a path.
         */
        fun createRoute(streamUrl: String): String =
            "player/${Uri.encode(streamUrl)}"
    }

    /**
     * Import screen for adding a playlist via URL or file.
     */
    data object Import : NavDestination("import")
}
