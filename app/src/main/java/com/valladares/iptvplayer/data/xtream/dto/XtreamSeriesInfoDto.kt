package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Series detail response from Xtream `get_series_info`.
 */
@Serializable
data class XtreamSeriesInfoDto(
    val seasons: List<XtreamSeasonDto>? = null,
    val info: XtreamSeriesInfoMetaDto,
    val episodes: JsonObject? = null
)

/**
 * Season info section in Xtream series info.
 */
@Serializable
data class XtreamSeasonDto(
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("episode_count") val episodeCount: Int? = null,
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    @SerialName("season_number") val seasonNumber: Int? = null,
    val cover: String? = null
)

/**
 * Series metadata section in Xtream series info.
 */
@Serializable
data class XtreamSeriesInfoMetaDto(
    val name: String? = null,
    val cover: String? = null,
    val plot: String? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null
)

/**
 * Episode row used when decoding dynamic episodes map entries.
 */
@Serializable
data class XtreamEpisodeDto(
    val id: String,
    @SerialName("episode_num") val episodeNum: Int? = null,
    val title: String,
    @SerialName("container_extension") val containerExtension: String? = null,
    val info: XtreamEpisodeInfoDto? = null,
    val added: String? = null,
    val season: Int? = null
)

/**
 * Extra episode metadata block.
 */
@Serializable
data class XtreamEpisodeInfoDto(
    @SerialName("movie_image") val movieImage: String? = null,
    val plot: String? = null,
    val duration: String? = null,
    @SerialName("duration_secs") val durationSecs: Int? = null,
    val rating: Double? = null
)
