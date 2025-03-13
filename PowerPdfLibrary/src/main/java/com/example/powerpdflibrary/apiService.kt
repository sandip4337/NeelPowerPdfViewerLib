package com.example.powerpdflibrary

import retrofit2.http.GET
import retrofit2.http.Url
import okhttp3.ResponseBody
import retrofit2.http.Streaming

interface ApiService {
    @GET
    @Streaming
    suspend fun downloadPdf(@Url fileUrl: String): ResponseBody
}
