package com.example.mindlex.domain.model

import kotlinx.datetime.Instant

/**
 * Доменная модель прогресса изучения слова.
 */
data class WordProgress(
    val id: String,
    val wordId: String,
    val status: WordStatus,
    val level: Int,
    val nextReviewAt: Instant,
    val lastReviewedAt: Instant?,
    val correctCount: Int,
    val incorrectCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant
)
