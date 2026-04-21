package com.example.mindlex.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomDatasetMeta(
    val id: String,
    val displayName: String,
    val format: String,
    val recordsCount: Int,
    val importedAtEpochMillis: Long,
    val sourceUri: String
)

