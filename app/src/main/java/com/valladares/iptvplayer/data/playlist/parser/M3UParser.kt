package com.valladares.iptvplayer.data.playlist.parser

import com.valladares.iptvplayer.data.playlist.PlaylistDataSource
import com.valladares.iptvplayer.data.playlist.model.Channel
import java.util.UUID
import javax.inject.Inject

/**
 * Parses standard M3U/M3U8 playlist content into channel models.
 */
class M3UParser @Inject constructor() : PlaylistDataSource {
    private val tvgIdRegex = Regex("""tvg-id="([^"]*)"""")
    private val tvgLogoRegex = Regex("""tvg-logo="([^"]*)"""")
    private val groupTitleRegex = Regex("""group-title="([^"]*)"""")

    /**
     * Converts raw M3U [content] into a list of [Channel].
     *
     * Returns an empty list when the input is blank or does not contain valid EXTINF + URL pairs.
     */
    override fun parse(content: String): List<Channel> {
        if (content.isBlank()) {
            return emptyList()
        }

        val lines = content.lineSequence()
            .map { it.trim() }
            .toList()

        if (lines.isEmpty()) {
            return emptyList()
        }

        val channels = mutableListOf<Channel>()
        var pendingMetadata: PendingChannelMetadata? = null

        for (line in lines) {
            if (line.isBlank() || line.startsWith("#EXTM3U")) {
                continue
            }

            if (line.startsWith("#EXTINF", ignoreCase = true)) {
                pendingMetadata = parseExtInfLine(line)
                continue
            }

            if (line.startsWith("#")) {
                continue
            }

            val metadata = pendingMetadata ?: continue
            if (!isLikelyUrl(line)) {
                pendingMetadata = null
                continue
            }

            val channelId = metadata.tvgId
                ?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

            val channelName = metadata.name.ifBlank { "Okänd kanal" }
            val logoUrl = metadata.logoUrl?.takeIf { it.isNotBlank() }
            val groupTitle = metadata.groupTitle?.takeIf { it.isNotBlank() }

            channels.add(
                Channel(
                    id = channelId,
                    name = channelName,
                    streamUrl = line,
                    logoUrl = logoUrl,
                    groupTitle = groupTitle
                )
            )
            pendingMetadata = null
        }

        return channels
    }

    private fun parseExtInfLine(line: String): PendingChannelMetadata {
        val name = line.substringAfter(",", missingDelimiterValue = "")
            .trim()

        return PendingChannelMetadata(
            tvgId = extractAttribute(line, tvgIdRegex),
            logoUrl = extractAttribute(line, tvgLogoRegex),
            groupTitle = extractAttribute(line, groupTitleRegex),
            name = name
        )
    }

    private fun extractAttribute(line: String, regex: Regex): String? {
        return regex.find(line)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun isLikelyUrl(value: String): Boolean {
        val lower = value.lowercase()
        return lower.startsWith("http://") || lower.startsWith("https://")
    }
}

private data class PendingChannelMetadata(
    val tvgId: String?,
    val logoUrl: String?,
    val groupTitle: String?,
    val name: String
)
