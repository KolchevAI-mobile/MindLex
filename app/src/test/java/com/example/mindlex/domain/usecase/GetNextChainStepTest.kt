package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.SynonymChain
import com.example.mindlex.domain.repository.SynonymChainRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class GetNextChainStepTest {

    private val chain = SynonymChain(
        id = "1",
        chainId = "c",
        stepNumber = 0,
        word = "start",
        validSynonyms = listOf("a", "b"),
        difficulty = 1,
        category = "g"
    )

    @Test
    fun getRandomChainStartStep_delegates() = runTest {
        val repo = mockk<SynonymChainRepository> {
            coEvery { getRandomChainStartStep("en", "food") } returns Result.success(chain)
        }
        val r = GetNextChainStep(repo).getRandomChainStartStep("en", "food")
        assertTrue(r.getOrNull() == chain)
    }

    @Test
    fun getStep_delegates() = runTest {
        val repo = mockk<SynonymChainRepository> {
            coEvery { getChainStep("c", 2, "en") } returns Result.success(null)
        }
        GetNextChainStep(repo).invoke("c", 2, "en")
        coVerify { repo.getChainStep("c", 2, "en") }
    }
}
