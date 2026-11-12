package com.valladares.iptvplayer.feature.playlistdetail.live

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import kotlinx.coroutines.launch

/**
 * Live tab: grouped Xtream live channels, navigation to the shared player on row tap.
 */
@Composable
fun LiveChannelsTab(
    playlistId: String,
    onChannelClick: (PlaybackRequest) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveChannelsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(playlistId) {
        viewModel.setPlaylistId(playlistId)
    }

    when (val state = uiState) {
        is LiveChannelsUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is LiveChannelsUiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyContent(
                    message = state.message,
                    showClearFilters = state.showClearFilters,
                    onClearFilters = onClearFilters
                )
            }
        }
        is LiveChannelsUiState.Content -> {
            LiveChannelsList(
                items = state.items,
                onChannelRowClick = { channel: LiveChannel ->
                    scope.launch {
                        val request = viewModel.buildPlaybackRequest(channel)
                        if (request != null) {
                            onChannelClick(request)
                        }
                    }
                },
                onToggleFavorite = { channelId ->
                    viewModel.onToggleFavorite(channelId)
                },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun EmptyContent(
    message: String,
    showClearFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
            if (showClearFilters) {
                Button(
                    onClick = onClearFilters,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(text = stringResource(R.string.live_clear_filters))
                }
            }
        }
    }
}

@Composable
private fun LiveChannelsList(
    items: List<LiveChannelsListItem>,
    onChannelRowClick: (LiveChannel) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = { it.listKey }
        ) { item: LiveChannelsListItem ->
            when (item) {
                is LiveChannelsListItem.Header -> GroupHeader(
                    title = item.title,
                    isPseudo = item.isPseudo
                )
                is LiveChannelsListItem.Row -> ChannelRow(
                    channel = item.channel,
                    isFavorite = item.isFavorite,
                    onClick = { onChannelRowClick(item.channel) },
                    onToggleFavorite = { onToggleFavorite(item.channel.id) }
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(
    title: String,
    isPseudo: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (isPseudo) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = if (isPseudo) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun ChannelRow(
    channel: LiveChannel,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = channel.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = null,
        trailingContent = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) {
                        Icons.Filled.Star
                    } else {
                        Icons.Outlined.StarOutline
                    },
                    contentDescription = if (isFavorite) {
                        stringResource(R.string.live_toggle_favorite_remove)
                    } else {
                        stringResource(R.string.live_toggle_favorite_add)
                    }
                )
            }
        },
        leadingContent = {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Tv,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                if (!channel.streamIcon.isNullOrBlank()) {
                    AsyncImage(
                        model = channel.streamIcon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    )
}
