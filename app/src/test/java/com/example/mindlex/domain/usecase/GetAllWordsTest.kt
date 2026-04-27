package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.WordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetAllWordsTest {

    @Test
    fun `returns repository list`() = runTest {
        val w = Word("1", "cat", "кот", emptyList(), "en")
        val repo = mockk<WordRepository> {
            coEvery { getAllWords() } returns listOf(w)
        }
        val out = GetAllWords(repo)()
        assertEquals(1, out.size)
        assertEquals(w, out.first())
    }
}
