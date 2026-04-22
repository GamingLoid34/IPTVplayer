package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room row for Xtream VOD categories.
 */
@Entity(
    tableName = "vod_categories",
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId")]
)
data class VodCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val playlistId: String,
    val externalId: String,
    val name: String,
    val parentId: Int?
)
