package com.valladares.iptvplayer.data.xtream.model

/**
 * Domain model for Xtream episode rows.
 */
data class Episode(
    val id: Long,
    val seriesRowId: Long,
    val externalId: String,
    val title: String,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val containerExtension: String?,
    val plot: String?,
    val durationSecs: Int?,
    val movieImage: String?
)
