package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * VOD stream row from Xtream `get_vod_streams`.
 */
@Serializable
data class XtreamVodStreamDto(
    val num: Int? = null,
    val name: String,
    @SerialName("stream_type") val streamType: String? = null,
    @SerialName("stream_id") val streamId: Int,
    @SerialName("stream_icon") val streamIcon: String? = null,
    val rating: String? = null,
    @SerialName("rating_5based") val rating5Based: Double? = null,
    val added: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("container_extension") val containerExtension: String? = null,
    @SerialName("custom_sid") val customSid: String? = null,
    @SerialName("direct_source") val directSource: String? = null
)
