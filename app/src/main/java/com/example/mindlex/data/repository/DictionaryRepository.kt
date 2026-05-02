package com.example.mindlex.data.repository

import com.example.mindlex.core.network.NetworkConstants
import com.example.mindlex.core.network.NetworkResult
import com.example.mindlex.core.network.safeApiCall
import com.example.mindlex.data.remote.api.DictionaryApi
import com.example.mindlex.data.remote.api.models.WordResponse
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DictionaryRepository @Inject constructor(
    private val api: DictionaryApi
) {
    fun getWordDefinitions(
        word: String,
        language: String = NetworkConstants.DEFAULT_LANGUAGE
    ): Flow<NetworkResult<List<WordResponse>>> = flow {
        val result = safeApiCall {
            api.getWordDefinitions(language = language, word = word)
        }
        emit(result)
    }
}
