package com.valladares.iptvplayer.di

import com.valladares.iptvplayer.data.playlist.PlaylistRepository
import com.valladares.iptvplayer.data.playlist.PlaylistRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that binds repository interfaces to implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    /**
     * Binds [PlaylistRepository] to [PlaylistRepositoryImpl].
     */
    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository
}
