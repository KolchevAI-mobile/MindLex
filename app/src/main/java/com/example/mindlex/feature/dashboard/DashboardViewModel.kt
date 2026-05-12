package com.example.mindlex.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.model.DashboardSnapshot
import com.example.mindlex.domain.model.VocabularySource
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.ObserveDashboard
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    observeDashboard: ObserveDashboard,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardSnapshot> = observeDashboard()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            DashboardSnapshot()
        )

    fun setOfflineCustomDatasetEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                val cat = settingsRepository.getSelectedCategory().first()
                if (cat != LearningDefaults.CUSTOM_DATASET_CATEGORY) {
                    settingsRepository.setLastRemoteCategory(cat)
                }
                settingsRepository.setVocabularySource(VocabularySource.CUSTOM)
                settingsRepository.setSelectedCategory(LearningDefaults.CUSTOM_DATASET_CATEGORY)
            } else {
                settingsRepository.setVocabularySource(VocabularySource.REMOTE)
                val restore = settingsRepository.getLastRemoteCategory().first()
                settingsRepository.setSelectedCategory(restore)
            }
        }
    }
}
