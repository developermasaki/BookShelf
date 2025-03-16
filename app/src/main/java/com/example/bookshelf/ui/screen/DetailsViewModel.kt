package com.example.bookshelf.ui.screen

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.FavoriteBookRepository
import com.example.bookshelf.data.remote.BookShelfRepository
import com.example.bookshelf.model.Items
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DetailsViewModel (
    savedStateHandle: SavedStateHandle,
    private val booksRepository: BookShelfRepository,
    favoriteBookRepository: FavoriteBookRepository,
):ViewModel() {
    private val itemId: String = savedStateHandle[DetailsDestination.ITEM_ID_ARG] ?: ""

    val uiState: StateFlow<DetailUiState> =
        favoriteBookRepository.getFavoriteBook(itemId)
            .map { favoriteBook ->
                val returnItem: Items
                var isFavorite = false
                if(favoriteBook == null) {
                    returnItem = booksRepository.getItem(itemId)
                    Log.d("DetailsViewModel", "returnItem: $returnItem")
                } else {
                    returnItem = favoriteBook.toItems()
                    Log.d("DetailsViewModel", "returnItem: $returnItem")
                    isFavorite = true
                }
                DetailUiState(
                    item = returnItem,
                    isFavorite = isFavorite
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailUiState()
            )
}

data class DetailUiState(
    val item: Items = Items(),
    val isFavorite: Boolean = false
)