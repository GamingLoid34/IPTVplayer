package com.valladares.iptvplayer.di

import android.content.Context
import androidx.room.Room
import com.valladares.iptvplayer.core.database.IPTVDatabase
import com.valladares.iptvplayer.core.database.dao.ChannelDao
import com.valladares.iptvplayer.core.database.dao.PlaylistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides the Room [IPTVDatabase] and DAOs.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    /**
     * Provides the singleton [IPTVDatabase] instance.
     *
     * Uses destructive migration on version upgrade so MVP does not require a
     * hand-written Room migration; existing local data is cleared when the DB version changes.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IPTVDatabase {
        return Room.databaseBuilder(
            context,
            IPTVDatabase::class.java,
            "iptvplayer.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /**
     * Provides the playlist [PlaylistDao].
     */
    @Provides
    fun providePlaylistDao(db: IPTVDatabase): PlaylistDao = db.playlistDao()

    /**
     * Provides the channel [ChannelDao].
     */
    @Provides
    fun provideChannelDao(db: IPTVDatabase): ChannelDao = db.channelDao()
}
