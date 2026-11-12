package com.valladares.iptvplayer.domain.usecase

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import javax.inject.Inject

/**
 * Deletes a playlist and its related rows via [PlaylistRepository].
 */
class DeletePlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    /**
     * Removes the playlist with [playlistId] if it exists.
     */
    suspend operator fun invoke(playlistId: String) {
        repository.deletePlaylist(playlistId)
    }
}
