package com.valladares.iptvplayer.feature.player

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import com.valladares.iptvplayer.core.common.findActivity
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
    val view = LocalView.current
    val fullscreenEnabled by viewModel.fullscreenEnabled.collectAsStateWithLifecycle()
    val availableSubtitles by viewModel.availableSubtitles.collectAsStateWithLifecycle()
    var showSubtitleDialog by remember { mutableStateOf(false) }

    DisposableEffect(fullscreenEnabled) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        val insetsController = WindowCompat.getInsetsController(window, view)
        val previousBehavior = insetsController.systemBarsBehavior
        if (fullscreenEnabled) {
            insetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
        onDispose {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            insetsController.systemBarsBehavior = previousBehavior
        }
    }
    DisposableEffect(fullscreenEnabled) {
        val activity = view.context.findActivity() ?: return@DisposableEffect onDispose {}
        val previousOrientation = activity.requestedOrientation
        activity.requestedOrientation = if (fullscreenEnabled) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose {
            activity.requestedOrientation = previousOrientation
        }
    }
    DisposableEffect(Unit) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose {}
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    BackHandler(onBack = onBack)

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
                    useController = true
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    subtitleView?.setStyle(
                        CaptionStyleCompat(
                            android.graphics.Color.WHITE,
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                            CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                            android.graphics.Color.BLACK,
                            null
                        )
                    )
                    subtitleView?.setFractionalTextSize(0.06f, true)
                }
            },
            update = { playerView ->
                playerView.player = viewModel.getPlayer()
                playerView.useController = true
                playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                playerView.subtitleView?.setStyle(
                    CaptionStyleCompat(
                        android.graphics.Color.WHITE,
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                        CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW,
                        android.graphics.Color.BLACK,
                        null
                    )
                )
                playerView.subtitleView?.setFractionalTextSize(0.06f, true)
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
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            if (availableSubtitles.isNotEmpty()) {
                Text(
                    text = "${availableSubtitles.size}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(end = 4.dp)
                )
            }
            IconButton(
                onClick = { showSubtitleDialog = true }
            ) {
                Icon(
                    imageVector = if (availableSubtitles.isNotEmpty()) {
                        Icons.Filled.Subtitles
                    } else {
                        Icons.Outlined.Subtitles
                    },
                    contentDescription = "Subtitles",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = { viewModel.setFullscreenEnabled(!fullscreenEnabled) }
            ) {
                Icon(
                    imageVector = if (fullscreenEnabled) {
                        Icons.Filled.FullscreenExit
                    } else {
                        Icons.Filled.Fullscreen
                    },
                    contentDescription = if (fullscreenEnabled) {
                        stringResource(R.string.player_fullscreen_disable)
                    } else {
                        stringResource(R.string.player_fullscreen_enable)
                    },
                    tint = Color.White
                )
            }
        }
    }

    if (showSubtitleDialog) {
        SubtitleSelectionDialog(
            subtitles = availableSubtitles,
            onDismiss = { showSubtitleDialog = false },
            onSubtitleSelected = { subtitleConfig ->
                viewModel.setSubtitleTrack(subtitleConfig)
                showSubtitleDialog = false
            }
        )
    }
}

@Composable
private fun SubtitleSelectionDialog(
    subtitles: List<MediaItem.SubtitleConfiguration>,
    onDismiss: () -> Unit,
    onSubtitleSelected: (MediaItem.SubtitleConfiguration) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = Color.DarkGray
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Subtitles",
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubtitleSelected(MediaItem.SubtitleConfiguration.Builder(Uri.EMPTY).setId("none").setLabel("Off").build()) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Off",
                                color = Color.White
                            )
                        }
                    }
                    items(subtitles) { subtitle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSubtitleSelected(subtitle) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subtitle.label.toString(),
                                color = Color.White
                            )
                        }
                    }
                }
            }
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
