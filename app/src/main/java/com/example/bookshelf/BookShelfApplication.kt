package com.example.bookshelf

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.bookshelf.data.AppContainer
import com.example.bookshelf.data.BookShelfAppContainer
import com.example.bookshelf.data.FavoriteAppContainer
import com.example.bookshelf.data.FavoriteBookAppContainer
import com.example.bookshelf.data.UserPreferencesRepository

private const val SEARCH_PREFERENCE_NAME = "search_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SEARCH_PREFERENCE_NAME
)

class BookShelfApplication: Application() {
    lateinit var container: AppContainer
    lateinit var favoriteAppContainer: FavoriteAppContainer
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        container = BookShelfAppContainer()
        favoriteAppContainer = FavoriteBookAppContainer(this)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}