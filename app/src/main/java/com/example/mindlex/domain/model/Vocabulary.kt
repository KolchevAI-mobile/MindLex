package com.example.mindlex.domain.model

data class Vocabulary(
    val id: String,
    val targetLanguage: String,
    val word: String,
    val translation: String,
    val example: String?,
    val phonetic: String?,
    val partOfSpeech: String?,
    val category: String,
    
    val synonymsForeign: List<String> = emptyList()
)
