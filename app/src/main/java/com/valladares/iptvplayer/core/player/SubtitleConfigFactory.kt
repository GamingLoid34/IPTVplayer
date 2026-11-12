package com.valladares.iptvplayer.core.player

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes

/**
 * Builds subtitle configurations with sensible MIME inference from URL/extension.
 */
fun buildSubtitleConfiguration(
    id: String?,
    label: String?,
    url: String?,
    language: String?
): MediaItem.SubtitleConfiguration? {
    val subtitleUrl = url?.takeIf { it.isNotBlank() } ?: return null
    val uri = Uri.parse(subtitleUrl)
    val resolvedId = id?.takeIf { it.isNotBlank() } ?: subtitleUrl
    val resolvedLabel = label?.takeIf { it.isNotBlank() } ?: "Unknown"
    val resolvedLanguage = language?.takeIf { it.isNotBlank() }
    return MediaItem.SubtitleConfiguration.Builder(uri)
        .setId(resolvedId)
        .setLabel(resolvedLabel)
        .setMimeType(inferSubtitleMimeType(subtitleUrl))
        .setLanguage(resolvedLanguage ?: "")
        .build()
}

private fun inferSubtitleMimeType(url: String): String {
    val lower = url.lowercase()
    return when {
        lower.endsWith(".vtt") || lower.endsWith(".webvtt") -> MimeTypes.TEXT_VTT
        lower.endsWith(".srt") -> MimeTypes.APPLICATION_SUBRIP
        lower.endsWith(".ssa") || lower.endsWith(".ass") -> MimeTypes.TEXT_SSA
        lower.endsWith(".ttml") || lower.endsWith(".dfxp") -> MimeTypes.APPLICATION_TTML
        else -> MimeTypes.TEXT_VTT
    }
}
