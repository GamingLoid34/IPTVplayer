package com.valladares.iptvplayer.domain.usecase

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.model.Playlist
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Exposes a cold [Flow] of all playlists from [PlaylistRepository].
 */
class GetPlaylistsUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    /**
     * Subscribes to all playlists, ordered as defined by the repository.
     */
    operator fun invoke(): Flow<List<Playlist>> = repository.observePlaylists()
}
