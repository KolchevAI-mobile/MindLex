package com.example.mindlex.domain.model

/**
 * Доменная модель упражнения «заполни пропуск в предложении».
 */
data class ClozeExercise(
    val id: String,
    val sentenceWithBlank: String,
    val correctAnswer: String,
    val hint: String,
    val category: String
)
