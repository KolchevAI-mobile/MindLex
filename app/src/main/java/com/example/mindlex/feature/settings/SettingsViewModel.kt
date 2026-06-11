package com.example.mindlex.feature.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.notifications.StudyNotificationScheduler
import com.example.mindlex.domain.model.VocabularySource
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.CalculateRecommendedTimes
import com.example.mindlex.domain.usecase.ObserveSettings
import com.example.mindlex.widget.StudyWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettings,
    private val settingsRepository: SettingsRepository,
    private val scheduler: StudyNotificationScheduler,
    private val calculateRecommendedTimes: CalculateRecommendedTimes,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    sealed interface PermissionEvent {
        data object OpenSystemSettings : PermissionEvent
    }

    private val saveMessage = MutableStateFlow<String?>(null)
    private val _permissionEvents = MutableSharedFlow<PermissionEvent>(extraBufferCapacity = 1)
    val permissionEvents = _permissionEvents.asSharedFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        observeSettings(),
        saveMessage
    ) { snapshot, message ->
        SettingsUiState.from(snapshot, message)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    fun onUserNameChange(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name.trim())
            flashSaveMessage("Имя сохранено: ${name.trim()}")
        }
    }

    fun onLanguageSelected(language: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguage(language)
            flashSaveMessage("Язык: ${Languages.getDisplayName(language)}")
        }
    }

    fun onCategorySelected(category: String) {
        viewModelScope.launch {
            settingsRepository.setLastRemoteCategory(category)
            settingsRepository.setVocabularySource(VocabularySource.REMOTE)
            settingsRepository.setSelectedCategory(category)
            flashSaveMessage("Категория: ${Categories.getDisplayName(category)}")
        }
    }

    fun onDailyGoalChanged(goal: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyGoal(goal)
            refreshReminders()
            flashSaveMessage("Цель: $goal слов/день")
        }
    }

    fun onNotificationsDisabledByUser() {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(false)
            settingsRepository.setPostNotificationsPermissionGranted(false)
            refreshReminders()
            flashSaveMessage("Уведомления выключены")
        }
    }

    fun onNotificationsEnabledByUser() {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(true)
            settingsRepository.setPostNotificationsPermissionGranted(true)
            refreshReminders()
            flashSaveMessage("Уведомления включены")
        }
    }

    fun onNotificationPermissionDenied() {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(false)
            settingsRepository.setPostNotificationsPermissionGranted(false)
            refreshReminders()
            _permissionEvents.emit(PermissionEvent.OpenSystemSettings)
        }
    }

    fun syncRuntimeNotificationPermission(runtimeGranted: Boolean) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settingsRepository.setPostNotificationsPermissionGranted(runtimeGranted)
            }
            refreshReminders()
        }
    }

    fun onPreferredStudyTimeChanged(time: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setPreferredStudyTime(time)
            refreshReminders()
            flashSaveMessage(
                "Время обучения: ${String.format(Locale.getDefault(), "%02d:%02d", time.hour, time.minute)}"
            )
        }
    }

    fun recommendedSessionTimes(preferred: LocalTime, dailyGoal: Int): List<LocalTime> =
        calculateRecommendedTimes(preferred, dailyGoal)

    fun clearSaveMessage() {
        saveMessage.value = null
    }

    private fun flashSaveMessage(message: String) {
        saveMessage.value = message
    }

    private suspend fun refreshReminders() {
        scheduler.rescheduleDailyNotifications(
            notificationsEnabled = settingsRepository.isNotificationsEnabled().firstOrNull() == true,
            preferredStudyTime = settingsRepository.getPreferredStudyTime().firstOrNull()
                ?: LocalTime(15, 0, 0),
            dailyGoal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
        )
        StudyWidgetUpdater.requestUpdateAll(appContext)
    }
}
