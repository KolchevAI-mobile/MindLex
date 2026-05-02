package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ReviewResult(
    val wordId: String,
    val quality: Int,
    val nextReviewAt: Instant,
    val newStatus: WordStatus,
    val newLevel: Int = 0,
    val newEaseFactor: Double = 2.5,
    val intervalDays: Int = 0
)
