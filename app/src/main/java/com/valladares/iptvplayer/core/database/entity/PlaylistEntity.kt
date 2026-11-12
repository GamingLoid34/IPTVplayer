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
    /**
     * Valfria HTTP-headers för stream-uppspelning. Vissa IPTV-servrar blockerar specifika
     * User-Agents med HTTP 458. Null = använd AppConstants.DEFAULT_USER_AGENT.
     */
    val userAgent: String? = null,
    /**
     * Valfria HTTP-headers för stream-uppspelning. Vissa IPTV-servrar blockerar specifika
     * User-Agents med HTTP 458. Null = använd AppConstants.DEFAULT_USER_AGENT.
     */
    val referer: String? = null,
    val xtreamServerUrl: String? = null,
    val xtreamUsername: String? = null,
    val xtreamPassword: String? = null
)
