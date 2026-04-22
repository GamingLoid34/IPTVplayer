package com.valladares.iptvplayer.data.playlist

import com.valladares.iptvplayer.data.playlist.model.Channel

/**
 * Contract for playlist data sources in the data layer.
 */
interface PlaylistDataSource {
    /**
     * Parses playlist [content] and returns discovered channels.
     */
    fun parse(content: String): List<Channel>
}
