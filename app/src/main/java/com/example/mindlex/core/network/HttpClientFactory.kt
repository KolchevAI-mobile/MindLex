package com.example.mindlex.core.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import java.util.concurrent.TimeUnit

object HttpClientFactory {
    fun create(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val networkErrorInterceptor = Interceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (e: IOException) {
                throw e
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(NetworkConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(networkErrorInterceptor)
            .build()
    }
}