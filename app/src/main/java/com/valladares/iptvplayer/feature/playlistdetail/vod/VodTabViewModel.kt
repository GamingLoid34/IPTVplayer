package com.valladares.iptvplayer.feature.playlistdetail.vod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.valladares.iptvplayer.core.player.buildSubtitleConfiguration
import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.model.PlaybackHeaders
import com.valladares.iptvplayer.data.xtream.XtreamSyncService
import com.valladares.iptvplayer.data.xtream.VodRepository
import com.valladares.iptvplayer.data.xtream.api.XtreamApiFactory
import com.valladares.iptvplayer.data.xtream.model.VodItem
import com.valladares.iptvplayer.data.xtream.model.XtreamCategory
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import com.valladares.iptvplayer.data.xtream.model.XtreamStreamUrls
import com.valladares.iptvplayer.feature.playlistdetail.live.PlaybackRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes grouped VOD data and playback-request building for the movies tab.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class VodTabViewModel @Inject constructor(
    private val vodRepository: VodRepository,
    private val playlistRepository: PlaylistRepository,
    private val xtreamSyncService: XtreamSyncService,
    private val xtreamStreamUrls: XtreamStreamUrls,
    private val xtreamApiFactory: XtreamApiFactory
) : ViewModel() {
    private val playlistIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    private val syncInFlightPlaylists: MutableSet<String> = mutableSetOf()
    private val isSyncing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    val uiState: StateFlow<VodTabUiState> = playlistIdFlow.flatMapLatest { playlistId ->
        if (playlistId == null) {
            flowOf(VodTabUiState.Loading)
        } else {
            combine(
                searchQuery,
                isSyncing,
                vodRepository.observeCategories(playlistId),
                vodRepository.observeItems(playlistId)
            ) { query, syncing, categories, items ->
                val filtered = if (query.isBlank()) {
                    items
                } else {
                    items.filter { it.name.contains(query, ignoreCase = true) }
                }
                if (filtered.isEmpty() && syncing) {
                    VodTabUiState.Loading
                } else if (filtered.isEmpty()) {
                    VodTabUiState.Empty
                } else {
                    VodTabUiState.Content(buildItems(categories, filtered))
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VodTabUiState.Loading
    )

    fun setPlaylistId(playlistId: String) {
        playlistIdFlow.value = playlistId
        if (syncInFlightPlaylists.add(playlistId)) {
            viewModelScope.launch {
                try {
                    val credentials = playlistRepository.getXtreamCredentials(playlistId) ?: return@launch
                    isSyncing.value = true
                    xtreamSyncService.syncVod(playlistId, credentials)
                } finally {
                    isSyncing.value = false
                    syncInFlightPlaylists.remove(playlistId)
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    suspend fun buildPlaybackRequest(item: VodItem): PlaybackRequest? {
        val credentials = playlistRepository.getXtreamCredentials(item.playlistId) ?: return null
        val headers: PlaybackHeaders = playlistRepository.getPlaybackHeaders(item.playlistId)
        val url = xtreamStreamUrls.vodStreamUrl(
            credentials = credentials,
            streamId = item.streamId,
            extension = item.containerExtension ?: "mp4"
        )

        val subtitles = try {
            val xtreamApi = xtreamApiFactory.create(credentials.serverUrl)
            val vodInfo = xtreamApi.getVodInfo(
                username = credentials.username,
                password = credentials.password,
                vodId = item.streamId
            )
            vodInfo.subtitles?.mapNotNull { subtitle ->
                buildSubtitleConfiguration(
                    id = subtitle.id,
                    label = subtitle.lang,
                    url = subtitle.url,
                    language = subtitle.lang
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("VodTabViewModel", "Failed to fetch VOD info for subtitles", e)
            null
        }

        return PlaybackRequest(
            url = url,
            userAgent = headers.userAgent,
            referer = headers.referer,
            playlistId = item.playlistId,
            liveChannelId = null,
            subtitles = subtitles
        )
    }

    private fun buildItems(
        categories: List<XtreamCategory>,
        vodItems: List<VodItem>
    ): List<VodListItem> {
        val result = mutableListOf<VodListItem>()
        val byCategory = vodItems.groupBy { it.categoryExternalId }
        categories.forEach { category ->
            val list = byCategory[category.externalId].orEmpty()
            if (list.isNotEmpty()) {
                result += VodListItem.Header(category.name)
                list.forEach { result += VodListItem.Row(it) }
            }
        }
        val known = categories.map { it.externalId }.toSet()
        val other = vodItems.filter { it.categoryExternalId == null || it.categoryExternalId !in known }
        if (other.isNotEmpty()) {
            result += VodListItem.Header("Övrigt")
            other.forEach { result += VodListItem.Row(it) }
        }
        return result
    }
}

sealed interface VodTabUiState {
    data object Loading : VodTabUiState
    data object Empty : VodTabUiState
    data class Content(val items: List<VodListItem>) : VodTabUiState
}

sealed interface VodListItem {
    val listKey: String

    data class Header(val title: String) : VodListItem {
        override val listKey: String = "header_$title"
    }

    data class Row(val vod: VodItem) : VodListItem {
        override val listKey: String = "vod_${vod.id}"
    }
}
