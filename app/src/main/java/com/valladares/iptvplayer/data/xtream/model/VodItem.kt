package com.valladares.iptvplayer.data.xtream.model

/**
 * Domain model for Xtream VOD items.
 */
data class VodItem(
    val id: Long,
    val playlistId: String,
    val streamId: Int,
    val name: String,
    val streamIcon: String?,
    val categoryExternalId: String?,
    val containerExtension: String?,
    val rating: String?,
    val rating5Based: Double?,
    val added: String?
)
