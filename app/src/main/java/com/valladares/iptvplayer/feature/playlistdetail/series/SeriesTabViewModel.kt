package com.valladares.iptvplayer.feature.playlistdetail.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.valladares.iptvplayer.core.player.buildSubtitleConfiguration
import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.model.PlaybackHeaders
import com.valladares.iptvplayer.data.xtream.SeriesRepository
import com.valladares.iptvplayer.data.xtream.XtreamSyncService
import com.valladares.iptvplayer.data.xtream.api.XtreamApiFactory
import com.valladares.iptvplayer.data.xtream.model.Episode
import com.valladares.iptvplayer.data.xtream.model.Series
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Exposes grouped series rows and resolves playable first-episode requests when available.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SeriesTabViewModel @Inject constructor(
    private val seriesRepository: SeriesRepository,
    private val playlistRepository: PlaylistRepository,
    private val xtreamSyncService: XtreamSyncService,
    private val xtreamStreamUrls: XtreamStreamUrls,
    private val xtreamApiFactory: XtreamApiFactory
) : ViewModel() {
    private val playlistIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    private val syncInFlightPlaylists: MutableSet<String> = mutableSetOf()
    private val isSyncing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val episodesSyncInFlightSeriesIds: MutableSet<Long> = mutableSetOf()
    val searchQuery: MutableStateFlow<String> = MutableStateFlow("")

    val uiState: StateFlow<SeriesTabUiState> = playlistIdFlow.flatMapLatest { playlistId ->
        if (playlistId == null) {
            flowOf(SeriesTabUiState.Loading)
        } else {
            combine(
                searchQuery,
                isSyncing,
                seriesRepository.observeCategories(playlistId),
                seriesRepository.observeSeriesList(playlistId)
            ) { query, syncing, categories, list ->
                val filtered = if (query.isBlank()) {
                    list
                } else {
                    list.filter { it.name.contains(query, ignoreCase = true) }
                }
                if (filtered.isEmpty() && syncing) {
                    SeriesTabUiState.Loading
                } else if (filtered.isEmpty()) {
                    SeriesTabUiState.Empty
                } else {
                    SeriesTabUiState.Content(buildItems(categories, filtered))
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SeriesTabUiState.Loading
    )

    fun setPlaylistId(playlistId: String) {
        playlistIdFlow.value = playlistId
        if (syncInFlightPlaylists.add(playlistId)) {
            viewModelScope.launch {
                try {
                    val credentials = playlistRepository.getXtreamCredentials(playlistId) ?: return@launch
                    isSyncing.value = true
                    xtreamSyncService.syncSeries(playlistId, credentials)
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

    suspend fun buildFirstEpisodeRequest(series: Series): PlaybackRequest? {
        val credentials = playlistRepository.getXtreamCredentials(series.playlistId) ?: return null
        var episode: Episode? = seriesRepository.observeEpisodes(series.id).first().firstOrNull()
        if (episode == null) {
            syncEpisodesIfNeeded(series, credentials)
            episode = seriesRepository.observeEpisodes(series.id).first().firstOrNull()
        }
        episode ?: return null
        return buildEpisodeRequest(series, episode, credentials)
    }

    suspend fun getEpisodes(series: Series): List<Episode> {
        val credentials = playlistRepository.getXtreamCredentials(series.playlistId) ?: return emptyList()
        var episodes = seriesRepository.observeEpisodes(series.id).first()
        if (episodes.isEmpty()) {
            syncEpisodesIfNeeded(series, credentials)
            episodes = seriesRepository.observeEpisodes(series.id).first()
        }
        return episodes.sortedWith(
            compareBy<Episode>({ it.seasonNumber ?: Int.MAX_VALUE }, { it.episodeNumber ?: Int.MAX_VALUE }, { it.title })
        )
    }

    suspend fun buildEpisodeRequest(series: Series, episode: Episode): PlaybackRequest? {
        val credentials = playlistRepository.getXtreamCredentials(series.playlistId) ?: return null
        return buildEpisodeRequest(series, episode, credentials)
    }

    private suspend fun buildEpisodeRequest(
        series: Series,
        episode: Episode,
        credentials: com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
    ): PlaybackRequest {
        val headers: PlaybackHeaders = playlistRepository.getPlaybackHeaders(series.playlistId)
        val url = xtreamStreamUrls.episodeStreamUrl(
            credentials = credentials,
            episodeId = episode.externalId,
            extension = episode.containerExtension ?: "mp4"
        )

        val subtitles = try {
            val xtreamApi = xtreamApiFactory.create(credentials.serverUrl)
            val seriesInfo = xtreamApi.getSeriesInfo(
                username = credentials.username,
                password = credentials.password,
                seriesId = series.seriesId
            )
            seriesInfo.extractSubtitleConfigsForEpisode(episode)
        } catch (e: Exception) {
            android.util.Log.e("SeriesTabViewModel", "Failed to fetch series info for subtitles", e)
            null
        }

        return PlaybackRequest(
            url = url,
            userAgent = headers.userAgent,
            referer = headers.referer,
            playlistId = series.playlistId,
            liveChannelId = null,
            subtitles = subtitles
        )
    }

    private suspend fun syncEpisodesIfNeeded(
        series: Series,
        credentials: com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
    ) {
        if (!episodesSyncInFlightSeriesIds.add(series.id)) return
        try {
            xtreamSyncService.syncSeriesEpisodes(
                seriesRowId = series.id,
                seriesId = series.seriesId,
                credentials = credentials
            )
        } finally {
            episodesSyncInFlightSeriesIds.remove(series.id)
        }
    }

    private fun buildItems(
        categories: List<XtreamCategory>,
        list: List<Series>
    ): List<SeriesListItem> {
        val result = mutableListOf<SeriesListItem>()
        val byCategory = list.groupBy { it.categoryExternalId }
        categories.forEach { category ->
            val rows = byCategory[category.externalId].orEmpty()
            if (rows.isNotEmpty()) {
                result += SeriesListItem.Header(category.name)
                rows.forEach { result += SeriesListItem.Row(it) }
            }
        }
        val known = categories.map { it.externalId }.toSet()
        val other = list.filter { it.categoryExternalId == null || it.categoryExternalId !in known }
        if (other.isNotEmpty()) {
            result += SeriesListItem.Header("Övrigt")
            other.forEach { result += SeriesListItem.Row(it) }
        }
        return result
    }
}

private val seriesSubtitleJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

private fun JsonElement.toEpisodeDtoList():
    List<com.valladares.iptvplayer.data.xtream.dto.XtreamEpisodeDto> {
    if (this !is JsonArray) return emptyList()
    return this.mapNotNull { element ->
        runCatching {
            seriesSubtitleJson.decodeFromJsonElement(
                com.valladares.iptvplayer.data.xtream.dto.XtreamEpisodeDto.serializer(),
                element
            )
        }.getOrNull()
    }
}

private fun com.valladares.iptvplayer.data.xtream.dto.XtreamSeriesInfoDto.extractSubtitleConfigsForEpisode(
    episode: Episode
): List<MediaItem.SubtitleConfiguration> {
    val objects = episodes
        ?.values
        ?.flatMap { it.toEpisodeJsonObjects() }
        .orEmpty()
    val matched = objects.firstOrNull { obj ->
        obj.matchesEpisode(episode)
    } ?: objects.firstOrNull { obj ->
        val objEpisodeNum = obj["episode_num"]?.asIntOrNull()
        val objSeason = obj["season"]?.asIntOrNull()
        objEpisodeNum != null &&
            episode.episodeNumber != null &&
            objEpisodeNum == episode.episodeNumber &&
            (objSeason == null || episode.seasonNumber == null || objSeason == episode.seasonNumber)
    } ?: return emptyList()

    return matched.extractSubtitleConfigs()
}

private fun JsonElement.toEpisodeJsonObjects(): List<JsonObject> {
    if (this !is JsonArray) return emptyList()
    return this.mapNotNull { it as? JsonObject }
}

private fun JsonObject.matchesEpisode(episode: Episode): Boolean {
    val idCandidates = listOf("id", "episode_id", "stream_id")
    val normalizedEpisodeId = episode.externalId.trim()
    if (normalizedEpisodeId.isNotEmpty()) {
        val idMatch = idCandidates.any { key ->
            val value = this[key]?.asStringOrNull()?.trim().orEmpty()
            value.isNotEmpty() && value == normalizedEpisodeId
        }
        if (idMatch) return true
    }
    val title = this["title"]?.asStringOrNull()?.trim().orEmpty()
    return title.isNotEmpty() && title.equals(episode.title.trim(), ignoreCase = true)
}

private fun JsonObject.extractSubtitleConfigs(): List<MediaItem.SubtitleConfiguration> {
    val subtitleElements = buildList {
        listOf("subtitles", "subtitle", "subs").forEach { key ->
            this@extractSubtitleConfigs[key]?.let(::add)
        }
        (this@extractSubtitleConfigs["info"] as? JsonObject)?.let { info ->
            listOf("subtitles", "subtitle", "subs").forEach { key ->
                info[key]?.let(::add)
            }
        }
    }
    return subtitleElements
        .flatMap { it.toSubtitleCandidates() }
        .mapNotNull { subtitle ->
            buildSubtitleConfiguration(
                id = subtitle.id,
                label = subtitle.lang,
                url = subtitle.url,
                language = subtitle.lang
            )
        }
        .distinctBy { it.uri.toString() }
}

private fun JsonElement.toSubtitleCandidates():
    List<com.valladares.iptvplayer.data.xtream.dto.XtreamSubtitleDto> {
    return when (this) {
        is JsonArray -> this.flatMap { it.toSubtitleCandidates() }
        is JsonObject -> listOfNotNull(this.toSubtitleCandidateOrNull()).ifEmpty {
            this.values.mapNotNull { child ->
                when (child) {
                    is JsonPrimitive -> {
                        val asUrl = child.content.takeIf {
                            it.startsWith("http://", true) || it.startsWith("https://", true)
                        } ?: return@mapNotNull null
                        com.valladares.iptvplayer.data.xtream.dto.XtreamSubtitleDto(
                            id = null,
                            lang = null,
                            url = asUrl
                        )
                    }
                    is JsonObject -> child.toSubtitleCandidateOrNull()
                    else -> null
                }
            }
        }
        is JsonPrimitive -> {
            val content = content
            if (content.isBlank()) return emptyList()
            val parsed = runCatching {
                seriesSubtitleJson.parseToJsonElement(content)
            }.getOrNull()
            parsed?.toSubtitleCandidates() ?: listOf(
                com.valladares.iptvplayer.data.xtream.dto.XtreamSubtitleDto(
                    id = null,
                    lang = null,
                    url = content.takeIf {
                        it.startsWith("http://", true) || it.startsWith("https://", true)
                    }
                )
            ).filter { !it.url.isNullOrBlank() }
        }
        else -> emptyList()
    }
}

private fun JsonObject.toSubtitleCandidateOrNull():
    com.valladares.iptvplayer.data.xtream.dto.XtreamSubtitleDto? {
    val id = this["id"]?.asStringOrNull()
    val lang = this["lang"]?.asStringOrNull()
        ?: this["language"]?.asStringOrNull()
        ?: this["label"]?.asStringOrNull()
    val url = this["url"]?.asStringOrNull()
        ?: this["src"]?.asStringOrNull()
        ?: this["file"]?.asStringOrNull()
        ?: this["link"]?.asStringOrNull()
    return if (url.isNullOrBlank()) null else {
        com.valladares.iptvplayer.data.xtream.dto.XtreamSubtitleDto(
            id = id,
            lang = lang,
            url = url
        )
    }
}

private fun JsonElement.asStringOrNull(): String? = (this as? JsonPrimitive)?.content

private fun JsonElement.asIntOrNull(): Int? =
    (this as? JsonPrimitive)?.content?.toIntOrNull()

sealed interface SeriesTabUiState {
    data object Loading : SeriesTabUiState
    data object Empty : SeriesTabUiState
    data class Content(val items: List<SeriesListItem>) : SeriesTabUiState
}

sealed interface SeriesListItem {
    val listKey: String

    data class Header(val title: String) : SeriesListItem {
        override val listKey: String = "header_$title"
    }

    data class Row(val series: Series) : SeriesListItem {
        override val listKey: String = "series_${series.id}"
    }
}
