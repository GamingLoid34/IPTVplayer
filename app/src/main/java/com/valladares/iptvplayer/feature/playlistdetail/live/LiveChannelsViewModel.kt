package com.valladares.iptvplayer.feature.playlistdetail.live

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.xtream.LiveRepository
import com.valladares.iptvplayer.data.xtream.model.LiveChannel
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import com.valladares.iptvplayer.data.xtream.model.XtreamStreamUrls
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

/**
 * Exposes live channels for one playlist, grouped by Xtream category for the live tab.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LiveChannelsViewModel @Inject constructor(
    private val liveRepository: LiveRepository,
    private val playlistRepository: PlaylistRepository,
    private val streamUrls: XtreamStreamUrls,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val playlistIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)

    val uiState: StateFlow<LiveChannelsUiState> = playlistIdFlow
        .flatMapLatest { id: String? ->
            if (id == null) {
                flowOf(LiveChannelsUiState.Loading)
            } else {
                combine(
                    liveRepository.observeCategories(id),
                    liveRepository.observeChannels(id)
                ) { categories: List<XtreamCategory>, channels: List<LiveChannel> ->
                    buildUiState(
                        categories = categories,
                        channels = channels,
                        otherCategoryLabel = appContext.getString(R.string.live_category_other)
                    )
                }
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
     * Resolves a playable Xtream live URL for [channel], or null if credentials are missing.
     */
    suspend fun buildStreamUrl(channel: LiveChannel): String? {
        val credentials = playlistRepository.getXtreamCredentials(channel.playlistId) ?: return null
        return streamUrls.liveStreamUrl(
            credentials = credentials,
            streamId = channel.streamId
        )
    }

    private fun buildUiState(
        categories: List<XtreamCategory>,
        channels: List<LiveChannel>,
        otherCategoryLabel: String
    ): LiveChannelsUiState {
        if (channels.isEmpty()) {
            return LiveChannelsUiState.Empty
        }
        val byExternalId = categories.associateBy { it.externalId }
        val items = mutableListOf<LiveChannelsListItem>()
        for (category in categories) {
            val inCategory = channels
                .filter { it.categoryExternalId == category.externalId }
                .sortedBy { it.name }
            if (inCategory.isNotEmpty()) {
                items.add(LiveChannelsListItem.Header(title = category.name))
                for (ch in inCategory) {
                    items.add(
                        LiveChannelsListItem.Row(
                            channel = ch,
                            categoryName = category.name
                        )
                    )
                }
            }
        }
        val knownIds = byExternalId.keys
        val other = channels
            .filter { ch ->
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
                        categoryName = null
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
    data object Empty : LiveChannelsUiState
    data class Content(val items: List<LiveChannelsListItem>) : LiveChannelsUiState
}

/**
 * Flat list entries: category headers and channel rows (avoids nested LazyColumn specials).
 */
sealed interface LiveChannelsListItem {
    val listKey: String

    data class Header(val title: String) : LiveChannelsListItem {
        override val listKey: String = "header_$title"
    }

    data class Row(
        val channel: LiveChannel,
        val categoryName: String?
    ) : LiveChannelsListItem {
        override val listKey: String = "channel_${channel.id}"
    }
}
