package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.ClozeRepository
import javax.inject.Inject

class GetNextClozeExercise @Inject constructor(
    private val clozeRepository: ClozeRepository
) {

    suspend operator fun invoke(excludedIds: Set<String>): Result<Pair<ClozeExercise, Word>> =
        clozeRepository.getNextExercise(excludedIds)
}
