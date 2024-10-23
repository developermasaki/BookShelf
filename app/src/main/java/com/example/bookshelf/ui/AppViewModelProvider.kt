package com.example.bookshelf.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookShelfApplication
import com.example.bookshelf.ui.screen.HomeViewModel
import com.example.bookshelf.ui.screen.SearchViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            HomeViewModel(
                favoriteBookRepository = application().favoriteAppContainer.favoriteBookRepository
            )
        }
        initializer {
            SearchViewModel(
                bookShelfRepository = application().container.bookShelfRepository,
                userPreferencesRepository = application().userPreferencesRepository
            )
        }
    }
}

fun CreationExtras.application(): BookShelfApplication = (this[APPLICATION_KEY] as BookShelfApplication)