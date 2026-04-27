package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.ClozeExercise
import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.ClozeRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNextClozeExerciseTest {

    @Test
    fun `delegates to repository`() = runTest {
        val ex = ClozeExercise("c1", "The ___ runs", "cat", "hint", "general")
        val w = Word("w1", "cat", "кот", emptyList(), "en")
        val repo = mockk<ClozeRepository> {
            coEvery { getNextExercise(setOf("a")) } returns Result.success(ex to w)
        }
        val r = GetNextClozeExercise(repo)(excludedIds = setOf("a"))
        assertTrue(r.isSuccess)
        assertTrue(r.getOrNull()!!.first == ex && r.getOrNull()!!.second == w)
    }
}
