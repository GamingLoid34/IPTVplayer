package com.valladares.iptvplayer.core.database.mapper

import com.valladares.iptvplayer.core.database.entity.PlaylistEntity
import com.valladares.iptvplayer.data.playlist.model.Playlist
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType

/**
 * Converts a [PlaylistEntity] to the domain [Playlist] model.
 */
fun PlaylistEntity.toDomain(): Playlist = Playlist(
    id = id,
    name = name,
    sourceType = runCatching { PlaylistSourceType.valueOf(sourceType) }
        .getOrElse { PlaylistSourceType.URL },
    sourceUri = sourceUri,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Converts a domain [Playlist] to a [PlaylistEntity] for persistence.
 */
fun Playlist.toEntity(): PlaylistEntity = PlaylistEntity(
    id = id,
    name = name,
    sourceType = sourceType.name,
    sourceUri = sourceUri,
    createdAt = createdAt,
    updatedAt = updatedAt
)
