package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * VOD stream row from Xtream `get_vod_streams`.
 */
@Serializable
data class XtreamVodStreamDto(
    val num: Int? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val name: String? = null,
    @SerialName("stream_type") val streamType: String? = null,
    @Serializable(with = FlexibleIntSerializer::class)
    @SerialName("stream_id") val streamId: Int? = null,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @Serializable(with = FlexibleStringSerializer::class)
    val rating: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    @SerialName("rating_5based") val rating5Based: Double? = null,
    val added: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("custom_sid") val customSid: String? = null,
    @SerialName("direct_source") val directSource: String? = null
)

/**
 * VOD detail response from Xtream `get_vod_info`.
 */
@Serializable
data class XtreamVodInfoDto(
    val info: XtreamVodInfoMetaDto,
    val subtitles: List<XtreamSubtitleDto>? = null
)

/**
 * VOD metadata section in Xtream VOD info.
 */
@Serializable
data class XtreamVodInfoMetaDto(
    val name: String? = null,
    @SerialName("movie_image") val movieImage: String? = null,
    val plot: String? = null,
    val duration: String? = null,
    @Serializable(with = FlexibleIntSerializer::class)
    @SerialName("duration_secs") val durationSecs: Int? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    val rating: Double? = null,
    val cast: String? = null,
    val director: String? = null,
    val genre: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null
)
