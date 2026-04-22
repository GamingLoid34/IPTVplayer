package com.valladares.iptvplayer.data.xtream.model

/**
 * Domain model for Xtream series rows.
 */
data class Series(
    val id: Long,
    val playlistId: String,
    val seriesId: Int,
    val name: String,
    val cover: String?,
    val plot: String?,
    val cast: String?,
    val director: String?,
    val genre: String?,
    val releaseDate: String?,
    val rating: String?,
    val rating5Based: Double?,
    val categoryExternalId: String?
)
