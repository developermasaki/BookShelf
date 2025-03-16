package com.example.bookshelf.fake

import com.example.bookshelf.network.BookShelfApiService
import com.example.bookshelf.model.BookShelfItems

class FakeBookShelfApiService: BookShelfApiService {
    override suspend fun getBookShelfItems(
        packageName: String,
        cert: String,
        search: String,
        field: String,
        maxResult: Int,
        index: Int,
        order: String,
        country: String,
        key: String
    ): BookShelfItems {
        return FakeDataSource.BookShelfItems
    }
}