package com.example.bookshelf.data.remote

import com.example.bookshelf.model.BookShelfItems
import com.example.bookshelf.model.Items
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface BookShelfRepository{
    suspend fun getSearchBookShelfItems(refresh: Boolean = false, search: String): BookShelfItems
    suspend fun getAddSearchBookShelfItems(search: String, index: Int): BookShelfItems
    suspend fun getItem(id: String): Items
}

class NetworkBookShelfRepository(
    private val bookShelfRemoteDataSource: BookShelfRemoteDataSource
): BookShelfRepository {
    private val latestBookShelfMutex = Mutex()

    private var latestBookShelfItems: BookShelfItems = BookShelfItems(0, emptyList())

    override suspend fun getSearchBookShelfItems(refresh: Boolean, search: String): BookShelfItems {
        if(refresh || latestBookShelfItems == BookShelfItems(0, emptyList())){
            val networkResult = bookShelfRemoteDataSource.fetchBookShelfItems(search = search, index = 0)

            latestBookShelfMutex.withLock {
                latestBookShelfItems = networkResult
            }
        }

        return latestBookShelfMutex.withLock { this.latestBookShelfItems }
    }

    override suspend fun getAddSearchBookShelfItems(search: String, index: Int): BookShelfItems {
        val networkResult = bookShelfRemoteDataSource.fetchBookShelfItems(search = search, index = index)

        latestBookShelfMutex.withLock {
            latestBookShelfItems = BookShelfItems(
                totalItems = latestBookShelfItems.totalItems,
                items = latestBookShelfItems.items.plus(networkResult.items)
            )
        }

        return latestBookShelfMutex.withLock { this.latestBookShelfItems }
    }

    override suspend fun getItem(id: String): Items {
        return latestBookShelfItems.items.find { it?.id == id } ?: Items()
    }
}