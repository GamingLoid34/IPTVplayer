package com.valladares.iptvplayer.feature.player

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.valladares.iptvplayer.core.common.AppConstants
import com.valladares.iptvplayer.core.player.PlayerManager
import com.valladares.iptvplayer.navigation.NavDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * ViewModel that controls playback state through [PlayerManager].
 * The stream URL is read from navigation arguments in [SavedStateHandle] (`streamUrl`, URI-encoded in the path).
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playerManager: PlayerManager
) : ViewModel() {
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
            playStream(decoded, userAgent, referer, playlistId, liveChannelId)
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
        liveChannelId: Long?
    ) {
        viewModelScope.launch {
            playerManager.play(
                url = url,
                userAgent = userAgent,
                referer = referer,
                playlistId = playlistId,
                liveChannelId = liveChannelId
            )
        }
    }

    override fun onCleared() {
        playerManager.release()
        super.onCleared()
    }
}
