package com.example.mindlex.data.remote.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Meaning(
    @SerialName("partOfSpeech")
    val partOfSpeech: String,

    val definitions: List<Definition> = emptyList(),

    val synonyms: List<String> = emptyList(),

    val antonyms: List<String> = emptyList()
)
