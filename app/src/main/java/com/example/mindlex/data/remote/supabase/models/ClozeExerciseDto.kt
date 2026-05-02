package com.example.mindlex.data.remote.supabase.models

import com.example.mindlex.domain.model.ClozeExercise
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClozeExerciseDto(
    val id: String,
    @SerialName("sentence_with_blank")
    val sentenceWithBlank: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    val hint: String,
    val category: String? = null
) {

    fun toDomain(): ClozeExercise = ClozeExercise(
        id = id,
        sentenceWithBlank = sentenceWithBlank,
        correctAnswer = correctAnswer.trim(),
        hint = hint,
        category = category ?: "general"
    )
}
