package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for Xtream live channels.
 */
@Entity(
    tableName = "live_channels",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("categoryExternalId")]
)
data class LiveChannelEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playlistId: String,
    val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryExternalId: String?,
    val epgChannelId: String?,
    val tvArchive: Int?,
    val tvArchiveDuration: Int?,
    val sortOrder: Int
)
