package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for a channel row belonging to a [PlaylistEntity].
 *
 * The primary key is an internal [id] (surrogate). [externalId] stores the
 * tvg-id or parser-generated UUID from the M3U line — it is not globally unique
 * across playlists, but is stable for the parsed channel within one import.
 */
@Entity(
    tableName = "channels",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("playlistId"),
        Index(value = ["playlistId", "externalId"])
    ]
)
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val externalId: String,
    val playlistId: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String?,
    val groupTitle: String?,
    val sortOrder: Int
)
