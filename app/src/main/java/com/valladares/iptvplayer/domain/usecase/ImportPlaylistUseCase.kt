package com.valladares.iptvplayer.domain.usecase

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType
import javax.inject.Inject

/**
 * Orchestrates importing a playlist from already-loaded M3U [content].
 */
class ImportPlaylistUseCase @Inject constructor(
    private val repository: PlaylistRepository
) {
    /**
     * Persists [name] and parsed channels, returning the new playlist id.
     */
    suspend operator fun invoke(
        name: String,
        sourceType: PlaylistSourceType,
        sourceUri: String,
        content: String
    ): Result<String> = repository.importPlaylistFromContent(
        name = name,
        sourceType = sourceType,
        sourceUri = sourceUri,
        content = content
    )
}
