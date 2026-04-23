package com.valladares.iptvplayer.feature.playlistdetail.vod

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.valladares.iptvplayer.feature.playlistdetail.live.PlaybackRequest
import kotlinx.coroutines.launch

/**
 * Movies tab that shows grouped VOD items and routes item taps to player navigation.
 */
@Composable
fun VodTab(
    playlistId: String,
    onItemClick: (PlaybackRequest) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VodTabViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(playlistId) {
        viewModel.setPlaylistId(playlistId)
    }

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            singleLine = true,
            placeholder = { Text("Sök filmer...") },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = null)
                    }
                }
            } else {
                null
            }
        )
        when (val state = uiState) {
            VodTabUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            VodTabUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Inga filmer att visa")
            }
            is VodTabUiState.Content -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.items, key = { it.listKey }) { item ->
                    when (item) {
                        is VodListItem.Header -> Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.title,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        is VodListItem.Row -> ListItem(
                            modifier = Modifier.clickable {
                                scope.launch {
                                    viewModel.buildPlaybackRequest(item.vod)?.let(onItemClick)
                                }
                            },
                            headlineContent = {
                                Text(item.vod.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            leadingContent = {
                                if (item.vod.streamIcon.isNullOrBlank()) {
                                    Icon(Icons.Filled.Movie, contentDescription = null, modifier = Modifier.size(40.dp))
                                } else {
                                    AsyncImage(
                                        model = item.vod.streamIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
