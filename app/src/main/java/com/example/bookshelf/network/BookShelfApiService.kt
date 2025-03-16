package com.example.bookshelf.network

import com.example.bookshelf.BuildConfig
import com.example.bookshelf.model.BookShelfItems
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface BookShelfApiService {
    @GET("volumes")
    suspend fun getBookShelfItems(
        @Header("X-Android-Package") packageName: String = "com.example.bookshelf",
        @Header("X-Android-Cert") cert: String = BuildConfig.sha1Certification,
        @Query("q") search: String = "",
        @Query("fields") field: String =
            "totalItems,"+
            "items/volumeInfo" +
                    "(title,authors,publisher,publishedDate, categories, description,pageCount," +
                    "imageLinks/thumbnail , industryIdentifiers(type, identifier))," +
                    "items/saleInfo/retailPrice(amount, currencyCode)," +
                    "items/id",
        @Query("maxResults") maxResult: Int = 38,
        @Query("startIndex") index: Int = 0,
        @Query("orderBy") order: String = "relevance",
        @Query("Country") country: String = "JP",
        @Query("key") key: String = BuildConfig.googleBooksAPI
    ): BookShelfItems
}