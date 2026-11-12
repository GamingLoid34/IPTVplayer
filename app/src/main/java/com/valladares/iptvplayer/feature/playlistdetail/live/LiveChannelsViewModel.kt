package com.valladares.iptvplayer.feature.playlistdetail.live

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.model.PlaybackHeaders
import com.valladares.iptvplayer.data.xtream.FavoritesRepository
import com.valladares.iptvplayer.data.xtream.LiveRepository
import com.valladares.iptvplayer.data.xtream.WatchHistoryRepository
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import com.valladares.iptvplayer.data.xtream.model.XtreamStreamUrls
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes live channels for one playlist, grouped by Xtream category for the live tab.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class LiveChannelsViewModel @Inject constructor(
    private val liveRepository: LiveRepository,
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val streamUrls: XtreamStreamUrls,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    companion object {
        const val COUNTRY_UNKNOWN: String = "UNKNOWN"
    }

    private val playlistIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val searchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val selectedCountry: MutableStateFlow<String?> = MutableStateFlow(null)
    private val debouncedSearchQuery = searchQuery
        .debounce(150)
        .map { it.trim() }
        .distinctUntilChanged()

    val availableCountries: StateFlow<List<String>> = playlistIdFlow
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                liveRepository.observeAvailableCountries(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val favoriteIds: StateFlow<Set<Long>> = playlistIdFlow
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptySet())
            } else {
                favoritesRepository.observeFavoriteIds(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptySet()
        )

    val favorites: StateFlow<List<LiveChannel>> = playlistIdFlow
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                favoritesRepository.observeFavorites(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val recentChannels: StateFlow<List<LiveChannel>> = playlistIdFlow
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                watchHistoryRepository.observeRecentChannels(id)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val categoriesFlow = playlistIdFlow
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(emptyList())
            } else {
                liveRepository.observeCategories(id)
            }
        }

    private val channelsFlow = combine(
        playlistIdFlow,
        selectedCountry,
        debouncedSearchQuery
    ) { id, country, query ->
        Triple(id, country, query)
    }.flatMapLatest { (id, country, query) ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            liveRepository.observeChannelsFiltered(
                playlistId = id,
                countryCode = country,
                searchQuery = query.ifBlank { null }
            )
        }
    }.flowOn(Dispatchers.Default)

    private val groupedChannelsFlow = combine(
        categoriesFlow,
        channelsFlow
    ) { categories, channels ->
        GroupedChannels(
            categories = categories,
            byCategory = channels.groupBy { it.categoryExternalId }
        )
    }.flowOn(Dispatchers.Default)

    private val favoritesMetaFlow = combine(
        favorites,
        recentChannels,
        favoriteIds
    ) { fav: List<LiveChannel>, recent: List<LiveChannel>, ids: Set<Long> ->
        Triple(fav, recent, ids)
    }

    val uiState: StateFlow<LiveChannelsUiState> = playlistIdFlow
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(LiveChannelsUiState.Loading)
            } else {
                combine(
                    debouncedSearchQuery,
                    selectedCountry,
                    groupedChannelsFlow,
                    favoritesMetaFlow
                ) { query: String,
                    country: String?,
                    grouped: GroupedChannels,
                    meta: Triple<List<LiveChannel>, List<LiveChannel>, Set<Long>> ->
                    buildUiState(
                        query = query,
                        selectedCountry = country,
                        groupedChannels = grouped,
                        favorites = meta.first,
                        recent = meta.second,
                        favoriteIds = meta.third,
                        otherCategoryLabel = appContext.getString(R.string.live_category_other)
                    )
                }.flowOn(Dispatchers.Default)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LiveChannelsUiState.Loading
        )

    /**
     * Attaches the active playlist; triggers loading of categories and channels.
     */
    fun setPlaylistId(playlistId: String) {
        playlistIdFlow.value = playlistId
    }

    /**
     * Updates search query for channel filtering.
     */
    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    /**
     * Updates selected country filter. Null means "all countries".
     */
    fun onCountryFilterChange(countryCode: String?) {
        selectedCountry.value = countryCode
    }

    /**
     * Toggles favorite status for one channel in current playlist.
     */
    fun onToggleFavorite(channelId: Long) {
        val playlistId = playlistIdFlow.value ?: return
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(playlistId, channelId)
        }
    }

    /**
     * Resolves a playable request for [channel], or null if credentials are missing.
     */
    suspend fun buildPlaybackRequest(channel: LiveChannel): PlaybackRequest? {
        val credentials = playlistRepository.getXtreamCredentials(channel.playlistId) ?: return null
        val url = streamUrls.liveStreamUrl(
            credentials = credentials,
            streamId = channel.streamId
        )
        val headers: PlaybackHeaders = playlistRepository.getPlaybackHeaders(channel.playlistId)
        return PlaybackRequest(
            url = url,
            userAgent = headers.userAgent,
            referer = headers.referer,
            playlistId = channel.playlistId,
            liveChannelId = channel.id
        )
    }

    private fun buildUiState(
        query: String,
        selectedCountry: String?,
        groupedChannels: GroupedChannels,
        favorites: List<LiveChannel>,
        recent: List<LiveChannel>,
        favoriteIds: Set<Long>,
        otherCategoryLabel: String
    ): LiveChannelsUiState {
        val categories = groupedChannels.categories
        val byCategory = groupedChannels.byCategory
        val channels = byCategory.values.flatten()
        if (channels.isEmpty()) {
            val message = when {
                query.isNotBlank() -> appContext.getString(R.string.live_empty_search, query)
                selectedCountry != null -> {
                    val countryLabel = if (selectedCountry == COUNTRY_UNKNOWN) {
                        appContext.getString(R.string.live_filter_country_unknown)
                    } else {
                        selectedCountry
                    }
                    appContext.getString(R.string.live_empty_country, countryLabel)
                }
                else -> appContext.getString(R.string.live_tab_empty)
            }
            return LiveChannelsUiState.Empty(
                message = message,
                showClearFilters = query.isNotBlank() || selectedCountry != null
            )
        }
        val visibleIds = channels.map { it.id }.toSet()
        val items: MutableList<LiveChannelsListItem> = mutableListOf()

        if (query.isBlank() && selectedCountry == null) {
            val favoriteRows = favorites.filter { it.id in visibleIds }
            if (favoriteRows.isNotEmpty()) {
                items.add(
                    LiveChannelsListItem.Header(
                        title = appContext.getString(R.string.live_section_favorites),
                        isPseudo = true
                    )
                )
                favoriteRows.forEach { ch ->
                    items.add(
                        LiveChannelsListItem.Row(
                            channel = ch,
                            categoryName = null,
                            isFavorite = true
                        )
                    )
                }
            }
            val recentRows = recent.filter { it.id in visibleIds && it.id !in favoriteRows.map { row -> row.id }.toSet() }
            if (recentRows.isNotEmpty()) {
                items.add(
                    LiveChannelsListItem.Header(
                        title = appContext.getString(R.string.live_section_recent),
                        isPseudo = true
                    )
                )
                recentRows.forEach { ch ->
                    items.add(
                        LiveChannelsListItem.Row(
                            channel = ch,
                            categoryName = null,
                            isFavorite = ch.id in favoriteIds
                        )
                    )
                }
            }
        }

        val byExternalId: Map<String, XtreamCategory> = categories.associateBy { it.externalId }
        for (category in categories) {
            val inCategory = byCategory[category.externalId]
                .orEmpty()
                .sortedBy { it.name }
            if (inCategory.isNotEmpty()) {
                items.add(
                    LiveChannelsListItem.Header(
                        title = category.name,
                        isPseudo = false
                    )
                )
                for (ch in inCategory) {
                    items.add(
                        LiveChannelsListItem.Row(
                            channel = ch,
                            categoryName = category.name,
                            isFavorite = ch.id in favoriteIds
                        )
                    )
                }
            }
        }
        val knownIds = byExternalId.keys
        val other = channels.filter { ch ->
            val ext = ch.categoryExternalId
            ext == null || ext !in knownIds
        }
            .sortedBy { it.name }
        if (other.isNotEmpty()) {
            items.add(LiveChannelsListItem.Header(title = otherCategoryLabel))
            for (ch in other) {
                items.add(
                    LiveChannelsListItem.Row(
                        channel = ch,
                        categoryName = null,
                        isFavorite = ch.id in favoriteIds
                    )
                )
            }
        }
        return LiveChannelsUiState.Content(items = items)
    }
}

/**
 * Top-level state for the live tab list.
 */
sealed interface LiveChannelsUiState {
    data object Loading : LiveChannelsUiState
    data class Empty(
        val message: String,
        val showClearFilters: Boolean
    ) : LiveChannelsUiState
    data class Content(val items: List<LiveChannelsListItem>) : LiveChannelsUiState
}

/**
 * Flat list entries: category headers and channel rows (avoids nested LazyColumn specials).
 */
sealed interface LiveChannelsListItem {
    val listKey: String

    data class Header(
        val title: String,
        val isPseudo: Boolean = false
    ) : LiveChannelsListItem {
        override val listKey: String = "header_$title"
    }

    data class Row(
        val channel: LiveChannel,
        val categoryName: String?,
        val isFavorite: Boolean
    ) : LiveChannelsListItem {
        override val listKey: String = "channel_${channel.id}"
    }
}

/**
 * Parameters required for playback of one selected live channel.
 */
data class PlaybackRequest(
    val url: String,
    val userAgent: String,
    val referer: String?,
    val playlistId: String,
    val liveChannelId: Long?,
    val subtitles: List<androidx.media3.common.MediaItem.SubtitleConfiguration>? = null
)

private data class GroupedChannels(
    val categories: List<XtreamCategory>,
    val byCategory: Map<String?, List<LiveChannel>>
)
