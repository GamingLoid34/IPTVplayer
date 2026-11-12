package com.valladares.iptvplayer.data.xtream.model

/**
 * Shared category domain model for live, VOD and series.
 */
data class XtreamCategory(
    val id: Long,
    val externalId: String,
    val name: String,
    val parentId: Int?
)
