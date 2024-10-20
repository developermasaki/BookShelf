package com.example.bookshelf.data

import com.example.bookshelf.network.BookShelfApiService
import com.example.bookshelf.network.BookShelfItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BookShelfRepository{
    suspend fun getBookShelfItems(search: String, index: Int): BookShelfItems
}

class NetworkBookShelfRepository(
    private val bookShelfApiService: BookShelfApiService,
): BookShelfRepository {
    override suspend fun getBookShelfItems(search: String, index: Int): BookShelfItems = withContext(Dispatchers.Default) {
        bookShelfApiService.getBookShelfItems(search = search, index = index)
    }
}