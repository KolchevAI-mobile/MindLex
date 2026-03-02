package com.example.mindlex.core.network

import kotlin.time.Duration

object NetworkConstants {
    const val BASE_URL: String = "https://api.dictionaryapi.dev/api/v2/entries/"
    const val DEFAULT_LANGUAGE: String = "en"
    private const val TIMEOUT_SECONDS: Long = 30L

    val CONNECT_TIMEOUT: Long = TIMEOUT_SECONDS
    val READ_TIMEOUT: Long = TIMEOUT_SECONDS
    val WRITE_TIMEOUT: Long = TIMEOUT_SECONDS
}