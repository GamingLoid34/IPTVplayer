package com.valladares.iptvplayer.feature.channels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.data.playlist.model.Channel
import com.valladares.iptvplayer.domain.usecase.GetChannelsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val DEFAULT_GROUP = "Övrigt"

/**
 * UI state for the channel list screen.
 */
sealed interface ChannelListUiState {
    /**
     * First load in progress.
     */
    data object Loading : ChannelListUiState

    /**
     * No channels in this playlist.
     */
    data object Empty : ChannelListUiState

    /**
     * Grouped by [Channel.groupTitle] (or [DEFAULT_GROUP] when missing).
     */
    data class Content(
        val groupedChannels: Map<String, List<Channel>>
    ) : ChannelListUiState
}

/**
 * Loads and groups channels for the playlist in [SavedStateHandle] under `"playlistId"`.
 */
@HiltViewModel
class ChannelListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getChannelsUseCase: GetChannelsUseCase
) : ViewModel() {

    private val playlistId: String? = savedStateHandle.get<String>("playlistId")

    val uiState: StateFlow<ChannelListUiState> = (
        if (playlistId == null) {
            flowOf(ChannelListUiState.Empty)
        } else {
            getChannelsUseCase(playlistId).map { list ->
                if (list.isEmpty()) {
                    ChannelListUiState.Empty
                } else {
                    val grouped = list.groupBy { channel ->
                        channel.groupTitle?.takeIf { it.isNotBlank() } ?: DEFAULT_GROUP
                    }
                    ChannelListUiState.Content(grouped)
                }
            }
        }
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = if (playlistId == null) {
                ChannelListUiState.Empty
            } else {
                ChannelListUiState.Loading
            }
        )
}
