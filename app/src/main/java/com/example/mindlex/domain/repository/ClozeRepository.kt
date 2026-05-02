package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.Word

interface ClozeRepository {

    suspend fun getNextExercise(excludedIds: Set<String>): Result<Pair<ClozeExercise, Word>>
}
