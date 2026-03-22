package com.example.mindlex.domain.repository

import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.Word

/** Репозиторий упражнений contextual cloze (Supabase `cloze_exercises`). */
interface ClozeRepository {

    /**
     * Следующее упражнение для сессии.
     *
     * @param excludedIds id уже показанных в этой сессии (чтобы не повторять, пока возможно)
     * @return пара: упражнение и [Word] для оценки ответа / ключа [WordProgress]
     */
    suspend fun getNextExercise(excludedIds: Set<String>): Result<Pair<ClozeExercise, Word>>
}
