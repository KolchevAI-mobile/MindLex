package com.example.mindlex.feature.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.repository.CustomDatasetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CustomDatasetViewModel @Inject constructor(
    private val customDatasetRepository: CustomDatasetRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val currentMeta: CustomDatasetMeta? = null,
        val history: List<CustomDatasetMeta> = emptyList(),
        val message: String? = null,
        val error: String? = null
    )

    private val localState = MutableStateFlow(UiState())

    val uiState: StateFlow<UiState> = combine(
        localState,
        customDatasetRepository.observeCurrentDatasetMeta(),
        customDatasetRepository.observeDatasetHistory()
    ) { local, currentMeta, history ->
        local.copy(currentMeta = currentMeta, history = history)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            localState.update { it.copy(isLoading = true, error = null, message = null) }
            val payload = runCatching { readPayload(uri) }.getOrElse { error ->
                localState.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Не удалось прочитать файл."
                    )
                }
                return@launch
            }

            customDatasetRepository.importDataset(payload)
                .onSuccess { importedMeta ->
                    localState.update {
                        it.copy(
                            isLoading = false,
                            message = "Датасет загружен: ${importedMeta.displayName}",
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    localState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка импорта датасета."
                        )
                    }
                }
        }
    }

    fun refreshDataset(datasetId: String) {
        viewModelScope.launch {
            localState.update { it.copy(isLoading = true, error = null, message = null) }
            customDatasetRepository.refreshDataset(datasetId)
                .onSuccess { meta ->
                    localState.update {
                        it.copy(
                            isLoading = false,
                            message = "Датасет обновлен: ${meta.displayName}"
                        )
                    }
                }
                .onFailure { error ->
                    localState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Не удалось обновить датасет."
                        )
                    }
                }
        }
    }

    fun deleteDataset(datasetId: String) {
        viewModelScope.launch {
            localState.update { it.copy(isLoading = true, error = null, message = null) }
            customDatasetRepository.deleteDataset(datasetId)
                .onSuccess {
                    localState.update {
                        it.copy(
                            isLoading = false,
                            message = "Датасет удален."
                        )
                    }
                }
                .onFailure { error ->
                    localState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Не удалось удалить датасет."
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        localState.update { it.copy(message = null, error = null) }
    }

    private fun readPayload(uri: Uri): DatasetImportPayload {
        val contentResolver = appContext.contentResolver
        try {
            appContext.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
            // Ignore; some providers do not grant persistable permissions.
        }
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        } ?: "dataset"

        val raw = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalArgumentException("Не удалось открыть файл.")

        return DatasetImportPayload(
            fileName = fileName,
            rawContent = raw,
            sourceUri = uri.toString()
        )
    }
}

