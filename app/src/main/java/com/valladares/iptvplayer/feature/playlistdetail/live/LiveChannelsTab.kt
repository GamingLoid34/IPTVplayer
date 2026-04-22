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
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
    onChannelClick: (String) -> Unit,
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
                Text(
                    text = stringResource(R.string.live_tab_empty),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        is LiveChannelsUiState.Content -> {
            LiveChannelsList(
                items = state.items,
                onChannelRowClick = { channel: LiveChannel ->
                    scope.launch {
                        val url = viewModel.buildStreamUrl(channel)
                        if (url != null) {
                            onChannelClick(url)
                        }
                    }
                },
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LiveChannelsList(
    items: List<LiveChannelsListItem>,
    onChannelRowClick: (LiveChannel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = { it.listKey }
        ) { item: LiveChannelsListItem ->
            when (item) {
                is LiveChannelsListItem.Header -> GroupHeader(title = item.title)
                is LiveChannelsListItem.Row -> ChannelRow(
                    channel = item.channel,
                    onClick = { onChannelRowClick(item.channel) }
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChannelRow(
    channel: LiveChannel,
    onClick: () -> Unit,
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
