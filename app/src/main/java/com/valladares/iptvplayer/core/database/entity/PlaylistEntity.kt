package com.valladares.iptvplayer.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a persisted M3U playlist.
 *
 * [sourceType] is stored as enum name (`URL`, `FILE`, `XTREAM`) for stable serialization.
 */
@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sourceType: String,
    val sourceUri: String,
    val createdAt: Long,
    val updatedAt: Long,
    val xtreamServerUrl: String? = null,
    val xtreamUsername: String? = null,
    val xtreamPassword: String? = null
)
