package com.valladares.iptvplayer.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.valladares.iptvplayer.core.database.dao.ChannelDao
import com.valladares.iptvplayer.core.database.dao.PlaylistDao
import com.valladares.iptvplayer.core.database.entity.ChannelEntity
import com.valladares.iptvplayer.core.database.entity.PlaylistEntity

/**
 * Application Room database for playlists and channel rows.
 */
@Database(
    entities = [PlaylistEntity::class, ChannelEntity::class],
    version = 2,
    exportSchema = false
)
abstract class IPTVDatabase : RoomDatabase() {
    /**
     * Provides access to playlist persistence.
     */
    abstract fun playlistDao(): PlaylistDao

    /**
     * Provides access to channel persistence.
     */
    abstract fun channelDao(): ChannelDao
}
