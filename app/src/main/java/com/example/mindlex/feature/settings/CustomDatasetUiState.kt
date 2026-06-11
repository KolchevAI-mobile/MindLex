package com.example.mindlex.feature.settings

import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.ManualWordEntry

enum class CustomDatasetTab {
    BUILDER,
    IMPORT_FILE
}

/** Экран своего датасета: конструктор, импорт и история. */
data class CustomDatasetUiState(
    val isLoading: Boolean = false,
    val selectedTab: CustomDatasetTab = CustomDatasetTab.BUILDER,
    val currentMeta: CustomDatasetMeta? = null,
    val history: List<CustomDatasetMeta> = emptyList(),
    val builderWord: String = "",
    val builderTranslation: String = "",
    val builderEntries: List<ManualWordEntry> = emptyList(),
    val builderName: String = "",
    val message: String? = null,
    val error: String? = null
) {
    val isCustomModeActive: Boolean
        get() = currentMeta != null

    val canSaveBuilder: Boolean
        get() = builderEntries.isNotEmpty() && !isLoading
}
