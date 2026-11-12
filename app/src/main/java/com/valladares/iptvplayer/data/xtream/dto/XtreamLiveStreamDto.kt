package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Live stream row from Xtream `get_live_streams`.
 */
@Serializable
data class XtreamLiveStreamDto(
    val num: Int? = null,
    val name: String,
    @SerialName("stream_type") val streamType: String? = null,
    @SerialName("stream_id") val streamId: Int,
    @SerialName("stream_icon") val streamIcon: String? = null,
    @SerialName("epg_channel_id") val epgChannelId: String? = null,
    val added: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("custom_sid") val customSid: String? = null,
    @SerialName("tv_archive") val tvArchive: Int? = null,
    @SerialName("direct_source") val directSource: String? = null,
    @SerialName("tv_archive_duration") val tvArchiveDuration: Int? = null
)
