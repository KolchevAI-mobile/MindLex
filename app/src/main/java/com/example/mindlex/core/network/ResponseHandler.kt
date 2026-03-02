package com.example.mindlex.core.network

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class HttpError(val code: Int, val message: String?) : NetworkResult<Nothing>()
    data class NetworkError(val throwable: Throwable) : NetworkResult<Nothing>()
    data class SerializationError(val throwable: Throwable) : NetworkResult<Nothing>()
}

suspend inline fun <T> safeApiCall(
    crossinline block: suspend () -> T
): NetworkResult<T> {
    return try {
        val result = block()
        NetworkResult.Success(result)
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        NetworkResult.HttpError(e.code(), e.message())
    } catch (e: IOException) {
        NetworkResult.NetworkError(e)
    } catch (e: Exception) {
        NetworkResult.SerializationError(e)
    }
}
