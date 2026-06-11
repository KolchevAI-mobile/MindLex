package com.example.mindlex.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.R
import com.example.mindlex.ui.components.BookOpenDecorLayer
import com.example.mindlex.ui.components.MechanicSessionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatasetScreen(
    onBackClick: () -> Unit,
    viewModel: CustomDatasetViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> if (uri != null) viewModel.importFromUri(uri) }
    )

    LaunchedEffect(state.message, state.error) {
        val text = state.error ?: state.message ?: return@LaunchedEffect
        snackbarHost.showSnackbar(text)
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            MechanicSessionHeader(
                title = stringResource(R.string.custom_dataset_screen_title),
                onBackClick = onBackClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            BookOpenDecorLayer()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                            )
                        )
                    )
            ) {
                CustomDatasetContent(
                    state = state,
                    onTabSelected = viewModel::selectTab,
                    onBuilderWordChange = viewModel::onBuilderWordChange,
                    onBuilderTranslationChange = viewModel::onBuilderTranslationChange,
                    onBuilderNameChange = viewModel::onBuilderNameChange,
                    onAddEntry = viewModel::addBuilderEntry,
                    onRemoveEntry = viewModel::removeBuilderEntry,
                    onSaveBuilder = viewModel::saveBuilderDataset,
                    onImportFile = {
                        filePicker.launch(arrayOf("text/*", "application/json", "text/csv"))
                    },
                    onRefreshDataset = viewModel::refreshDataset,
                    onDeleteDataset = viewModel::deleteDataset
                )
                if (state.isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
