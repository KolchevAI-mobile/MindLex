package com.example.mindlex.domain.model

/** Пара слово–перевод из конструктора датасета. */
data class ManualWordEntry(
    val word: String,
    val translation: String,
    val example: String? = null,
    val phonetic: String? = null
)
