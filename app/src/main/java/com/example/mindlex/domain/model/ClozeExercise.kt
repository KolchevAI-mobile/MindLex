package com.example.mindlex.domain.model

data class ClozeExercise(
    val id: String,
    val sentenceWithBlank: String,
    val correctAnswer: String,
    val hint: String,
    val category: String
)
