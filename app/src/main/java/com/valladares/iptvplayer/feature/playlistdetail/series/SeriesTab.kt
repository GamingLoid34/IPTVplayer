package com.valladares.iptvplayer.feature.playlistdetail.series

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.valladares.iptvplayer.data.xtream.model.Episode
import com.valladares.iptvplayer.data.xtream.model.Series
import com.valladares.iptvplayer.feature.playlistdetail.live.PlaybackRequest
import kotlinx.coroutines.launch

/**
 * Series tab that lists series by category and plays the first available synced episode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesTab(
    playlistId: String,
    onItemClick: (PlaybackRequest) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SeriesTabViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var selectedSeries by androidx.compose.runtime.remember { mutableStateOf<Series?>(null) }
    var selectedSeriesEpisodes by androidx.compose.runtime.remember { mutableStateOf<List<Episode>>(emptyList()) }
    var loadingEpisodesForId by androidx.compose.runtime.remember { mutableStateOf<Long?>(null) }
    var expandedSeasons by androidx.compose.runtime.remember { mutableStateOf<Set<Int>>(emptySet()) }

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
            placeholder = { Text("Sök serier...") },
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
            SeriesTabUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            SeriesTabUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Inga serier att visa")
            }
            is SeriesTabUiState.Content -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.items, key = { it.listKey }) { item ->
                    when (item) {
                        is SeriesListItem.Header -> Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = item.title,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }

                        is SeriesListItem.Row -> ListItem(
                            modifier = Modifier.clickable {
                                scope.launch {
                                    loadingEpisodesForId = item.series.id
                                    val episodes = viewModel.getEpisodes(item.series)
                                    loadingEpisodesForId = null
                                    if (episodes.isEmpty()) {
                                        viewModel.buildFirstEpisodeRequest(item.series)?.let(onItemClick)
                                    } else {
                                        selectedSeries = item.series
                                        selectedSeriesEpisodes = episodes
                                    }
                                }
                            },
                            headlineContent = {
                                Text(item.series.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                if (loadingEpisodesForId == item.series.id) {
                                    Text("Laddar avsnitt...")
                                }
                            },
                            leadingContent = {
                                if (item.series.cover.isNullOrBlank()) {
                                    Icon(Icons.Filled.LiveTv, contentDescription = null, modifier = Modifier.size(40.dp))
                                } else {
                                    AsyncImage(
                                        model = item.series.cover,
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

    val dialogSeries = selectedSeries
    LaunchedEffect(dialogSeries?.id, selectedSeriesEpisodes) {
        if (dialogSeries == null || selectedSeriesEpisodes.isEmpty()) {
            expandedSeasons = emptySet()
        } else {
            val firstSeason = selectedSeriesEpisodes
                .map { it.seasonNumber ?: 0 }
                .distinct()
                .sorted()
                .firstOrNull()
            expandedSeasons = firstSeason?.let { setOf(it) } ?: emptySet()
        }
    }
    if (dialogSeries != null) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedSeries = null
                selectedSeriesEpisodes = emptyList()
                expandedSeasons = emptySet()
            }
        ) {
            Text(
                text = dialogSeries.name,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                val episodesBySeason = selectedSeriesEpisodes.groupBy { it.seasonNumber ?: 0 }
                    .toSortedMap()
                episodesBySeason.forEach { (season, episodes) ->
                    item(key = "season_$season") {
                        val isExpanded = season in expandedSeasons
                        ListItem(
                            modifier = Modifier.clickable {
                                expandedSeasons = if (isExpanded) {
                                    expandedSeasons - season
                                } else {
                                    expandedSeasons + season
                                }
                            },
                            headlineContent = {
                                Text(
                                    text = if (season > 0) "Säsong $season" else "Okänd säsong",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            supportingContent = {
                                Text("${episodes.size} avsnitt")
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                    if (season in expandedSeasons) {
                        items(
                            items = episodes.sortedBy { it.episodeNumber ?: Int.MAX_VALUE },
                            key = { it.id }
                        ) { episode ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            ) {
                                ListItem(
                                    modifier = Modifier.clickable {
                                        scope.launch {
                                            viewModel.buildEpisodeRequest(dialogSeries, episode)?.let(onItemClick)
                                            selectedSeries = null
                                            selectedSeriesEpisodes = emptyList()
                                            expandedSeasons = emptySet()
                                        }
                                    },
                                    headlineContent = {
                                        val epNum = episode.episodeNumber ?: 0
                                        val episodeCode = if (epNum > 0) "Avsnitt $epNum" else "Avsnitt ?"
                                        Text("$episodeCode - ${episode.title}")
                                    },
                                    supportingContent = {
                                        episode.plot?.takeIf { it.isNotBlank() }?.let { plot ->
                                            Text(
                                                text = plot,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
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
    }
}
