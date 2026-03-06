package com.example.mindlex.domain.model

import kotlinx.datetime.Instant

data class UserProgress(
    val wordId: String,
    val level: Int,
    val nextReviewAt: Instant,
    val lastReviewedAt: Instant? = null,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0
)
