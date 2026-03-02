package com.example.mindlex.data.remote.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Definition(
    val definition: String,

    val example: String? = null,

    val synonyms: List<String> = emptyList(),

    val antonyms: List<String> = emptyList()
)
