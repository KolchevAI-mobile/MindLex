package com.example.mindlex.feature.settings

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.notifications.StudyNotificationScheduler
import com.example.mindlex.core.constants.LearningDefaults
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.CalculateRecommendedTimes
import com.example.mindlex.widget.StudyWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val scheduler: StudyNotificationScheduler,
    private val calculateRecommendedTimes: CalculateRecommendedTimes,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    sealed interface NotificationPermissionUiEvent {
        data object ShowRationaleSnackbar : NotificationPermissionUiEvent
    }

    data class UiState(
        val userName: String = "",
        val selectedLanguage: String = "en",
        val selectedCategory: String = "general",
        val dailyGoal: Int = 10,
        val notificationsEnabled: Boolean = true,
        val preferredStudyTime: LocalTime = LocalTime(15, 0, 0),
        val isLoading: Boolean = false,
        val saveMessage: String? = null
    )

    private val _notificationPermissionEvents =
        MutableSharedFlow<NotificationPermissionUiEvent>(extraBufferCapacity = 1)
    val notificationPermissionEvents = _notificationPermissionEvents.asSharedFlow()

    private val settingsBaseFlow = combine(
        combine(
            settingsRepository.getUserName(),
            settingsRepository.getSelectedLanguage(),
            settingsRepository.getSelectedCategory()
        ) { userName, language, category ->
            Triple(userName, language, category)
        },
        combine(
            settingsRepository.getDailyGoal(),
            settingsRepository.isNotificationsEnabled(),
            settingsRepository.getPreferredStudyTime()
        ) { goal, notifications, preferredTime ->
            Triple(goal, notifications, preferredTime)
        }
    ) { profile, preferences ->
        UiState(
            userName = profile.first,
            selectedLanguage = profile.second,
            selectedCategory = profile.third,
            dailyGoal = preferences.first,
            notificationsEnabled = preferences.second,
            preferredStudyTime = preferences.third,
            isLoading = false,
            saveMessage = null
        )
    }

    private val saveMessageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<UiState> = combine(settingsBaseFlow, saveMessageFlow) { base, msg ->
        base.copy(saveMessage = msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun onUserNameChange(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
            showSaveMessage("Имя сохранено: $name")
        }
    }

    fun onLanguageSelected(language: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguage(language)
            showSaveMessage("Язык: ${Languages.getDisplayName(language)}")
        }
    }

    fun onCategorySelected(category: String) {
        viewModelScope.launch {
            settingsRepository.setSelectedCategory(category)
            showSaveMessage("Категория: ${Categories.getDisplayName(category)}")
        }
    }

    fun onDailyGoalChanged(goal: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyGoal(goal)
            rescheduleNotifications()
            showSaveMessage("Цель: $goal слов/день")
        }
    }

    fun onNotificationsDisabledByUser() {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(false)
            settingsRepository.setPostNotificationsPermissionGranted(false)
            rescheduleNotifications()
            showSaveMessage("Уведомления выключены")
        }
    }

    fun onNotificationsEnabledByUser() {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(true)
            settingsRepository.setPostNotificationsPermissionGranted(true)
            rescheduleNotifications()
            showSaveMessage("Уведомления включены")
        }
    }

    fun onNotificationPermissionDenied() {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(false)
            settingsRepository.setPostNotificationsPermissionGranted(false)
            rescheduleNotifications()
            _notificationPermissionEvents.emit(NotificationPermissionUiEvent.ShowRationaleSnackbar)
        }
    }

    fun syncRuntimeNotificationPermission(runtimeGranted: Boolean) {
        viewModelScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                settingsRepository.setPostNotificationsPermissionGranted(runtimeGranted)
            }
            rescheduleNotifications()
        }
    }

    fun onPreferredStudyTimeChanged(time: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setPreferredStudyTime(time)
            rescheduleNotifications()
            showSaveMessage("Время обучения: ${formatTime(time)}")
        }
    }

    fun getRecommendedSessionTimes(preferred: LocalTime, dailyGoal: Int): List<LocalTime> =
        calculateRecommendedTimes(preferred, dailyGoal)

    private fun showSaveMessage(message: String) {
        saveMessageFlow.value = message
        viewModelScope.launch {
            delay(2000)
            saveMessageFlow.value = null
        }
    }

    fun clearSaveMessage() {
        saveMessageFlow.value = null
    }

    private suspend fun rescheduleNotifications() {
        scheduler.rescheduleDailyNotifications(
            notificationsEnabled = settingsRepository.isNotificationsEnabled().firstOrNull() == true,
            preferredStudyTime = settingsRepository.getPreferredStudyTime().firstOrNull() ?: LocalTime(15, 0, 0),
            dailyGoal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
        )
        StudyWidgetUpdater.requestUpdateAll(appContext)
    }

    private fun formatTime(time: LocalTime): String {
        return "%02d:%02d".format(time.hour, time.minute)
    }
}

object Languages {
    val ALL = listOf(
        LanguageOption("en", "English"),
        LanguageOption("de", "Deutsch"),
        LanguageOption("fr", "Français"),
        LanguageOption("es", "Español")
    )

    fun getDisplayName(code: String): String =
        ALL.find { it.code == code }?.displayName ?: code
}

data class LanguageOption(val code: String, val displayName: String)

object Categories {
    val ALL = listOf(
        CategoryOption("general", "Общие"),
        CategoryOption("food", "Еда"),
        CategoryOption("travel", "Путешествия"),
        CategoryOption("business", "Бизнес"),
        CategoryOption("it", "IT"),
        CategoryOption("sport", "Спорт"),
        CategoryOption("family", "Семья"),
        CategoryOption(LearningDefaults.CUSTOM_DATASET_CATEGORY, "Свой датасет")
    )

    fun getDisplayName(code: String): String =
        ALL.find { it.code == code }?.displayName ?: code
}

data class CategoryOption(val code: String, val displayName: String)
