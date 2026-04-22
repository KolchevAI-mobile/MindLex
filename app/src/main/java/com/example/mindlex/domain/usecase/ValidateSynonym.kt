package com.example.mindlex.domain.usecase

import java.text.Normalizer
import javax.inject.Inject

/** Проверка, что введённый ответ входит в список валидных синонимов. */
class ValidateSynonym @Inject constructor() {
    operator fun invoke(userInput: String, validSynonyms: List<String>): Boolean {
        val normalized = normalizeToken(userInput)
        if (normalized.isBlank()) return false

        return validSynonyms.asSequence()
            .flatMap { splitVariants(it).asSequence() }
            .map(::normalizeToken)
            .any { it == normalized }
    }

    private fun splitVariants(value: String): List<String> {
        return value.split(',', ';', '/', '|')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(value) }
    }

    private fun normalizeToken(value: String): String {
        val withoutDiacritics = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")

        return withoutDiacritics
            .lowercase()
            .replace("[^\\p{L}\\p{N}\\s-]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
