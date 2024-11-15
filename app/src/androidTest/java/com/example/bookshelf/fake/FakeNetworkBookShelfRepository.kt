package com.example.bookshelf.fake

import com.example.bookshelf.data.BookShelfRepository
import com.example.bookshelf.network.BookShelfItems

class FakeNetworkBookShelfRepository: BookShelfRepository {
    override suspend fun getBookShelfItems(search: String, index: Int): BookShelfItems{
        return FakeDataSource.BookShelfItems
    }
}