package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Favorite relation between playlist and live channel.
 */
@Entity(
    tableName = "favorite_channels",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LiveChannelEntity::class,
            parentColumns = ["id"],
            childColumns = ["liveChannelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["playlistId", "liveChannelId"], unique = true),
        Index("playlistId"),
        Index("liveChannelId")
    ]
)
data class FavoriteChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playlistId: String,
    val liveChannelId: Long,
    val addedAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0
)
