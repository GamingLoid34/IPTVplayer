package com.valladares.iptvplayer.data.xtream.model

/**
 * Domain model for Xtream live channel rows.
 */
data class LiveChannel(
    val id: Long,
    val playlistId: String,
    val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryExternalId: String?,
    val detectedCountryCode: String? = null,
    val epgChannelId: String?,
    val hasTvArchive: Boolean,
    val tvArchiveDuration: Int?
)
