package com.valladares.iptvplayer.feature.importplaylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valladares.iptvplayer.core.network.PlaylistContentFetcher
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType
import com.valladares.iptvplayer.domain.usecase.ImportPlaylistUseCase
import com.valladares.iptvplayer.domain.usecase.ImportXtreamPlaylistUseCase
import com.valladares.iptvplayer.data.xtream.model.XtreamCredentials
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
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
    private val importXtreamPlaylistUseCase: ImportXtreamPlaylistUseCase,
    private val contentFetcher: PlaylistContentFetcher
) : ViewModel() {
    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    private val _name = MutableStateFlow("")
    private val _sourceType = MutableStateFlow(PlaylistSourceType.URL)
    private val _urlInput = MutableStateFlow("")
    private val _fileUri = MutableStateFlow<String?>(null)
    private val _serverUrl = MutableStateFlow("")
    private val _xtreamUsername = MutableStateFlow("")
    private val _xtreamPassword = MutableStateFlow("")

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
     * Xtream server URL input.
     */
    val serverUrl: StateFlow<String> = _serverUrl.asStateFlow()

    /**
     * Xtream username input.
     */
    val xtreamUsername: StateFlow<String> = _xtreamUsername.asStateFlow()

    /**
     * Xtream password input.
     */
    val xtreamPassword: StateFlow<String> = _xtreamPassword.asStateFlow()

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
     * Updates Xtream server URL field.
     */
    fun onServerUrlChange(value: String) {
        _serverUrl.value = value
    }

    /**
     * Updates Xtream username field.
     */
    fun onXtreamUsernameChange(value: String) {
        _xtreamUsername.value = value
    }

    /**
     * Updates Xtream password field.
     */
    fun onXtreamPasswordChange(value: String) {
        _xtreamPassword.value = value
    }

    /**
     * Parses an M3U URL and fills Xtream credentials fields when successful.
     */
    fun onPasteM3uUrl(url: String) {
        val result = XtreamCredentials.fromM3uUrl(url)
        result.onSuccess { credentials ->
            _serverUrl.value = credentials.serverUrl
            _xtreamUsername.value = credentials.username
            _xtreamPassword.value = credentials.password
            _sourceType.value = PlaylistSourceType.XTREAM
            _uiState.value = ImportUiState.Idle
        }.onFailure {
            _uiState.value = ImportUiState.Error("import_error_xtream_invalid_url")
        }
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
            val trimmedServerUrl = serverUrl.value.trim()
            val trimmedXtreamUsername = xtreamUsername.value.trim()
            val trimmedXtreamPassword = xtreamPassword.value.trim()

            val validationError = validateInputs(
                name = trimmedName,
                sourceType = selectedSourceType,
                url = trimmedUrl,
                fileUri = selectedFileUri,
                serverUrl = trimmedServerUrl,
                xtreamUsername = trimmedXtreamUsername,
                xtreamPassword = trimmedXtreamPassword
            )
            if (validationError != null) {
                _uiState.value = ImportUiState.Error(validationError)
                return@launch
            }

            _uiState.value = ImportUiState.Loading
            when (selectedSourceType) {
                PlaylistSourceType.URL,
                PlaylistSourceType.FILE -> {
                    val sourceUri = when (selectedSourceType) {
                        PlaylistSourceType.URL -> trimmedUrl
                        PlaylistSourceType.FILE -> selectedFileUri.orEmpty()
                        PlaylistSourceType.XTREAM -> ""
                    }

                    val contentResult = withContext(Dispatchers.IO) {
                        when (selectedSourceType) {
                            PlaylistSourceType.URL -> contentFetcher.fetchFromUrl(trimmedUrl)
                            PlaylistSourceType.FILE -> contentFetcher.fetchFromUri(sourceUri)
                            PlaylistSourceType.XTREAM -> Result.failure(
                                IOException("import_error_xtream_unknown")
                            )
                        }
                    }

                    val content = contentResult.getOrElse { error ->
                        _uiState.value = ImportUiState.Error(
                            error.message ?: "import_error_xtream_unknown"
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
                                ?: "import_error_xtream_unknown"
                        )
                    }
                }

                PlaylistSourceType.XTREAM -> {
                    val credentials = runCatching {
                        XtreamCredentials(
                            serverUrl = trimmedServerUrl,
                            username = trimmedXtreamUsername,
                            password = trimmedXtreamPassword
                        )
                    }.getOrElse {
                        _uiState.value = ImportUiState.Error("import_error_xtream_invalid_url")
                        return@launch
                    }

                    val importResult = withContext(Dispatchers.IO) {
                        importXtreamPlaylistUseCase(
                            name = trimmedName,
                            credentials = credentials
                        )
                    }

                    _uiState.value = if (importResult.isSuccess) {
                        ImportUiState.Success
                    } else {
                        ImportUiState.Error(
                            importResult.exceptionOrNull()?.message
                                ?: "import_error_xtream_unknown"
                        )
                    }
                }
            }
        }
    }

    private fun validateInputs(
        name: String,
        sourceType: PlaylistSourceType,
        url: String,
        fileUri: String?,
        serverUrl: String,
        xtreamUsername: String,
        xtreamPassword: String
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
        if (sourceType == PlaylistSourceType.XTREAM && serverUrl.isBlank()) {
            return "import_error_xtream_server_required"
        }
        if (sourceType == PlaylistSourceType.XTREAM && xtreamUsername.isBlank()) {
            return "import_error_xtream_username_required"
        }
        if (sourceType == PlaylistSourceType.XTREAM && xtreamPassword.isBlank()) {
            return "import_error_xtream_password_required"
        }
        return null
    }
}
