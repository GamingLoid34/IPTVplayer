package com.valladares.iptvplayer.feature.playlistsettings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.navigation.NavDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for per-playlist playback header settings (User-Agent and Referer).
 */
@HiltViewModel
class PlaylistSettingsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {
    private val playlistId: String = savedStateHandle
        .get<String>(NavDestination.PlaylistSettings.ARG_PLAYLIST_ID)
        .orEmpty()

    private val didHydrateFields: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val _userAgent: MutableStateFlow<String> = MutableStateFlow("")
    private val _referer: MutableStateFlow<String> = MutableStateFlow("")

    val playlistName: StateFlow<String> = playlistRepository.observePlaylists()
        .map { list ->
            val playlist = list.find { it.id == playlistId }
            if (playlist != null && !didHydrateFields.value) {
                _userAgent.value = playlist.userAgent.orEmpty()
                _referer.value = playlist.referer.orEmpty()
                didHydrateFields.value = true
            }
            playlist?.name.orEmpty()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    val userAgent: StateFlow<String> = _userAgent
    val referer: StateFlow<String> = _referer

    fun onUserAgentChange(value: String) {
        _userAgent.value = value
    }

    fun onRefererChange(value: String) {
        _referer.value = value
    }

    fun onSelectPreset(preset: UserAgentPreset) {
        _userAgent.value = preset.value
    }

    suspend fun save(): Result<Unit> = runCatching {
        playlistRepository.updatePlaybackHeaders(
            playlistId = playlistId,
            userAgent = _userAgent.value,
            referer = _referer.value
        )
    }
}

/**
 * Known User-Agent presets commonly accepted by IPTV/Xtream servers.
 */
enum class UserAgentPreset(val label: String, val value: String) {
    VLC("VLC", "VLC/3.0.20 LibVLC/3.0.20"),
    TIVIMATE("Tivimate", "TiviMate/4.7.0 (Linux;Android 11) ExoPlayerLib/2.18.1"),
    IPTV_SMARTERS("IPTV Smarters", "IPTVSmartersPro"),
    TELEVIZO("Televizo", "Televizo/1.9.3.2 (Linux;Android 14) ExoPlayerLib/2.19.1"),
    OKHTTP("okhttp", "okhttp/4.12.0"),
    LAVF("Lavf", "Lavf/60.3.100"),
    CUSTOM("Custom", "")
}
