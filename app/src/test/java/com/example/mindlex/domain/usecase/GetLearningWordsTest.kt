package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.Vocabulary
import com.example.mindlex.domain.repository.SettingsRepository
import com.example.mindlex.domain.repository.VocabularyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLearningWordsTest {

    @Test
    fun `general uses random words`() = runTest {
        val list = listOf(sampleVoc("1"))
        val repository = mockk<VocabularyRepository> {
            coEvery { getRandomWords(10) } returns flowOf(Result.success(list))
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getSelectedLanguage() } returns flowOf("en")
            coEvery { getSelectedCategory() } returns flowOf("General")
        }
        val r = GetLearningWords(repository, settings).invoke(10).first()
        assertTrue(r.isSuccess)
        assertTrue(r.getOrNull()?.size == 1)
    }

    @Test
    fun `specific category lowercased`() = runTest {
        val list = listOf(sampleVoc("2"))
        val repository = mockk<VocabularyRepository> {
            coEvery { getWordsByCategory("food", 5) } returns flowOf(Result.success(list))
        }
        val settings = mockk<SettingsRepository> {
            coEvery { getSelectedLanguage() } returns flowOf("en")
            coEvery { getSelectedCategory() } returns flowOf("Food")
        }
        val r = GetLearningWords(repository, settings).invoke(5).first()
        assertTrue(r.isSuccess)
    }

    private fun sampleVoc(id: String) = Vocabulary(
        id = id,
        targetLanguage = "en",
        word = "x",
        translation = "y",
        example = null,
        phonetic = null,
        partOfSpeech = null,
        category = "general"
    )
}
