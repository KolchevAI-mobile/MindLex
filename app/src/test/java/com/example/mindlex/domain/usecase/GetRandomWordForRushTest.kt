package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.Word
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRandomWordForRushTest {

    @Test
    fun `uses selected category from settings`() = runTest {
        val word = Word("id1", "run", "бежать", emptyList(), "en", category = "general")
        val vocab = mockk<VocabularyRepository> {
            coEvery {
                getRandomWordByCategoryExcluding("sport", 80, emptySet(), true)
            } returns Result.success(word)
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getSelectedCategory() } returns flowOf("sport")
        }
        val r = GetRandomWordForRush(vocab, settings)(excludedIds = emptySet())
        assertTrue(r.isSuccess)
        assertEquals(word, r.getOrNull())
    }

    @Test
    fun `falls back to general when category is null`() = runTest {
        val word = Word("2", "a", "b", emptyList(), "en")
        val vocab = mockk<VocabularyRepository> {
            coEvery { getRandomWordByCategoryExcluding("general", 80, any(), any()) } returns Result.success(word)
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getSelectedCategory() } returns emptyFlow<String>()
        }
        val r = GetRandomWordForRush(vocab, settings)(excludedIds = setOf("x"))
        assertTrue(r.isSuccess)
    }
}
