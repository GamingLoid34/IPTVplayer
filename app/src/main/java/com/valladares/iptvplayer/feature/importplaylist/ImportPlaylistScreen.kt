package com.valladares.iptvplayer.feature.importplaylist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valladares.iptvplayer.R
import com.valladares.iptvplayer.data.playlist.model.PlaylistSourceType

/**
 * Form screen for importing a playlist from URL or a local file.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportPlaylistScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportPlaylistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val sourceType by viewModel.sourceType.collectAsStateWithLifecycle()
    val urlInput by viewModel.urlInput.collectAsStateWithLifecycle()
    val fileUri by viewModel.fileUri.collectAsStateWithLifecycle()
    val serverUrl by viewModel.serverUrl.collectAsStateWithLifecycle()
    val xtreamUsername by viewModel.xtreamUsername.collectAsStateWithLifecycle()
    val xtreamPassword by viewModel.xtreamPassword.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showPasteDialog by rememberSaveable { mutableStateOf(false) }
    var pastedM3uUrl by rememberSaveable { mutableStateOf("") }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        viewModel.onFileSelected(uri?.toString())
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            ImportUiState.Success -> onDone()
            is ImportUiState.Error -> {
                val rawMessage = (uiState as ImportUiState.Error).message
                val errorMessage = rawMessage.toErrorMessage(context)
                snackbarHostState.showSnackbar(errorMessage)
            }

            else -> Unit
        }
    }

    val isLoading = uiState is ImportUiState.Loading
    val isFormValid = name.isNotBlank() && when (sourceType) {
        PlaylistSourceType.URL -> urlInput.isNotBlank()
        PlaylistSourceType.FILE -> !fileUri.isNullOrBlank()
        PlaylistSourceType.XTREAM -> serverUrl.isNotBlank() &&
            xtreamUsername.isNotBlank() &&
            xtreamPassword.isNotBlank()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text(text = stringResource(R.string.import_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading
            )

            SourceTypeSelector(
                selected = sourceType,
                enabled = !isLoading,
                onSelect = viewModel::onSourceTypeChange
            )

            when (sourceType) {
                PlaylistSourceType.URL -> {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = viewModel::onUrlChange,
                        label = { Text(text = stringResource(R.string.import_url_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }

                PlaylistSourceType.FILE -> {
                    Button(
                        onClick = {
                            filePicker.launch(
                                arrayOf(
                                    "audio/x-mpegurl",
                                    "application/x-mpegurl",
                                    "audio/mpegurl",
                                    "application/vnd.apple.mpegurl",
                                    "text/plain",
                                    "*/*"
                                )
                            )
                        },
                        enabled = !isLoading
                    ) {
                        Text(text = stringResource(R.string.import_select_file))
                    }
                    if (!fileUri.isNullOrBlank()) {
                        val selectedFileUri = fileUri.orEmpty()
                        Text(
                            text = Uri.parse(selectedFileUri).lastPathSegment ?: selectedFileUri,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                PlaylistSourceType.XTREAM -> {
                    OutlinedTextField(
                        value = serverUrl,
                        onValueChange = viewModel::onServerUrlChange,
                        label = { Text(text = stringResource(R.string.import_xtream_server_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = xtreamUsername,
                        onValueChange = viewModel::onXtreamUsernameChange,
                        label = { Text(text = stringResource(R.string.import_xtream_username_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    OutlinedTextField(
                        value = xtreamPassword,
                        onValueChange = viewModel::onXtreamPasswordChange,
                        label = { Text(text = stringResource(R.string.import_xtream_password_label)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    TextButton(
                        onClick = { showPasteDialog = true },
                        enabled = !isLoading
                    ) {
                        Text(text = stringResource(R.string.import_xtream_paste_m3u))
                    }
                    Text(
                        text = stringResource(R.string.import_xtream_paste_m3u_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = viewModel::onImportClick,
                enabled = !isLoading && isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.import_button))
            }

            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(text = stringResource(R.string.import_loading))
                }
                if (sourceType == PlaylistSourceType.XTREAM) {
                    Text(
                        text = stringResource(R.string.import_xtream_loading_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState is ImportUiState.Error) {
                TextButton(onClick = viewModel::onResetError) {
                    Text(text = stringResource(R.string.import_retry))
                }
            }
        }
    }

    if (showPasteDialog) {
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = { Text(text = stringResource(R.string.import_xtream_paste_m3u)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.import_xtream_paste_m3u_hint))
                    OutlinedTextField(
                        value = pastedM3uUrl,
                        onValueChange = { pastedM3uUrl = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onPasteM3uUrl(pastedM3uUrl)
                    showPasteDialog = false
                }) {
                    Text(text = stringResource(R.string.import_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text(text = stringResource(R.string.back))
                }
            }
        )
    }
}

@Composable
private fun SourceTypeSelector(
    selected: PlaylistSourceType,
    enabled: Boolean,
    onSelect: (PlaylistSourceType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row {
            RadioButton(
                selected = selected == PlaylistSourceType.URL,
                onClick = { onSelect(PlaylistSourceType.URL) },
                enabled = enabled
            )
            Text(
                text = stringResource(R.string.import_source_m3u_url),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Row {
            RadioButton(
                selected = selected == PlaylistSourceType.FILE,
                onClick = { onSelect(PlaylistSourceType.FILE) },
                enabled = enabled
            )
            Text(
                text = stringResource(R.string.import_source_m3u_file),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        Row {
            RadioButton(
                selected = selected == PlaylistSourceType.XTREAM,
                onClick = { onSelect(PlaylistSourceType.XTREAM) },
                enabled = enabled
            )
            Text(
                text = stringResource(R.string.import_source_xtream),
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private fun String.toErrorMessage(context: android.content.Context): String = when (this) {
    "import_error_name" -> context.getString(R.string.import_error_name)
    "import_error_url" -> context.getString(R.string.import_error_url)
    "import_error_file" -> context.getString(R.string.import_error_file)
    "import_error_xtream_invalid_url" -> context.getString(R.string.import_error_xtream_invalid_url)
    "import_error_xtream_server_required" -> context.getString(R.string.import_error_xtream_server_required)
    "import_error_xtream_username_required" -> context.getString(R.string.import_error_xtream_username_required)
    "import_error_xtream_password_required" -> context.getString(R.string.import_error_xtream_password_required)
    "import_error_xtream_auth_invalid_credentials" -> context.getString(R.string.import_error_xtream_auth_invalid_credentials)
    "import_error_xtream_auth_expired" -> context.getString(R.string.import_error_xtream_auth_expired)
    "import_error_xtream_auth_disabled" -> context.getString(R.string.import_error_xtream_auth_disabled)
    "import_error_xtream_network" -> context.getString(R.string.import_error_xtream_network)
    "import_error_xtream_server_error" -> context.getString(R.string.import_error_xtream_server_error)
    "import_error_xtream_unknown" -> context.getString(R.string.import_error_xtream_unknown)
    else -> this
}
