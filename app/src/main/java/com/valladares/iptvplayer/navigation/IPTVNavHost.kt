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
import com.valladares.iptvplayer.feature.channels.ChannelListScreen
import com.valladares.iptvplayer.feature.home.HomeScreen
import com.valladares.iptvplayer.feature.player.PlayerScreen

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
                onImportClick = { /* Fas 2c: import flow */ },
                onPlaylistClick = { playlistId: String ->
                    navController.navigate(
                        NavDestination.Channels.createRoute(playlistId)
                    )
                },
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
                        NavDestination.Player.createRoute(streamUrl)
                    )
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.fillMaxSize()
            )
        }
        composable(
            route = NavDestination.Player.route,
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("streamUrl").orEmpty()
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
