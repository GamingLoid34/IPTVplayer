package com.valladares.iptvplayer.feature.playlistsettings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.valladares.iptvplayer.R
import kotlinx.coroutines.launch

/**
 * Playlist-level playback settings (User-Agent and Referer).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlaylistSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaylistSettingsViewModel = hiltViewModel()
) {
    val playlistName by viewModel.playlistName.collectAsStateWithLifecycle()
    val userAgent by viewModel.userAgent.collectAsStateWithLifecycle()
    val referer by viewModel.referer.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.playlist_settings_saved)
    val saveErrorMessage = stringResource(R.string.playlist_settings_save_error)

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = R.string.playlist_settings_title,
                            playlistName
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(R.string.playlist_settings_user_agent_section))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserAgentPreset.entries.forEach { preset ->
                    AssistChip(
                        onClick = { viewModel.onSelectPreset(preset) },
                        label = {
                            Text(
                                text = if (preset == UserAgentPreset.CUSTOM) {
                                    stringResource(R.string.playlist_settings_preset_custom)
                                } else {
                                    preset.label
                                }
                            )
                        }
                    )
                }
            }
            OutlinedTextField(
                value = userAgent,
                onValueChange = viewModel::onUserAgentChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(text = stringResource(R.string.playlist_settings_user_agent_hint))

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = stringResource(R.string.playlist_settings_referer_section))
            OutlinedTextField(
                value = referer,
                onValueChange = viewModel::onRefererChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text(text = stringResource(R.string.playlist_settings_referer_hint))

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        val result = viewModel.save()
                        if (result.isSuccess) {
                            snackbarHostState.showSnackbar(
                                message = savedMessage
                            )
                            onBack()
                        } else {
                            snackbarHostState.showSnackbar(
                                message = saveErrorMessage
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.playlist_settings_save))
            }
        }
    }
}
