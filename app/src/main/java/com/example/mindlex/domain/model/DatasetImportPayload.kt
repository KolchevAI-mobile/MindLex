package com.example.mindlex.domain.model

data class DatasetImportPayload(
    val fileName: String,
    val rawContent: String,
    val sourceUri: String
)

