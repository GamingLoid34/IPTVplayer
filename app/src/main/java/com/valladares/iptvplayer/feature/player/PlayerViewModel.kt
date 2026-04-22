package com.valladares.iptvplayer.feature.player

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.valladares.iptvplayer.core.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

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
        val encoded = savedStateHandle.get<String>("streamUrl")
        if (encoded != null) {
            val decoded = Uri.decode(encoded)
            playStream(decoded)
        }
    }

    /**
     * Starts playback for the supplied stream [url].
     */
    fun playStream(url: String) {
        playerManager.play(url)
    }

    override fun onCleared() {
        playerManager.release()
        super.onCleared()
    }
}
