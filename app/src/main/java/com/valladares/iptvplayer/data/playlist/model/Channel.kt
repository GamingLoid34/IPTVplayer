package com.valladares.iptvplayer.data.playlist.model

/**
 * Domain-ready channel model parsed from M3U metadata.
 */
data class Channel(
    val id: String,
    val name: String,
    val streamUrl: String,
    val logoUrl: String?,
    val groupTitle: String?
)
