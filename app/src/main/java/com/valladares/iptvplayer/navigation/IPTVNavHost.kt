package com.valladares.iptvplayer.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.valladares.iptvplayer.core.common.AppConstants
import com.valladares.iptvplayer.feature.channels.ChannelListScreen
import com.valladares.iptvplayer.feature.home.HomeScreen
import com.valladares.iptvplayer.feature.importplaylist.ImportPlaylistScreen
import com.valladares.iptvplayer.feature.player.PlayerScreen
import com.valladares.iptvplayer.feature.playlistdetail.PlaylistDetailScreen
import com.valladares.iptvplayer.feature.playlistsettings.PlaylistSettingsScreen

/**
 * Root navigation graph: home → channels → player.
 */
@Composable
fun IPTVNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavDestination.Home.route,
        modifier = modifier
    ) {
        composable(NavDestination.Home.route) {
            HomeScreen(
                onImportClick = { navController.navigate(NavDestination.Import.route) },
                onPlaylistClick = { playlistId: String ->
                    navController.navigate(
                        NavDestination.PlaylistDetail.createRoute(playlistId)
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(NavDestination.Import.route) {
            ImportPlaylistScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = NavDestination.PlaylistDetail.route,
            arguments = listOf(
                navArgument(NavDestination.PlaylistDetail.ARG_PLAYLIST_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            PlaylistDetailScreen(
                onBack = { navController.popBackStack() },
                onOpenSettings = { playlistId ->
                    navController.navigate(
                        NavDestination.PlaylistSettings.createRoute(playlistId)
                    )
                },
                onChannelClick = { request ->
                    navController.navigate(
                        NavDestination.Player.createRoute(
                            streamUrl = request.url,
                            userAgent = request.userAgent,
                            referer = request.referer,
                            playlistId = request.playlistId,
                            liveChannelId = request.liveChannelId,
                            subtitles = request.subtitles
                        )
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = NavDestination.PlaylistSettings.route,
            arguments = listOf(
                navArgument(NavDestination.PlaylistSettings.ARG_PLAYLIST_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            PlaylistSettingsScreen(
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = NavDestination.Channels.route,
            arguments = listOf(
                navArgument("playlistId") { type = NavType.StringType }
            )
        ) {
            ChannelListScreen(
                onChannelClick = { streamUrl: String ->
                    navController.navigate(
                        NavDestination.Player.createRoute(
                            streamUrl = streamUrl,
                            userAgent = AppConstants.DEFAULT_USER_AGENT
                        )
                    )
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = NavDestination.Player.route,
            arguments = listOf(
                navArgument(NavDestination.Player.ARG_STREAM_URL) { type = NavType.StringType },
                navArgument(NavDestination.Player.ARG_USER_AGENT) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(NavDestination.Player.ARG_REFERER) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(NavDestination.Player.ARG_PLAYLIST_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(NavDestination.Player.ARG_LIVE_CHANNEL_ID) {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument(NavDestination.Player.ARG_SUBTITLES) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments
                ?.getString(NavDestination.Player.ARG_STREAM_URL)
                .orEmpty()
            val decoded = if (encoded.isNotEmpty()) {
                Uri.decode(encoded)
            } else {
                ""
            }
            PlayerScreen(
                streamUrl = decoded,
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
