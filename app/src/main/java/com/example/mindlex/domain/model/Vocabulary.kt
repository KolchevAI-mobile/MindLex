package com.example.mindlex.domain.model

/** Доменная модель слова словаря с переводами. */
data class Vocabulary(
    val id: String,
    val targetLanguage: String,
    val word: String,
    val translation: String,
    val example: String?,
    val phonetic: String?,
    val partOfSpeech: String?,
    val category: String,
    /** Разобранные синонимы на языке обучения (из synonyms_en и т.п.). */
    val synonymsForeign: List<String> = emptyList()
)

