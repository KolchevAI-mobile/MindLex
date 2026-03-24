package com.example.mindlex.domain.usecase

import javax.inject.Inject

/** Проверка, что введённый ответ входит в список валидных синонимов. */
class ValidateSynonym @Inject constructor() {
    operator fun invoke(userInput: String, validSynonyms: List<String>): Boolean {
        val normalized = userInput.trim()
        if (normalized.isBlank()) return false

        return validSynonyms.any { synonym ->
            synonym.equals(normalized, ignoreCase = true)
        }
    }
}
