package com.valladares.iptvplayer.feature.importplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.core.network.PlaylistContentFetcher
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType
import com.valladares.iptvplayer.domain.usecase.ImportPlaylistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for playlist import flow.
 */
sealed interface ImportUiState {
    /**
     * Idle form state.
     */
    data object Idle : ImportUiState

    /**
     * Import work is running.
     */
    data object Loading : ImportUiState

    /**
     * Import failed with a user-facing [message].
     */
    data class Error(val message: String) : ImportUiState

    /**
     * Import finished successfully.
     */
    data object Success : ImportUiState
}

/**
 * Handles import form state, validation, content fetch and persistence.
 */
@HiltViewModel
class ImportPlaylistViewModel @Inject constructor(
    private val importPlaylistUseCase: ImportPlaylistUseCase,
    private val contentFetcher: PlaylistContentFetcher
) : ViewModel() {
    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    private val _name = MutableStateFlow("")
    private val _sourceType = MutableStateFlow(PlaylistSourceType.URL)
    private val _urlInput = MutableStateFlow("")
    private val _fileUri = MutableStateFlow<String?>(null)

    /**
     * Current async result state for the import flow.
     */
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    /**
     * Playlist display name input.
     */
    val name: StateFlow<String> = _name.asStateFlow()

    /**
     * Selected source type (URL or FILE).
     */
    val sourceType: StateFlow<PlaylistSourceType> = _sourceType.asStateFlow()

    /**
     * URL text input.
     */
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    /**
     * Selected SAF URI as string when importing from file.
     */
    val fileUri: StateFlow<String?> = _fileUri.asStateFlow()

    /**
     * Updates playlist name field.
     */
    fun onNameChange(value: String) {
        _name.value = value
    }

    /**
     * Updates source type field.
     */
    fun onSourceTypeChange(type: PlaylistSourceType) {
        _sourceType.value = type
    }

    /**
     * Updates URL input field.
     */
    fun onUrlChange(value: String) {
        _urlInput.value = value
    }

    /**
     * Stores selected SAF URI string.
     */
    fun onFileSelected(uri: String?) {
        _fileUri.value = uri
    }

    /**
     * Clears current error and returns to idle state.
     */
    fun onResetError() {
        _uiState.value = ImportUiState.Idle
    }

    /**
     * Validates fields, fetches playlist content and persists it through the use case.
     */
    fun onImportClick() {
        viewModelScope.launch {
            val trimmedName = name.value.trim()
            val selectedSourceType = sourceType.value
            val trimmedUrl = urlInput.value.trim()
            val selectedFileUri = fileUri.value

            val validationError = validateInputs(
                name = trimmedName,
                sourceType = selectedSourceType,
                url = trimmedUrl,
                fileUri = selectedFileUri
            )
            if (validationError != null) {
                _uiState.value = ImportUiState.Error(validationError)
                return@launch
            }

            _uiState.value = ImportUiState.Loading

            val sourceUri = when (selectedSourceType) {
                PlaylistSourceType.URL -> trimmedUrl
                PlaylistSourceType.FILE -> selectedFileUri.orEmpty()
            }

            val contentResult = withContext(Dispatchers.IO) {
                when (selectedSourceType) {
                    PlaylistSourceType.URL -> contentFetcher.fetchFromUrl(trimmedUrl)
                    PlaylistSourceType.FILE -> contentFetcher.fetchFromUri(sourceUri)
                }
            }

            val content = contentResult.getOrElse { error ->
                _uiState.value = ImportUiState.Error(
                    error.message ?: "Kunde inte läsa spellistans innehåll"
                )
                return@launch
            }

            val importResult = withContext(Dispatchers.IO) {
                importPlaylistUseCase(
                    name = trimmedName,
                    sourceType = selectedSourceType,
                    sourceUri = sourceUri,
                    content = content
                )
            }

            _uiState.value = if (importResult.isSuccess) {
                ImportUiState.Success
            } else {
                ImportUiState.Error(
                    importResult.exceptionOrNull()?.message
                        ?: "Importen misslyckades"
                )
            }
        }
    }

    private fun validateInputs(
        name: String,
        sourceType: PlaylistSourceType,
        url: String,
        fileUri: String?
    ): String? {
        if (name.isBlank()) {
            return "import_error_name"
        }
        if (sourceType == PlaylistSourceType.URL && url.isBlank()) {
            return "import_error_url"
        }
        if (sourceType == PlaylistSourceType.FILE && fileUri.isNullOrBlank()) {
            return "import_error_file"
        }
        return null
    }
}
