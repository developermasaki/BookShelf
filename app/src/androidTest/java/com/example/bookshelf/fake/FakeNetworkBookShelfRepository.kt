package com.example.bookshelf.fake

import com.example.bookshelf.data.remote.BookShelfRepository
import com.example.bookshelf.model.BookShelfItems

class FakeNetworkBookShelfRepository: BookShelfRepository {
    override suspend fun getSearchBookShelfItems(search: String): BookShelfItems {
        return FakeDataSource.BookShelfItems
    }
}