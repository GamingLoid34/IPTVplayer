package com.valladares.iptvplayer.data.xtream.model

import com.valladares.iptvplayer.data.xtream.dto.XtreamServerInfoDto
import com.valladares.iptvplayer.data.xtream.dto.XtreamUserInfoDto

/**
 * Result status for an Xtream authentication attempt.
 */
sealed interface XtreamAuthStatus {
    /**
     * Authentication succeeded.
     */
    data class Success(
        val userInfo: XtreamUserInfoDto,
        val serverInfo: XtreamServerInfoDto
    ) : XtreamAuthStatus

    /**
     * Authentication failed with a normalized reason.
     */
    data class Failed(val reason: XtreamAuthFailureReason) : XtreamAuthStatus
}

/**
 * Normalized authentication failure reasons for app logic.
 */
enum class XtreamAuthFailureReason {
    INVALID_CREDENTIALS,
    ACCOUNT_EXPIRED,
    ACCOUNT_DISABLED,
    NETWORK_ERROR,
    SERVER_ERROR,
    UNKNOWN
}
