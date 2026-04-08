package com.example.mindlex.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.notifications.StudyNotificationScheduler
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.usecase.CalculateRecommendedTimes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    private val calculateRecommendedTimes: CalculateRecommendedTimes
) : ViewModel() {

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

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = combine(
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

    fun onNotificationsToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            rescheduleNotifications()
            showSaveMessage(if (enabled) "Уведомления включены" else "Уведомления выключены")
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
        _uiState.value = _uiState.value.copy(saveMessage = message)
        viewModelScope.launch {
            delay(2000)
            _uiState.value = _uiState.value.copy(saveMessage = null)
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }

    private suspend fun rescheduleNotifications() {
        scheduler.rescheduleDailyNotifications(
            notificationsEnabled = settingsRepository.isNotificationsEnabled().firstOrNull() == true,
            preferredStudyTime = settingsRepository.getPreferredStudyTime().firstOrNull() ?: LocalTime(15, 0, 0),
            dailyGoal = settingsRepository.getDailyGoal().firstOrNull() ?: 10
        )
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
        CategoryOption("family", "Семья")
    )

    fun getDisplayName(code: String): String =
        ALL.find { it.code == code }?.displayName ?: code
}

data class CategoryOption(val code: String, val displayName: String)
