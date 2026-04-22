package com.valladares.iptvplayer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that provides app-wide Media3 player dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
    /**
     * Provides a singleton [ExoPlayer] instance scoped to the application lifecycle.
     */
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer = ExoPlayer.Builder(context).build()
}
