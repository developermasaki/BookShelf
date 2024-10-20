package com.example.bookshelf

import com.example.bookshelf.fake.FakeDataSource
import com.example.bookshelf.fake.FakeNetworkBookShelfRepository
import com.example.bookshelf.rules.TestDispatcherRule
import com.example.bookshelf.ui.screen.BookShelfViewModel
import com.example.bookshelf.ui.screen.ScreenUiState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class BookShelfViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    @Test
    fun bookShelfViewModel_getBookShelfItems_verifyBookShelfUiStateSuccess() =
        runTest {
        }
}