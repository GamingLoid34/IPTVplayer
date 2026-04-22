package com.valladares.iptvplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView

/**
 * Fullscreen player screen that renders Media3 [PlayerView] in Compose.
 */
@Composable
fun PlayerScreen(modifier: Modifier = Modifier) {
    val viewModel: PlayerViewModel = hiltViewModel()

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.release()
        }
    }

    AndroidView(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        factory = { context ->
            PlayerView(context).apply {
                player = viewModel.player
            }
        },
        update = { playerView ->
            playerView.player = viewModel.player
        }
    )
}
