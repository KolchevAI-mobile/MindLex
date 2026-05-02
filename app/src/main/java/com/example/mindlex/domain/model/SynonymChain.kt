package com.example.mindlex.domain.model

data class SynonymChain(
    val id: String,
    val chainId: String,
    val stepNumber: Int,
    val word: String,
    val validSynonyms: List<String>,
    val difficulty: Int,
    val category: String
)
