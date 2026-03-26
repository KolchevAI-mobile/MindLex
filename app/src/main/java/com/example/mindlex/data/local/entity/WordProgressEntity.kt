package com.example.mindlex.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import java.util.UUID

/**
 * Room entity для хранения прогресса изучения слов.
 *
 * @property id Уникальный идентификатор записи
 * @property wordId ID слова из словаря
 * @property status Статус изучения ("NEW", "LEARNING", "KNOWN", "REVIEW")
 * @property level Уровень знания (0-5)
 * @property nextReviewAt Время следующего повторения
 * @property lastReviewedAt Время последнего повторения
 * @property correctCount Количество правильных ответов
 * @property incorrectCount Количество неправильных ответов
 * @property createdAt Время создания записи
 * @property updatedAt Время последнего обновления
 */
@Entity(
    tableName = "word_progress",
    indices = [Index(value = ["wordId"], unique = true)]
)
data class WordProgressEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val wordId: String,
    val status: String,
    val level: Int,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0,
    val nextReviewAt: Instant,
    val lastReviewedAt: Instant? = null,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val createdAt: Instant,
    val updatedAt: Instant
)
