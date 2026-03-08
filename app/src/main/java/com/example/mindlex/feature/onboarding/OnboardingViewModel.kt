package com.example.mindlex.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.domain.usecase.CompleteOnboarding
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboarding: CompleteOnboarding
) : ViewModel() {

    data class UiState(
        val userName: String = "",
        val selectedLanguage: String = "en",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val completed: Boolean = false
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onNameChanged(name: String) {
        _uiState.update { it.copy(userName = name, errorMessage = null) }
    }

    fun onLanguageSelected(language: String) {
        _uiState.update { it.copy(selectedLanguage = language, errorMessage = null) }
    }

    fun onCompleteClick() {
        val current = _uiState.value
        if (current.userName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите имя", isLoading = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = completeOnboarding(current.userName, current.selectedLanguage)

            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, completed = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Ошибка при сохранении настроек"
                        )
                    }
                }
        }
    }
}
