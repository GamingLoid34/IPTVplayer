package com.valladares.iptvplayer.feature.player

import androidx.lifecycle.ViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.valladares.iptvplayer.core.common.AppConstants
import com.valladares.iptvplayer.core.player.PlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel that controls playback state through [PlayerManager].
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerManager: PlayerManager
) : ViewModel() {
    /**
     * Exposes the Media3 player instance to the UI layer.
     */
    val player: ExoPlayer = playerManager.player

    init {
        playStream(AppConstants.DEFAULT_TEST_STREAM_URL)
    }

    /**
     * Starts playback for the supplied stream [url].
     */
    fun playStream(url: String) {
        playerManager.play(url)
    }

    /**
     * Releases all player resources.
     */
    fun release() {
        playerManager.release()
    }
}
