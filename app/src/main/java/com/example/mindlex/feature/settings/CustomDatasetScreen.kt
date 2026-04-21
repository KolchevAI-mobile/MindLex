package com.example.mindlex.feature.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mindlex.domain.model.VocabularySource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatasetScreen(
    onBackClick: () -> Unit,
    viewModel: CustomDatasetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importFromUri(uri)
            }
        }
    )

    LaunchedEffect(uiState.message, uiState.error) {
        val text = uiState.error ?: uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(text)
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Свой датасет") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (uiState.source == VocabularySource.CUSTOM) {
                    "Источник слов: пользовательский датасет"
                } else {
                    "Источник слов: Supabase"
                },
                style = MaterialTheme.typography.titleMedium
            )

            uiState.meta?.let { meta ->
                Text(
                    text = "Файл: ${meta.displayName}\nФормат: ${meta.format}\nЗаписей: ${meta.recordsCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            } ?: Text(
                text = "Пользовательский датасет еще не загружен.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (uiState.isLoading) {
                CircularProgressIndicator()
            }

            Button(
                onClick = { filePickerLauncher.launch(arrayOf("text/*", "application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("Загрузить датасет (CSV/JSON)")
            }

            OutlinedButton(
                onClick = { filePickerLauncher.launch(arrayOf("text/*", "application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                Text("Обновить датасет")
            }

            OutlinedButton(
                onClick = viewModel::deleteDataset,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.meta != null
            ) {
                Text("Удалить датасет")
            }
        }
    }
}

