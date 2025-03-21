package com.example.bookshelf

import com.example.bookshelf.data.remote.NetworkBookShelfRepository
import com.example.bookshelf.fake.FakeBookShelfApiService
import com.example.bookshelf.fake.FakeDataSource
import kotlinx.coroutines.test.runTest

import org.junit.Test

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class NetworkBookShelfRepositoryTest {
    @Test
    fun networkBookShelfRepository_getBookShelfItems_verifyBookShelfItemsList() =
        runTest {
            val repository = NetworkBookShelfRepository(
                bookShelfApiService = FakeBookShelfApiService()
            )
            assertEquals(FakeDataSource.BookShelfItems, repository.getSearchBookShelfItems(
                search = ""
            ))
        }
}