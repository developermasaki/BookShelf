package com.example.bookshelf.data.remote

import com.example.bookshelf.model.BookShelfItems
import com.example.bookshelf.network.BookShelfApiService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class BookShelfRemoteDataSource (
    private val bookShelfApiService: BookShelfApiService,
    private val ioDispatcher: CoroutineDispatcher
) {
    suspend fun fetchBookShelfItems(search: String, index: Int): BookShelfItems =
        withContext(ioDispatcher){
            bookShelfApiService.getBookShelfItems(search = search, index = index)
        }
}