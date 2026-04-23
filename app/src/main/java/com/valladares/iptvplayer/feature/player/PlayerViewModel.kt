package com.valladares.iptvplayer.feature.player

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.media3.exoplayer.ExoPlayer
import com.valladares.iptvplayer.core.common.AppConstants
import com.valladares.iptvplayer.core.player.buildSubtitleConfiguration
import com.valladares.iptvplayer.core.player.PlayerManager
import com.valladares.iptvplayer.core.preferences.PlayerPreferencesRepository
import com.valladares.iptvplayer.navigation.NavDestination
import com.valladares.iptvplayer.navigation.SubtitleMetadata
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * ViewModel that controls playback state through [PlayerManager].
 * The stream URL is read from navigation arguments in [SavedStateHandle] (`streamUrl`, URI-encoded in the path).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager,
    private val playerPreferencesRepository: PlayerPreferencesRepository
) : ViewModel() {
    private var lastPlayKey: String? = null
    private var lastPlayStartedAtMs: Long = 0L
    private var lastSubtitleSwitchAtMs: Long = 0L
    private var attachedPlayer: ExoPlayer? = null
    private val embeddedSubtitleOverridesById: MutableMap<String, TrackSelectionOverride> = mutableMapOf()
    private val _availableSubtitles = kotlinx.coroutines.flow.MutableStateFlow<List<MediaItem.SubtitleConfiguration>>(emptyList())
    val availableSubtitles: StateFlow<List<MediaItem.SubtitleConfiguration>> = _availableSubtitles
    private val subtitleTrackListener = object : Player.Listener {
        override fun onCues(cues: CueGroup) {
            if (cues.cues.isNotEmpty()) {
                android.util.Log.d(
                    "SubDiag",
                    "Cues received: count=${cues.cues.size}, first text='${cues.cues.first().text?.take(100)}'"
                )
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            android.util.Log.d("SubDiag", "=== onTracksChanged ===")
            android.util.Log.d("SubDiag", "Total groups: ${tracks.groups.size}")
            tracks.groups.forEachIndexed { gIdx, group ->
                val typeStr = when (group.type) {
                    C.TRACK_TYPE_VIDEO -> "VIDEO"
                    C.TRACK_TYPE_AUDIO -> "AUDIO"
                    C.TRACK_TYPE_TEXT -> "TEXT"
                    else -> "OTHER(${group.type})"
                }
                for (tIdx in 0 until group.length) {
                    val format = group.getTrackFormat(tIdx)
                    val isSupported = group.isTrackSupported(tIdx)
                    val isSelected = group.isTrackSelected(tIdx)
                    android.util.Log.d(
                        "SubDiag",
                        "  G$gIdx[$tIdx] type=$typeStr id=${format.id} " +
                            "lang=${format.language} label=${format.label} mime=${format.sampleMimeType} " +
                            "supported=$isSupported selected=$isSelected " +
                            "containerMime=${format.containerMimeType}"
                    )
                }
            }
            refreshAvailableSubtitlesFromTracks(tracks)
        }

        override fun onPlayerError(error: PlaybackException) {
            android.util.Log.e("SubDiag", "onPlayerError: code=${error.errorCode} message=${error.message}", error)
            val player = attachedPlayer ?: return
            val switchedRecently = System.currentTimeMillis() - lastSubtitleSwitchAtMs <= 4_000L
            if (switchedRecently) {
                recoverFromSubtitleFailure(player, error)
            }
        }
    }

    val fullscreenEnabled: StateFlow<Boolean> = playerPreferencesRepository.fullscreenEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true
    )

    /**
     * Returns the current [ExoPlayer] for [PlayerView] binding.
     *
     * Exposed as a function so the UI only binds after navigation and does not
     * read the [PlayerManager] before [init] has run [playStream].
     */
    fun getPlayer(): ExoPlayer = playerManager.player

    init {
        val encoded = savedStateHandle.get<String>(NavDestination.Player.ARG_STREAM_URL)
        if (encoded != null) {
            val decoded = Uri.decode(encoded)
            val userAgent = savedStateHandle
                .get<String>(NavDestination.Player.ARG_USER_AGENT)
                ?.let { Uri.decode(it) }
                ?.takeIf { it.isNotBlank() }
                ?: AppConstants.DEFAULT_USER_AGENT
            val referer = savedStateHandle
                .get<String>(NavDestination.Player.ARG_REFERER)
                ?.let { Uri.decode(it) }
                ?.takeIf { it.isNotBlank() }
            val playlistId = savedStateHandle
                .get<String>(NavDestination.Player.ARG_PLAYLIST_ID)
                ?.let { Uri.decode(it) }
                ?.takeIf { it.isNotBlank() }
            val liveChannelId = savedStateHandle
                .get<String>(NavDestination.Player.ARG_LIVE_CHANNEL_ID)
                ?.toLongOrNull()

            val subtitlesJson = savedStateHandle
                .get<String>(NavDestination.Player.ARG_SUBTITLES)
                ?.let { Uri.decode(it) }
                ?.takeIf { it.isNotBlank() }

            val subtitles = if (!subtitlesJson.isNullOrBlank()) {
                try {
                    val metadataList = Json.decodeFromString<List<SubtitleMetadata>>(subtitlesJson)
                    metadataList.mapNotNull { metadata ->
                        buildSubtitleConfiguration(
                            id = metadata.id,
                            label = metadata.lang,
                            url = metadata.url,
                            language = metadata.lang
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PlayerViewModel", "Failed to parse subtitles JSON", e)
                    null
                }
            } else null

            _availableSubtitles.value = subtitles.orEmpty()
            playStream(decoded, userAgent, referer, playlistId, liveChannelId, subtitles)
        }
    }

    /**
     * Starts playback for the supplied stream [url].
     */
    fun playStream(
        url: String,
        userAgent: String,
        referer: String?,
        playlistId: String?,
        liveChannelId: Long?,
        subtitles: List<MediaItem.SubtitleConfiguration>? = null
    ) {
        val requestKey = listOf(url, userAgent, referer.orEmpty(), playlistId.orEmpty(), liveChannelId?.toString().orEmpty())
            .joinToString("|")
        val now = System.currentTimeMillis()
        if (requestKey == lastPlayKey && now - lastPlayStartedAtMs < 1_500L) {
            return
        }
        lastPlayKey = requestKey
        lastPlayStartedAtMs = now
        viewModelScope.launch {
            playerManager.play(
                url = url,
                userAgent = userAgent,
                referer = referer,
                playlistId = playlistId,
                liveChannelId = liveChannelId,
                subtitles = subtitles
            )
            attachSubtitleListener()
            refreshAvailableSubtitlesFromTracks(playerManager.player.currentTracks)
        }
    }

    fun setFullscreenEnabled(enabled: Boolean) {
        viewModelScope.launch {
            playerPreferencesRepository.setFullscreenEnabled(enabled)
        }
    }

    fun setSubtitleTrack(subtitleConfig: MediaItem.SubtitleConfiguration) {
        val player = playerManager.player
        lastSubtitleSwitchAtMs = System.currentTimeMillis()
        android.util.Log.d("SubDiag", "=== setSubtitleTrack ===")
        android.util.Log.d("SubDiag", "Selected: id=${subtitleConfig.id} label=${subtitleConfig.label} lang=${subtitleConfig.language}")
        if (subtitleConfig.id.toString() == "none") {
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                .setPreferredTextLanguage(null)
                .build()
        } else {
            var selectedOverride: TrackSelectionOverride? =
                embeddedSubtitleOverridesById[subtitleConfig.id?.toString().orEmpty()]
            android.util.Log.d("SubDiag", "Override from embedded map: $selectedOverride")
            val selectedLabel = subtitleConfig.label?.toString()
            val selectedLanguage = subtitleConfig.language
            val selectedId = subtitleConfig.id?.toString()

            if (selectedOverride == null) {
                for (group in player.currentTracks.groups) {
                    if (group.type != C.TRACK_TYPE_TEXT) continue
                    for (index in 0 until group.length) {
                        val format = group.getTrackFormat(index)
                        val idMatch = !selectedId.isNullOrBlank() && selectedId == format.id
                        val labelMatch = !selectedLabel.isNullOrBlank() &&
                            selectedLabel.equals(format.label, ignoreCase = true)
                        val languageMatch = languageMatches(
                            selected = selectedLanguage ?: selectedLabel,
                            trackLanguage = format.language
                        )
                        if (idMatch || labelMatch || languageMatch) {
                            selectedOverride = TrackSelectionOverride(group.mediaTrackGroup, listOf(index))
                            break
                        }
                    }
                    if (selectedOverride != null) break
                }
            }

            val preferredLanguage = subtitleConfig.language
                ?.takeIf { it.isNotBlank() }
                ?: subtitleConfig.label
                    ?.toString()
                    ?.takeIf { it.isNotBlank() }
            android.util.Log.d("SubDiag", "Final override: $selectedOverride, preferredLang: $preferredLanguage")
            player.trackSelectionParameters = player.trackSelectionParameters
                .buildUpon()
                .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                .clearOverridesOfType(C.TRACK_TYPE_TEXT)
                .setPreferredTextLanguage(preferredLanguage)
                .apply {
                    if (selectedOverride != null) {
                        setOverrideForType(selectedOverride)
                    }
                }
                .build()
        }
        android.util.Log.d(
            "SubDiag",
            "Params applied. Current text disabled: ${player.trackSelectionParameters.disabledTrackTypes}"
        )
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
        player.playWhenReady = true
        player.play()
    }

    private fun refreshAvailableSubtitlesFromTracks(tracks: Tracks) {
        embeddedSubtitleOverridesById.clear()
        val detected = tracks.groups
            .asSequence()
            .filter { it.type == C.TRACK_TYPE_TEXT }
            .flatMap { group ->
                (0 until group.length).asSequence().mapNotNull { index ->
                    val format: Format = group.getTrackFormat(index)
                    val id = "embedded_${group.mediaTrackGroup.id}_$index"
                    val label = when {
                        !format.label.isNullOrBlank() -> format.label
                        !format.language.isNullOrBlank() -> format.language
                        else -> "Subtitle ${index + 1}"
                    }
                    embeddedSubtitleOverridesById[id] = TrackSelectionOverride(group.mediaTrackGroup, listOf(index))
                    MediaItem.SubtitleConfiguration.Builder(Uri.EMPTY)
                        .setId(id)
                        .setLabel(label)
                        .setLanguage(format.language ?: "")
                        .build()
                }
            }
            .toList()
        _availableSubtitles.value = detected
    }

    private fun attachSubtitleListener() {
        val currentPlayer = playerManager.player
        if (attachedPlayer === currentPlayer) return
        attachedPlayer?.removeListener(subtitleTrackListener)
        currentPlayer.addListener(subtitleTrackListener)
        attachedPlayer = currentPlayer
    }

    private fun recoverFromSubtitleFailure(player: ExoPlayer, error: PlaybackException) {
        android.util.Log.w("PlayerViewModel", "Subtitle switch failed, recovering playback", error)
        val resumePosition = player.currentPosition.coerceAtLeast(0L)
        player.trackSelectionParameters = player.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_TEXT)
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
            .setPreferredTextLanguage(null)
            .build()
        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }
        player.seekTo(resumePosition)
        player.playWhenReady = true
        player.play()
    }

    override fun onCleared() {
        attachedPlayer?.removeListener(subtitleTrackListener)
        playerManager.release()
        super.onCleared()
    }
}

private fun languageMatches(selected: String?, trackLanguage: String?): Boolean {
    if (selected.isNullOrBlank() || trackLanguage.isNullOrBlank()) return false
    val normalizedSelected = selected.trim().lowercase()
    val normalizedTrack = trackLanguage.trim().lowercase()
    if (normalizedSelected == normalizedTrack) return true
    if (normalizedSelected.startsWith(normalizedTrack) || normalizedTrack.startsWith(normalizedSelected)) {
        return true
    }
    val alias = when (normalizedSelected) {
        "sv", "sv-se", "swedish" -> setOf("sv", "sv-se", "swe")
        "en", "en-us", "english" -> setOf("en", "en-us", "eng")
        else -> setOf(normalizedSelected)
    }
    return normalizedTrack in alias
}
