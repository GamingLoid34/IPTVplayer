package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for Xtream VOD items.
 */
@Entity(
    tableName = "vod_items",
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
data class VodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playlistId: String,
    val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryExternalId: String?,
    val containerExtension: String?,
    val rating: String?,
    val rating5Based: Double?,
    val added: String?,
    val sortOrder: Int
)
