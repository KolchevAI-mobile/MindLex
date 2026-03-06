package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val id: String,
    val word: String,
    val translation: String? = null,
    val phonetic: String? = null,
    val partOfSpeech: String? = null,
    val definitions: List<Definition>,
    val createdAt: Instant,
    val updatedAt: Instant
)

@Serializable
data class Definition(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)
