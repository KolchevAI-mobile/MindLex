package com.example.mindlex.domain.usecase

import com.example.mindlex.domain.model.CustomDatasetMeta
import com.example.mindlex.domain.model.DatasetImportPayload
import com.example.mindlex.domain.model.ManualWordEntry
import com.example.mindlex.domain.repository.CustomDatasetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ManageCustomDatasetTest {

    @Test
    fun `import delegates`() = runTest {
        val meta = CustomDatasetMeta("id", "n", "csv", 3, 0L, "uri")
        val repo = mockk<CustomDatasetRepository> {
            coEvery { importDataset(any()) } returns Result.success(meta)
        }
        val r = ManageCustomDataset(repo).importDataset(
            DatasetImportPayload("f", "a,b", "u")
        )
        assertTrue(r.getOrNull() == meta)
    }

    @Test
    fun `observe current meta pipes repository`() = runTest {
        val m = CustomDatasetMeta("1", "x", "t", 1, 1L, "s")
        val repo = mockk<CustomDatasetRepository> {
            every { observeCurrentDatasetMeta() } returns flowOf(m)
            every { observeDatasetHistory() } returns emptyFlow()
        }
        val v = ManageCustomDataset(repo).observeCurrentMeta().first()
        assertEquals(m, v)
    }

    @Test
    fun `import manual delegates`() = runTest {
        val meta = CustomDatasetMeta("id", "Мой", "MANUAL", 2, 0L, "manual://x")
        val entries = listOf(ManualWordEntry("hello", "привет"))
        val repo = mockk<CustomDatasetRepository> {
            coEvery { importManualDataset(entries, "Мой") } returns Result.success(meta)
        }
        val r = ManageCustomDataset(repo).importManualDataset(entries, "Мой")
        assertTrue(r.getOrNull() == meta)
    }

    @Test
    fun `delete calls repo`() = runTest {
        val repo = mockk<CustomDatasetRepository>(relaxed = true) {
            coEvery { deleteDataset("x") } returns Result.success(Unit)
        }
        ManageCustomDataset(repo).deleteDataset("x")
        coVerify { repo.deleteDataset("x") }
    }
}
