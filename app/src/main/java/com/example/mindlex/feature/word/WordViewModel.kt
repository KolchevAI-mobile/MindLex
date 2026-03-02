package com.example.mindlex.feature.word

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindlex.core.network.NetworkResult
import com.example.mindlex.data.remote.api.models.WordResponse
import com.example.mindlex.data.repository.DictionaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class WordViewModel @Inject constructor(
    private val repository: DictionaryRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val data: List<WordResponse>? = null,
        val errorMessage: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun loadWord(word: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.getWordDefinitions(word).collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                data = result.data,
                                errorMessage = null
                            )
                        }
                    }

                    is NetworkResult.HttpError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "HTTP ошибка: ${result.code} ${result.message.orEmpty()}",
                                data = null
                            )
                        }
                    }

                    is NetworkResult.NetworkError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Проблемы с сетью. Проверьте подключение к интернету.",
                                data = null
                            )
                        }
                    }

                    is NetworkResult.SerializationError -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Ошибка обработки ответа сервера.",
                                data = null
                            )
                        }
                    }
                }
            }
        }
    }
}