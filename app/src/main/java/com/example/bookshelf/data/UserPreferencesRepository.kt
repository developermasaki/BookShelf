package com.example.bookshelf.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val SEARCH_KEYWORD = stringPreferencesKey("search_keyword")
        val TITLE_KEYWORD = stringPreferencesKey("title")
        val AUTHOR_KEYWORD = stringPreferencesKey("author")
        val PUBLISHING_KEYWORD = stringPreferencesKey("publishing_company")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun saveSearchKeyword(keyword: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_KEYWORD] = keyword
        }
    }

    suspend fun saveTitle(title: String) {
        dataStore.edit { preferences ->
            preferences[TITLE_KEYWORD] = title
        }
    }

    suspend fun saveAuthor(author: String) {
        dataStore.edit { preferences ->
            preferences[AUTHOR_KEYWORD] = author
        }
    }

    suspend fun savePublishingCompany(company: String) {
        dataStore.edit { preferences ->
            preferences[PUBLISHING_KEYWORD] = company
        }
    }

    val searchKeyword: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[SEARCH_KEYWORD] ?: ""
        }

    val title: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[TITLE_KEYWORD] ?: ""
        }

    val author: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map {preferences ->
            preferences[AUTHOR_KEYWORD] ?: ""
        }

    val publisher: Flow<String> = dataStore.data
        .catch {
            if(it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }
        .map { preferences ->
            preferences[PUBLISHING_KEYWORD] ?: ""
        }
}