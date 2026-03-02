package com.example.mindlex.data.remote.api

import com.example.mindlex.data.remote.api.models.WordResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryApi {
    @GET("{language}/{word}")
    suspend fun getWordDefinitions(
        @Path("language") language: String,
        @Path("word") word: String
    ): List<WordResponse>
}
