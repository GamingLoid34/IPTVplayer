package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Series row from Xtream `get_series`.
 */
@Serializable
data class XtreamSeriesDto(
    val num: Int? = null,
    val name: String,
    @SerialName("series_id") val seriesId: Int,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null,
    @SerialName("last_modified") val lastModified: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val rating: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    @SerialName("rating_5based") val rating5Based: Double? = null,
    @SerialName("category_id") val categoryId: String? = null
)
