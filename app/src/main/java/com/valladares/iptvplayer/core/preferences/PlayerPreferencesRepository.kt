package com.valladares.iptvplayer.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.playerPreferencesDataStore by preferencesDataStore(name = "player_preferences")

/**
 * Stores global player preferences, including fullscreen behavior.
 */
@Singleton
class PlayerPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val fullscreenEnabled: Preferences.Key<Boolean> = booleanPreferencesKey("fullscreen_enabled")
    }

    /**
     * Global fullscreen preference. Defaults to true.
     */
    val fullscreenEnabled: Flow<Boolean> = context.playerPreferencesDataStore.data.map { preferences ->
        preferences[Keys.fullscreenEnabled] ?: true
    }

    /**
     * Persists global fullscreen preference.
     */
    suspend fun setFullscreenEnabled(enabled: Boolean) {
        context.playerPreferencesDataStore.edit { prefs ->
            prefs[Keys.fullscreenEnabled] = enabled
        }
    }
}
