package com.valladares.iptvplayer.core.database.mapper

import com.valladares.iptvplayer.core.database.entity.ChannelEntity
import com.valladares.iptvplayer.data.playlist.model.Channel

/**
 * Converts a [ChannelEntity] to the domain [Channel] model.
 *
 * Domain [Channel.id] is [ChannelEntity.externalId] (parser/tvg), not the internal row id.
 */
fun ChannelEntity.toDomain(): Channel = Channel(
    id = externalId,
    name = name,
    streamUrl = streamUrl,
    logoUrl = logoUrl,
    groupTitle = groupTitle
)

/**
 * Maps a parsed [Channel] to a [ChannelEntity] scoped to a playlist and M3U order.
 *
 * The row primary key is left at the default (0) so Room can auto-generate on insert;
 * [externalId] is the parser’s [Channel.id] (tvg-id or generated UUID).
 */
fun Channel.toEntity(playlistId: String, sortOrder: Int): ChannelEntity = ChannelEntity(
    externalId = id,
    playlistId = playlistId,
    name = name,
    streamUrl = streamUrl,
    logoUrl = logoUrl,
    groupTitle = groupTitle,
    sortOrder = sortOrder
)
