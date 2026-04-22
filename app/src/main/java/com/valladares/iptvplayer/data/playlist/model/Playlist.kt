package com.valladares.iptvplayer.data.playlist.model

/**
 * How the playlist was supplied (URL fetch vs. local file).
 */
enum class PlaylistSourceType {
    /**
     * Remote URL string (e.g. M3U served over HTTP/HTTPS).
     */
    URL,

    /**
     * Local file path or content-uri style reference for on-device file.
     */
    FILE
}

/**
 * User-facing playlist metadata stored in the database.
 */
data class Playlist(
    val id: String,
    val name: String,
    val sourceType: PlaylistSourceType,
    val sourceUri: String,
    val createdAt: Long,
    val updatedAt: Long
)
