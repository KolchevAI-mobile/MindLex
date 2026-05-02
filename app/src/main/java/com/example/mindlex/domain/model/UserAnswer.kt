package com.example.mindlex.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserAnswer(
    val wordId: String,
    val userInput: String,
    val isCorrect: Boolean,
    val responseTimeMs: Long,
    val timestamp: Instant
)
