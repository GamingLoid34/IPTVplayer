@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.valladares.iptvplayer.feature.channels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.stickyHeader
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.data.playlist.model.Channel
import com.valladares.iptvplayer.ui.theme.IPTVPlayerTheme

/**
 * Presents channels grouped by group title, with optional logos from [Channel.logoUrl].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    onChannelClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChannelListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ChannelListScreenContent(
        uiState = uiState,
        onChannelClick = onChannelClick,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelListScreenContent(
    uiState: ChannelListUiState,
    onChannelClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.channels_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                ChannelListUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                ChannelListUiState.Empty -> {
                    Text(
                        text = stringResource(R.string.channels_empty),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                is ChannelListUiState.Content -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        uiState.groupedChannels.forEach { (groupTitle, channels) ->
                            stickyHeader {
                                Text(
                                    text = groupTitle,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme
                                                .surfaceContainerHighest
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            items(
                                items = channels,
                                key = { it.id }
                            ) { channel ->
                                ChannelRow(
                                    channel = channel,
                                    onClick = { onChannelClick(channel.streamUrl) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelRow(
    channel: Channel,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!channel.logoUrl.isNullOrBlank()) {
            AsyncImage(
                model = channel.logoUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Tv,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = channel.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val groupLabel = channel.groupTitle.orEmpty()
            if (groupLabel.isNotEmpty()) {
                Text(
                    text = groupLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Channels Loading")
@Composable
private fun ChannelListLoadingPreview() {
    IPTVPlayerTheme {
        ChannelListScreenContent(
            uiState = ChannelListUiState.Loading,
            onChannelClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Channels Empty")
@Composable
private fun ChannelListEmptyPreview() {
    IPTVPlayerTheme {
        ChannelListScreenContent(
            uiState = ChannelListUiState.Empty,
            onChannelClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Channels Content")
@Composable
private fun ChannelListContentPreview() {
    val ch = Channel(
        id = "c1",
        name = "SVT 1",
        streamUrl = "https://example.com/svt1.m3u8",
        logoUrl = null,
        groupTitle = "Sverige"
    )
    val map = mapOf("Sverige" to listOf(ch))
    IPTVPlayerTheme {
        ChannelListScreenContent(
            uiState = ChannelListUiState.Content(map),
            onChannelClick = {},
            onBack = {}
        )
    }
}
