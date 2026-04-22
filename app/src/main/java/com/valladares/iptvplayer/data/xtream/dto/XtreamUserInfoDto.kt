package com.valladares.iptvplayer.data.xtream.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Authentication response payload from `player_api.php`.
 */
@Serializable
data class XtreamAuthResponseDto(
    @SerialName("user_info") val userInfo: XtreamUserInfoDto,
    @SerialName("server_info") val serverInfo: XtreamServerInfoDto
)

/**
 * User section in Xtream auth response.
 */
@Serializable
data class XtreamUserInfoDto(
    val username: String? = null,
    val password: String? = null,
    val status: String? = null,
    @SerialName("exp_date") val expDate: String? = null,
    @SerialName("is_trial") val isTrial: String? = null,
    @SerialName("active_cons") val activeConnections: String? = null,
    @SerialName("max_connections") val maxConnections: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val auth: Int? = null
)

/**
 * Server section in Xtream auth response.
 */
@Serializable
data class XtreamServerInfoDto(
    val url: String? = null,
    val port: String? = null,
    @SerialName("https_port") val httpsPort: String? = null,
    @SerialName("server_protocol") val serverProtocol: String? = null,
    val timezone: String? = null,
    @SerialName("time_now") val timeNow: String? = null
)
