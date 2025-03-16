package com.example.bookshelf.ui.screen

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.model.FavoriteBook
import com.example.bookshelf.data.FavoriteBookRepository
import com.example.bookshelf.model.ImageLinks
import com.example.bookshelf.model.IndustryIdentifiers
import com.example.bookshelf.model.Items
import com.example.bookshelf.model.RetailPrice
import com.example.bookshelf.model.SaleInfo
import com.example.bookshelf.model.VolumeInfo
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Currency

class EditViewModel(
    savedStateHandle: SavedStateHandle,
    private val favoriteBookRepository: FavoriteBookRepository
):ViewModel() {
    var editUiState by mutableStateOf(EditUiState())
        private set

    private val itemId: String = savedStateHandle[EditDestination.ITEM_ID_ARG] ?: ""

    init {
        viewModelScope.launch {
            Log.d("EditViewModel", itemId)
            editUiState = favoriteBookRepository.getFavoriteBook(itemId)
                .filterNotNull()
                .first()
                .toEditUiState()
            Log.d("EditViewModel", editUiState.toString())
        }
    }

    suspend fun editFavoriteBook(id: String) {
        favoriteBookRepository.updateFavoriteBook(exchangeEditFiledToItems(id).toFavoriteBook())
    }

    fun updateEditUiState(uiState: EditUiState) {
        editUiState = uiState
    }

    private fun exchangeEditFiledToItems(id: String): Items {
        return Items(
            id = id,
            volumeInfo = VolumeInfo(
                title = editUiState.title,
                authors = listOf(editUiState.author),
                publisher = editUiState.publisher,
                publishedDate = editUiState.publishedDate,
                description = editUiState.description,
                industryIdentifiers = listOf(
                    IndustryIdentifiers(
                        type = editUiState.isbnType,
                        identifier = editUiState.isbnNumber
                    )
                ),
                pageCount = editUiState.page.toIntOrNull(),
                categories = listOf(editUiState.category),
                imageLinks = ImageLinks(
                    thumbnail = editUiState.imageLinks
                )
            ),
            saleInfo = SaleInfo(
                retailPrice = RetailPrice(
                    amount = editUiState.price.toDoubleOrNull(),
                    currencyCode = "JPY"
                )
            )
        )
    }
}

fun showCurrencyUnit(country: String?): String? {
    Log.d("EditViewModel", "$country")
    return  if(country == "") "" else Currency.getInstance(country).symbol
}

fun FavoriteBook.toEditUiState(): EditUiState = EditUiState(
    title = this.title ?: "",
    author = this.authors ?: "",
    description = this.description ?: "",
    category = this.categories ?: "",
    publishedDate = this.publishedDate ?: "",
    publisher = this.publisher ?: "",
    price = this.amount.toString(),
    page = this.pageCount.toString(),
    isbnType = this.type ?: "",
    isbnNumber = this.identifier ?: "",
    currencyCode = this.currencyCode ?: "",
    imageLinks = this.imageLinks ?: ""
)

data class EditUiState(
    val id: String = "",
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val category: String = "",
    val publishedDate: String = "",
    val publisher: String = "",
    val price: String = "",
    val page: String = "",
    val isbnType: String = "",
    val isbnNumber: String = "",
    val currencyCode: String = "",
    val imageLinks: String = ""
)