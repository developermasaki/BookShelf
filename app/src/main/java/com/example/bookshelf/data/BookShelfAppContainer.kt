package com.example.bookshelf.data

import com.example.bookshelf.network.BookShelfApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

interface AppContainer{
    val bookShelfRepository: BookShelfRepository
}

class BookShelfAppContainer: AppContainer {
    private val baseUrl = "https://www.googleapis.com/books/v1/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: BookShelfApiService by lazy {
        retrofit.create(BookShelfApiService::class.java)
    }

    override val bookShelfRepository: BookShelfRepository by lazy {
        NetworkBookShelfRepository(retrofitService)
    }
}