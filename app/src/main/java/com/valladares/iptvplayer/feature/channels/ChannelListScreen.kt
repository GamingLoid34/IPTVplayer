package com.valladares.iptvplayer.feature.channels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Internt sealed-interface för att representera alternerande
 * header- och kanalrader i en platt lista. Detta undviker nästlade
 * forEach-lambdas inuti LazyColumn där scope-resolution blir bräckligt.
 */
private sealed interface ChannelListItem {
    val listKey: String

    data class Header(val title: String) : ChannelListItem {
        override val listKey: String = "header_$title"
    }

    data class Row(val channel: Channel) : ChannelListItem {
        override val listKey: String = "channel_${channel.id}"
    }
}

private fun Map<String, List<Channel>>.toListItems(): List<ChannelListItem> =
    flatMap { (group, channels) ->
        buildList {
            add(ChannelListItem.Header(group))
            channels.forEach { add(ChannelListItem.Row(it)) }
        }
    }

/**
 * Screen that renders channels for one playlist and routes row clicks to playback.
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
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = androidx.compose.ui.res.stringResource(R.string.channels_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = androidx.compose.ui.res.stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            ChannelListUiState.Loading -> {
                LoadingContent(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }

            ChannelListUiState.Empty -> {
                EmptyContent(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }

            is ChannelListUiState.Content -> {
                ChannelListContent(
                    items = state.groupedChannels.toListItems(),
                    onChannelClick = onChannelClick,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = androidx.compose.ui.res.stringResource(R.string.channels_empty))
    }
}

@Composable
private fun ChannelListContent(
    items: List<ChannelListItem>,
    onChannelClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = { it.listKey }
        ) { item ->
            when (item) {
                is ChannelListItem.Header -> GroupHeader(title = item.title)
                is ChannelListItem.Row -> ChannelRow(
                    channel = item.channel,
                    onClick = { onChannelClick(item.channel.streamUrl) }
                )
            }
        }
    }
}

@Composable
private fun GroupHeader(title: String, modifier: Modifier = Modifier) {
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
    channel: Channel,
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
        supportingContent = channel.groupTitle?.let { group ->
            {
                Text(
                    text = group,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        leadingContent = {
            if (channel.logoUrl != null) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Tv,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
        },
        tonalElevation = 0.dp
    )
}

@Preview(showBackground = true, name = "Channels Loading")
@Composable
private fun ChannelListLoadingPreview() {
    IPTVPlayerTheme {
        LoadingContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Channels Empty")
@Composable
private fun ChannelListEmptyPreview() {
    IPTVPlayerTheme {
        EmptyContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(showBackground = true, name = "Channels Content")
@Composable
private fun ChannelListContentPreview() {
    val fake = listOf(
        ChannelListItem.Header("Sport"),
        ChannelListItem.Row(
            Channel(
                id = "1",
                name = "Kanal 1",
                streamUrl = "http://x",
                logoUrl = null,
                groupTitle = "Sport"
            )
        ),
        ChannelListItem.Row(
            Channel(
                id = "2",
                name = "Kanal 2",
                streamUrl = "http://y",
                logoUrl = null,
                groupTitle = "Sport"
            )
        ),
        ChannelListItem.Header("Nyheter"),
        ChannelListItem.Row(
            Channel(
                id = "3",
                name = "Kanal 3",
                streamUrl = "http://z",
                logoUrl = null,
                groupTitle = "Nyheter"
            )
        )
    )
    IPTVPlayerTheme {
        ChannelListContent(
            items = fake,
            onChannelClick = {}
        )
    }
}
