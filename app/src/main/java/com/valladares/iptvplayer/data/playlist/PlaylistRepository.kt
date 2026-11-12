package com.valladares.iptvplayer.data.playlist

import com.valladares.iptvplayer.data.playlist.model.Channel
import com.valladares.iptvplayer.data.playlist.model.PlaybackHeaders
import com.valladares.iptvplayer.data.playlist.model.Playlist
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for playlist and channel data (Fas 2; later a dedicated domain module).
 */
interface PlaylistRepository {
    /**
     * Observes all playlists, ordered by the implementation (typically newest first).
     */
    fun observePlaylists(): Flow<List<Playlist>>

    /**
     * Observes channels for the given [playlistId] in M3U order.
     */
    fun observeChannels(playlistId: String): Flow<List<Channel>>

    /**
     * Imports a playlist from already-loaded [content], persists metadata and channels.
     *
     * @return [Result] with the new playlist id on success.
     */
    suspend fun importPlaylistFromContent(
        name: String,
        sourceType: PlaylistSourceType,
        sourceUri: String,
        content: String
    ): Result<String>

    /**
     * Imports an Xtream playlist, returning the created playlist id on success.
     */
    suspend fun importXtreamPlaylist(
        name: String,
        credentials: XtreamCredentials
    ): Result<String>

    /**
     * Deletes a playlist and its channels (CASCADE) by [playlistId].
     */
    suspend fun deletePlaylist(playlistId: String)

    /**
     * Returns stored Xtream credentials for [playlistId], or null for M3U-only lists.
     */
    suspend fun getXtreamCredentials(playlistId: String): XtreamCredentials?

    /**
     * Returns effective playback headers for one playlist.
     */
    suspend fun getPlaybackHeaders(playlistId: String): PlaybackHeaders

    /**
     * Updates optional playback header overrides for one playlist.
     *
     * Blank inputs are normalized to null.
     */
    suspend fun updatePlaybackHeaders(
        playlistId: String,
        userAgent: String?,
        referer: String?
    )
}
