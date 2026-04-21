package com.example.mindlex.domain.model

data class CustomDatasetMeta(
    val displayName: String,
    val format: String,
    val recordsCount: Int,
    val importedAtEpochMillis: Long
)

