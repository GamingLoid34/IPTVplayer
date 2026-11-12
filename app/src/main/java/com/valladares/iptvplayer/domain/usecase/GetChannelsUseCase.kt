package com.valladares.iptvplayer.domain.usecase

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.model.Channel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Exposes a cold [Flow] of channels for a specific playlist.
 */
class GetChannelsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    /**
     * Subscribes to channels for [playlistId] in M3U order.
     */
    operator fun invoke(playlistId: String): Flow<List<Channel>> =
        repository.observeChannels(playlistId)
}
