package com.example.bookshelf.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface BookShelfApiService {
    @GET("volumes")
    suspend fun getBookShelfItems(
        @Header("X-Android-Package") packageName: String = "com.example.bookshelf",
        @Header("X-Android-Cert") cert: String = "A055550CB8CBA6B7D5FDE568ACE821801ABAF3BC",
        @Query("q") search: String = "",
        @Query("fields") field: String =
            "totalItems,"+
            "items/volumeInfo" +
                    "(title,authors,publisher,publishedDate, categories, description,pageCount," +
                    "imageLinks/* , industryIdentifiers(type, identifier))," +
                    "items/saleInfo/retailPrice(amount, currencyCode)" +
                    "items/id",
        @Query("maxResults") maxResult: Int = 38,
        @Query("startIndex") index: Int = 0,
        @Query("orderBy") order: String = "relevance",
        @Query("Country") country: String = "JP",
        @Query("key") key: String = "AIzaSyCQbjeuHS76a5zK17-U-cNXgt8-fEHUF7s"
    ): BookShelfItems
}