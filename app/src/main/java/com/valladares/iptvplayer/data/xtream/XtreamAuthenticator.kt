package com.valladares.iptvplayer.data.xtream

import com.valladares.iptvplayer.data.xtream.api.XtreamApiFactory
import com.valladares.iptvplayer.data.xtream.model.XtreamAuthFailureReason
import com.valladares.iptvplayer.data.xtream.model.XtreamAuthStatus
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/**
 * Performs Xtream authentication against `player_api.php`.
 */
@Singleton
class XtreamAuthenticator @Inject constructor(
    private val apiFactory: XtreamApiFactory
) {
    /**
     * Authenticates [credentials] and maps provider-specific values to app status.
     */
    suspend fun authenticate(credentials: XtreamCredentials): XtreamAuthStatus =
        withContext(Dispatchers.IO) {
            try {
                val api = apiFactory.create(credentials.serverUrl)
                val response = api.authenticate(
                    username = credentials.username,
                    password = credentials.password
                )
                val userInfo = response.userInfo
                val status = userInfo.status
                when {
                    userInfo.auth == 0 -> XtreamAuthStatus.Failed(
                        XtreamAuthFailureReason.INVALID_CREDENTIALS
                    )

                    status.equals("Expired", ignoreCase = true) -> XtreamAuthStatus.Failed(
                        XtreamAuthFailureReason.ACCOUNT_EXPIRED
                    )

                    status.equals("Disabled", ignoreCase = true) -> XtreamAuthStatus.Failed(
                        XtreamAuthFailureReason.ACCOUNT_DISABLED
                    )

                    status.equals("Active", ignoreCase = true) || status == null -> {
                        XtreamAuthStatus.Success(
                            userInfo = response.userInfo,
                            serverInfo = response.serverInfo
                        )
                    }

                    else -> XtreamAuthStatus.Failed(XtreamAuthFailureReason.UNKNOWN)
                }
            } catch (_: IOException) {
                XtreamAuthStatus.Failed(XtreamAuthFailureReason.NETWORK_ERROR)
            } catch (_: HttpException) {
                XtreamAuthStatus.Failed(XtreamAuthFailureReason.SERVER_ERROR)
            } catch (_: Exception) {
                XtreamAuthStatus.Failed(XtreamAuthFailureReason.UNKNOWN)
            }
        }
}
