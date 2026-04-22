package com.valladares.iptvplayer.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.ui.theme.IPTVPlayerTheme

/**
 * Fullscreen player: [streamUrl] matches navigation args; actual playback is started in [PlayerViewModel].
 */
@Composable
fun PlayerScreen(
    @Suppress("UNUSED_PARAMETER")
    streamUrl: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    player = viewModel.getPlayer()
                }
            },
            update = { playerView ->
                playerView.player = viewModel.getPlayer()
            }
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.player_back),
                tint = Color.White
            )
        }
    }
}

@Preview(name = "Player (shell)")
@Composable
private fun PlayerScreenPreview() {
    IPTVPlayerTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                tint = Color.White
            )
        }
    }
}
