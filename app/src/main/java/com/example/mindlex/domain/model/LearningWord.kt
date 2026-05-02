package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val id: String,
    val wordForeign: String,
    val wordNative: String,
    val alternativeTranslations: List<String> = emptyList(),
    val targetLanguage: String,
    val example: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val category: String = "general"
)
