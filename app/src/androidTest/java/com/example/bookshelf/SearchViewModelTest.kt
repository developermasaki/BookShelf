package com.example.bookshelf

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.example.bookshelf.data.UserPreferencesRepository
import com.example.bookshelf.fake.FakeDataSource
import com.example.bookshelf.fake.FakeNetworkBookShelfRepository
import com.example.bookshelf.rules.TestDispatcherRule
import com.example.bookshelf.ui.screen.SearchViewModel
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    @get:Rule
    val testDispatcher = TestDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun searchViewModel_getBookShelfItems_verifyBookShelfItems() = runTest {

        val testCoroutineScope = TestScope(UnconfinedTestDispatcher() + Job())

        val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = { context.preferencesDataStoreFile("TEST_DATASTORE_NAME") }
        )

        val searchViewModel = SearchViewModel(
            bookShelfRepository = FakeNetworkBookShelfRepository(),
            userPreferencesRepository = UserPreferencesRepository(dataStore = dataStore)
        )

        searchViewModel.getBookShelfItems(true)
        advanceUntilIdle()
        assertEquals(
            FakeDataSource.BookShelfItems.items,
            searchViewModel.showedBookShelfItems
        )
    }
}