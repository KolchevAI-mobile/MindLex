package com.example.mindlex.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.VocabularySource
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.ObserveDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboard: ObserveDashboard,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val vocabularySwitchBusy = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        observeDashboard(),
        vocabularySwitchBusy
    ) { snapshot, busy ->
        DashboardUiState.from(snapshot, busy)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    fun setOfflineCustomDatasetEnabled(enabled: Boolean) {
        if (vocabularySwitchBusy.value) return
        viewModelScope.launch {
            vocabularySwitchBusy.update { true }
            try {
                if (enabled) {
                    enableCustomVocabulary()
                } else {
                    restoreRemoteVocabulary()
                }
            } finally {
                vocabularySwitchBusy.update { false }
            }
        }
    }

    private suspend fun enableCustomVocabulary() {
        val category = settingsRepository.getSelectedCategory().first()
        if (category != LearningDefaults.CUSTOM_DATASET_CATEGORY) {
            settingsRepository.setLastRemoteCategory(category)
        }
        settingsRepository.setVocabularySource(VocabularySource.CUSTOM)
        settingsRepository.setSelectedCategory(LearningDefaults.CUSTOM_DATASET_CATEGORY)
    }

    private suspend fun restoreRemoteVocabulary() {
        settingsRepository.setVocabularySource(VocabularySource.REMOTE)
        val category = settingsRepository.getLastRemoteCategory().first()
        settingsRepository.setSelectedCategory(category)
    }
}
