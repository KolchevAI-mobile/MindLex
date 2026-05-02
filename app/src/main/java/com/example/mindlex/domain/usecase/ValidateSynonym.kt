package com.example.mindlex.domain.usecase

import java.text.Normalizer
import javax.inject.Inject

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
            .replace(DIACRITICS_REGEX, "")

        return withoutDiacritics
            .lowercase()
            .replace(NON_WORD_REGEX, " ")
            .replace(MULTIPLE_SPACES_REGEX, " ")
            .trim()
    }

    private companion object {
        val DIACRITICS_REGEX = "\\p{Mn}+".toRegex()
        val NON_WORD_REGEX = "[^\\p{L}\\p{N}\\s-]".toRegex()
        val MULTIPLE_SPACES_REGEX = "\\s+".toRegex()
    }
}
