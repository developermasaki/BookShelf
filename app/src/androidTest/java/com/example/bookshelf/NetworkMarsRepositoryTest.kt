package com.example.bookshelf

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.bookshelf.data.NetworkBookShelfRepository
import com.example.bookshelf.fake.FakeBookShelfApiService
import com.example.bookshelf.fake.FakeDataSource
import kotlinx.coroutines.test.runTest

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

class NetworkMarsRepositoryTest {
    @Test
    fun networkBookShelfRepository_getBookShelfItems_verifyBookShelfItemsList() =
        runTest {
            val repository = NetworkBookShelfRepository(
                bookShelfApiService = FakeBookShelfApiService()
            )
            assertEquals(FakeDataSource.BookShelfItems, repository.getBookShelfItems(search = "", index = 10))
        }
}