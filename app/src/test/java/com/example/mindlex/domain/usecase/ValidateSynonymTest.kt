package com.example.mindlex.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateSynonymTest {

    private val validate = ValidateSynonym()

    @Test
    fun `matches exact ignoring case`() {
        assertTrue(validate("Hello", listOf("hello", "hi")))
    }

    @Test
    fun `matches one of comma separated variants`() {
        assertTrue(validate("b", listOf("a, b, c")))
    }

    @Test
    fun `rejects blank input`() {
        assertFalse(validate("   ", listOf("a")))
    }

    @Test
    fun `rejects when no overlap`() {
        assertFalse(validate("x", listOf("a", "b")))
    }

    @Test
    fun `ignores diacritics`() {
        assertTrue(validate("resume", listOf("résumé")))
    }
}
