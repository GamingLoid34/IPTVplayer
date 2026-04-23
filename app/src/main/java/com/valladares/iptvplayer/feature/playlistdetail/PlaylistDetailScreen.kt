package com.valladares.iptvplayer.feature.playlistdetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.feature.playlistdetail.live.LiveChannelsTab
import com.valladares.iptvplayer.feature.playlistdetail.live.LiveChannelsViewModel
import com.valladares.iptvplayer.feature.playlistdetail.live.PlaybackRequest
import com.valladares.iptvplayer.feature.playlistdetail.series.SeriesTab
import com.valladares.iptvplayer.feature.playlistdetail.vod.VodTab
import kotlinx.coroutines.launch

/**
 * Tabbed view for a playlist: live channels (implemented), VOD and series (placeholders).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    onOpenSettings: (String) -> Unit,
    onChannelClick: (PlaybackRequest) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val playlistName by viewModel.playlistName.collectAsStateWithLifecycle()
    val liveViewModel: LiveChannelsViewModel = hiltViewModel()
    val searchQuery by liveViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCountry by liveViewModel.selectedCountry.collectAsStateWithLifecycle()
    val availableCountries by liveViewModel.availableCountries.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    var isSearchActive by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val tabLive = stringResource(R.string.playlist_detail_tab_live)
    val tabVod = stringResource(R.string.playlist_detail_tab_vod)
    val tabSeries = stringResource(R.string.playlist_detail_tab_series)
    val tabs = listOf(tabLive, tabVod, tabSeries)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive && pagerState.currentPage == 0) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { liveViewModel.onSearchQueryChange(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(text = stringResource(R.string.live_search_placeholder)) },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        liveViewModel.onSearchQueryChange("")
                                        isSearchActive = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(R.string.live_search_clear)
                                    )
                                }
                            }
                        )
                    } else {
                        Text(text = playlistName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (pagerState.currentPage == 0) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = stringResource(R.string.live_search_placeholder)
                            )
                        }
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = stringResource(R.string.live_filter_title)
                            )
                        }
                    }
                    IconButton(onClick = { onOpenSettings(viewModel.playlistId) }) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(R.string.playlist_settings_save)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index: Int, title: String ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = title) }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page: Int ->
                when (page) {
                    0 -> LiveChannelsTab(
                        playlistId = viewModel.playlistId,
                        onChannelClick = onChannelClick,
                        onClearFilters = {
                            liveViewModel.onSearchQueryChange("")
                            liveViewModel.onCountryFilterChange(null)
                            isSearchActive = false
                        },
                        viewModel = liveViewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> VodTab(
                        playlistId = viewModel.playlistId,
                        onItemClick = onChannelClick,
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> SeriesTab(
                        playlistId = viewModel.playlistId,
                        onItemClick = onChannelClick,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showFilterSheet && pagerState.currentPage == 0) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.live_filter_title),
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    CountryFilterChip(
                        label = stringResource(R.string.live_filter_country_all),
                        selected = selectedCountry == null,
                        onClick = { liveViewModel.onCountryFilterChange(null) }
                    )
                    CountryFilterChip(
                        label = stringResource(R.string.live_filter_country_unknown),
                        selected = selectedCountry == LiveChannelsViewModel.COUNTRY_UNKNOWN,
                        onClick = { liveViewModel.onCountryFilterChange(LiveChannelsViewModel.COUNTRY_UNKNOWN) }
                    )
                    availableCountries.forEach { code ->
                        CountryFilterChip(
                            label = "${flagEmojiFor(code)} $code",
                            selected = selectedCountry == code,
                            onClick = { liveViewModel.onCountryFilterChange(code) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ComingSoonTab(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CountryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
    )
}

private fun flagEmojiFor(countryCode: String): String {
    val normalized = countryCode.uppercase().take(2)
    if (normalized.length != 2 || !normalized.all { it in 'A'..'Z' }) {
        return ""
    }
    val base = 0x1F1E6
    val first = base + (normalized[0] - 'A')
    val second = base + (normalized[1] - 'A')
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
