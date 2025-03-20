package com.example.powerpdflibrary

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)  // Allow automatic redirects
        .followSslRedirects(true)
        .connectTimeout(120, TimeUnit.SECONDS)  // Increase timeout to 2 minutes
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var retrofit: Retrofit? = null

    fun getApiService(baseUrl: String): ApiService {
        return retrofit?.create(ApiService::class.java) ?: synchronized(this) {
            val newRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofit = newRetrofit
            newRetrofit.create(ApiService::class.java)
        }
    }
}