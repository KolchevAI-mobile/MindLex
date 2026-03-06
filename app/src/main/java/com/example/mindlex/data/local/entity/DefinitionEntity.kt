package com.example.mindlex.data.local.entity

import kotlinx.serialization.Serializable

@Serializable
data class DefinitionEntity(
    val definition: String,
    val example: String? = null,
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList()
)