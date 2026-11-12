package com.valladares.iptvplayer.data.xtream.model

import android.net.Uri
import java.io.IOException

/**
 * Xtream credentials parsed from provider URL or entered manually.
 *
 * Example input URL:
 * `http://example.com:8080/get.php?username=john&password=secret&type=m3u_plus`
 * becomes:
 * - serverUrl = `http://example.com:8080`
 * - username = `john`
 * - password = `secret`
 */
data class XtreamCredentials(
    val serverUrl: String,
    val username: String,
    val password: String
) {
    init {
        require(serverUrl.isNotBlank()) { "serverUrl får inte vara tom" }
        require(username.isNotBlank()) { "username får inte vara tom" }
        require(password.isNotBlank()) { "password får inte vara tom" }
    }

    companion object {
        /**
         * Parses Xtream credentials from an M3U URL.
         *
         * Supports both `username` and `user` parameter names.
         */
        fun fromM3uUrl(url: String): Result<XtreamCredentials> = runCatching {
            if (url.isBlank()) {
                throw IOException("URL får inte vara tom")
            }
            val uri = Uri.parse(url)
            val scheme = uri.scheme?.takeIf { it.isNotBlank() }
                ?: throw IOException("URL saknar protokoll (http/https)")
            val host = uri.host?.takeIf { it.isNotBlank() }
                ?: throw IOException("URL saknar host")
            val port = uri.port
            val normalizedServerUrl = buildString {
                append(scheme)
                append("://")
                append(host)
                if (port != -1) {
                    append(":")
                    append(port)
                }
            }.trimEnd('/')
            val username = uri.getQueryParameter("username")
                ?: uri.getQueryParameter("user")
                ?: throw IOException("URL saknar username-parameter")
            val password = uri.getQueryParameter("password")
                ?: throw IOException("URL saknar password-parameter")
            XtreamCredentials(
                serverUrl = normalizedServerUrl,
                username = username,
                password = password
            )
        }
    }
}
