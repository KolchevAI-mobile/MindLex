package com.example.mindlex.core.network

object NetworkConstants {
    const val BASE_URL = "https://api.dictionaryapi.dev/api/v2/entries/"
    const val DEFAULT_LANGUAGE = "en"
    private const val TIMEOUT_SECONDS = 30L
    const val CONNECT_TIMEOUT = TIMEOUT_SECONDS
    const val READ_TIMEOUT = TIMEOUT_SECONDS
    const val WRITE_TIMEOUT = TIMEOUT_SECONDS
}
