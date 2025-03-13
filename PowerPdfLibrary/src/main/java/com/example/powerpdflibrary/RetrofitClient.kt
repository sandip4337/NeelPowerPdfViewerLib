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

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/")
        .client(client)  // Apply timeout settings
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: com.example.powerpdflibrary.ApiService = retrofit.create(com.example.powerpdflibrary.ApiService::class.java)
}