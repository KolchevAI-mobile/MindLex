package com.example.mindlex.domain.model

/** Один шаг цепочки синонимов из таблицы `synonym_chains`. */
data class SynonymChain(
    val id: String,
    val chainId: String,
    val stepNumber: Int,
    val word: String,
    val validSynonyms: List<String>,
    val difficulty: Int,
    val category: String
)
