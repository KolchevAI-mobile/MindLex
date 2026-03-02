package com.example.mindlex.data.remote.api.models

import kotlinx.serialization.Serializable

@Serializable
data class WordResponse(
    val word: String,

    val phonetic: String? = null,

    val phonetics: List<Phonetic> = emptyList(),

    val origin: String? = null,

    val meanings: List<Meaning> = emptyList()
)

@Serializable
data class Phonetic(
    val text: String? = null,
    val audio: String? = null
)
