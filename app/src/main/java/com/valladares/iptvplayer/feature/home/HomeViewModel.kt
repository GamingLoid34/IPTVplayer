package com.valladares.iptvplayer.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.data.playlist.model.Playlist
import com.valladares.iptvplayer.domain.usecase.GetPlaylistsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * UI state for the home (playlist) screen.
 */
sealed interface HomeUiState {
    /**
     * Initial / loading state before first known playlist emission.
     */
    data object Loading : HomeUiState

    /**
     * Non-empty playlist list from the database.
     */
    data class Content(val playlists: List<Playlist>) : HomeUiState

    /**
     * Database returned no playlists.
     */
    data object Empty : HomeUiState
}

/**
 * Exposes [HomeUiState] from [GetPlaylistsUseCase] for the home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPlaylistsUseCase: GetPlaylistsUseCase
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = getPlaylistsUseCase()
        .map { list ->
            if (list.isEmpty()) HomeUiState.Empty
            else HomeUiState.Content(list)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )
}
