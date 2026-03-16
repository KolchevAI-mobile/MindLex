package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Ответ пользователя при активном вспоминании.
 *
 * @property wordId ID слова
 * @property userInput Введённый пользователем текст
 * @property isCorrect Правильный ли ответ
 * @property responseTimeMs Время ответа в миллисекундах
 * @property timestamp Время создания ответа
 */
@Serializable
data class UserAnswer(
    val wordId: String,
    val userInput: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val timestamp: Instant
)
