package com.example.mindlex.feature.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.model.ManualWordEntry
import com.example.mindlex.domain.usecase.ManageCustomDataset
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
    private val manageCustomDataset: ManageCustomDataset,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val local = MutableStateFlow(CustomDatasetUiState())

    val uiState: StateFlow<CustomDatasetUiState> = combine(
        local,
        manageCustomDataset.observeCurrentMeta(),
        manageCustomDataset.observeHistory()
    ) { state, currentMeta, history ->
        state.copy(currentMeta = currentMeta, history = history)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CustomDatasetUiState())

    fun selectTab(tab: CustomDatasetTab) {
        local.update { it.copy(selectedTab = tab, error = null) }
    }

    fun onBuilderWordChange(value: String) {
        local.update { it.copy(builderWord = value) }
    }

    fun onBuilderTranslationChange(value: String) {
        local.update { it.copy(builderTranslation = value) }
    }

    fun onBuilderNameChange(value: String) {
        local.update { it.copy(builderName = value) }
    }

    fun addBuilderEntry() {
        val word = local.value.builderWord.trim()
        val translation = local.value.builderTranslation.trim()
        if (word.isBlank() || translation.isBlank()) {
            local.update { it.copy(error = "Введите слово и перевод.") }
            return
        }
        local.update {
            it.copy(
                builderEntries = it.builderEntries + ManualWordEntry(word, translation),
                builderWord = "",
                builderTranslation = "",
                error = null
            )
        }
    }

    fun removeBuilderEntry(index: Int) {
        local.update { state ->
            state.copy(
                builderEntries = state.builderEntries.filterIndexed { i, _ -> i != index }
            )
        }
    }

    fun saveBuilderDataset() {
        val state = local.value
        if (state.builderEntries.isEmpty()) return
        viewModelScope.launch {
            local.update { it.copy(isLoading = true, error = null, message = null) }
            val name = state.builderName.trim().ifBlank { "Мой словарь" }
            manageCustomDataset.importManualDataset(state.builderEntries, name)
                .onSuccess { meta ->
                    local.update {
                        it.copy(
                            isLoading = false,
                            message = "Словарь сохранён: ${meta.displayName} (${meta.recordsCount} слов)",
                            builderEntries = emptyList(),
                            builderName = "",
                            builderWord = "",
                            builderTranslation = ""
                        )
                    }
                }
                .onFailure { error ->
                    local.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Не удалось сохранить словарь."
                        )
                    }
                }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            local.update { it.copy(isLoading = true, error = null, message = null) }
            val payload = runCatching { readPayload(uri) }.getOrElse { error ->
                local.update {
                    it.copy(isLoading = false, error = error.message ?: "Не удалось прочитать файл.")
                }
                return@launch
            }
            manageCustomDataset.importDataset(payload)
                .onSuccess { meta ->
                    local.update {
                        it.copy(
                            isLoading = false,
                            message = "Импортировано: ${meta.displayName} (${meta.recordsCount} слов)"
                        )
                    }
                }
                .onFailure { error ->
                    local.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка импорта."
                        )
                    }
                }
        }
    }

    fun refreshDataset(datasetId: String) {
        viewModelScope.launch {
            local.update { it.copy(isLoading = true, error = null, message = null) }
            manageCustomDataset.refreshDataset(datasetId)
                .onSuccess { meta ->
                    local.update {
                        it.copy(isLoading = false, message = "Обновлено: ${meta.displayName}")
                    }
                }
                .onFailure { error ->
                    local.update {
                        it.copy(isLoading = false, error = error.message ?: "Не удалось обновить.")
                    }
                }
        }
    }

    fun deleteDataset(datasetId: String) {
        viewModelScope.launch {
            local.update { it.copy(isLoading = true, error = null, message = null) }
            manageCustomDataset.deleteDataset(datasetId)
                .onSuccess {
                    local.update {
                        it.copy(isLoading = false, message = "Датасет удалён. Снова используются слова из сети.")
                    }
                }
                .onFailure { error ->
                    local.update {
                        it.copy(isLoading = false, error = error.message ?: "Не удалось удалить.")
                    }
                }
        }
    }

    fun clearMessage() {
        local.update { it.copy(message = null, error = null) }
    }

    private fun readPayload(uri: Uri): DatasetImportPayload {
        val contentResolver = appContext.contentResolver
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (_: SecurityException) {
        }
        val fileName = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        } ?: "dataset"
        val raw = contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: throw IllegalArgumentException("Не удалось открыть файл.")
        return DatasetImportPayload(fileName = fileName, rawContent = raw, sourceUri = uri.toString())
    }
}
