package com.valladares.iptvplayer.domain.usecase

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import javax.inject.Inject

/**
 * Imports an Xtream playlist from explicit server credentials.
 */
class ImportXtreamPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    /**
     * Persists an Xtream playlist and returns playlist id on success.
     */
    suspend operator fun invoke(
        name: String,
        credentials: XtreamCredentials
    ): Result<String> = repository.importXtreamPlaylist(name, credentials)
}
