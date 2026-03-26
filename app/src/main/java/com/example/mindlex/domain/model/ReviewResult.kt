package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Результат повторения слова.
 *
 * @property wordId ID слова
 * @property quality Качество ответа (1-5, где 5 = отлично)
 * @property nextReviewAt Время следующего повторения
 * @property newStatus Новый статус слова
 */
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
