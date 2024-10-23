package com.example.bookshelf.ui.screen

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.BookShelfRepository
import com.example.bookshelf.data.UserPreferencesRepository
import com.example.bookshelf.network.Items
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Currency

@Immutable
sealed interface ScreenUiState2{
    data object Success: ScreenUiState2
    data class Error(val errorType: String, val errorDetails: String) : ScreenUiState2
    data object Loading: ScreenUiState2
}

class SearchViewModel(
    private val bookShelfRepository: BookShelfRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {
    var screenUiState2: ScreenUiState2 by mutableStateOf(ScreenUiState2.Success)
        private set
    var searchUiState by mutableStateOf(SearchUiState())
        private set
    private var index by mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            searchUiState = searchUiState.copy(
                fullTextSearch = userPreferencesRepository.searchKeyword.first(),
                titleSearch = userPreferencesRepository.title.first(),
                authorSearch = userPreferencesRepository.author.first(),
                publishingCompany = userPreferencesRepository.publisher.first()
            )
            judgeIsNoInput()
        }
    }

    fun pickUpItemBookShelf(key: String?): Items {
        val returnItems = searchUiState.bookShelfItems.filter {
            it?.id == key
        }
        return if(returnItems.isEmpty()) Items() else returnItems.first()!!
    }

    fun updateEditTextField(key: EditTextField, value: String) {
        viewModelScope.launch{
            when(key) {
                EditTextField.FullTextSearch -> {
                    searchUiState = searchUiState.copy(fullTextSearch = value)
                    userPreferencesRepository.saveSearchKeyword(value)
                }
                EditTextField.TitleSearch -> {
                    searchUiState = searchUiState.copy(titleSearch = value)
                    userPreferencesRepository.saveTitle(value)
                }
                EditTextField.AuthorSearch -> {
                    searchUiState = searchUiState.copy(authorSearch = value)
                    userPreferencesRepository.saveAuthor(value)
                }
                EditTextField.PublishingCompany -> {
                    searchUiState = searchUiState.copy(publishingCompany = value)
                    userPreferencesRepository.savePublishingCompany(value)
                }
            }
        }
        Log.d("EditText",value)
        judgeIsNoInput()
    }

    fun authorsListUp(authorsList: List<String?>?): String? {
        var authorsLine: String? = null
        if (authorsList?.size != 0) {
            var count = 0
            authorsList?.forEach { author ->
                if(count == 0) {
                    authorsLine = author
                    count++
                } else{
                    authorsLine += "$author "
                }
            }
        }
        return authorsLine
    }

    fun showPrice(price: Double?, country: String?): String? {
        var showedPrice: String? = null
        if(price != null && country != null) {
            val priceMark = Currency.getInstance(country).symbol
            showedPrice = "$priceMark $price"
        }
        return  showedPrice
    }

    //TODO privateで処理できないか考える
    fun bestImage(items: Items?): String? {
        val imageLinks = items?.volumeInfo?.imageLinks
        return if(imageLinks?.extraLarge != null) {
            Log.d("highImage", "highImage")
            imageLinks.extraLarge
        } else if (imageLinks?.large != null) {
            imageLinks.large
        } else if (imageLinks?.medium != null) {
            imageLinks.medium
        } else if (imageLinks?.small != null) {
            imageLinks.small
        } else {
            imageLinks?.thumbnail
        }
    }

    fun isFirstSearch(firstSearch: Boolean){
        searchUiState = searchUiState.copy(firstSearch = firstSearch)
    }

    fun getBookShelfItems(isFirstSearch: Boolean = false) {
        if (isFirstSearch) {
            index = 0
            screenUiState2 = ScreenUiState2.Loading
            searchUiState = searchUiState.copy(firstSearch = true)
        } else {
            index += 38
            searchUiState = searchUiState.copy(firstSearch = false)
        }
        viewModelScope.launch {
            var searchText = ""
            if(searchUiState.fullTextSearch != "") {
                searchText+= searchUiState.fullTextSearch
            }
            if(searchUiState.titleSearch != "") {
                searchText += "+intitle:${searchUiState.titleSearch}"
            }
            if(searchUiState.authorSearch != "") {
                searchText += "+inauthor:${searchUiState.authorSearch}"
            }
            if (searchUiState.publishingCompany != "") {
                searchText += "+inpublisher:${searchUiState.publishingCompany}"
            }
            Log.d("searchText", searchText)

            try {
                val bookShelfItems: List<Items?> = bookShelfRepository.getBookShelfItems(
                    search = searchText,
                    index = index
                ).items ?: emptyList()
                Log.d("index","$index")

                if(screenUiState2 is ScreenUiState2.Success) {
                    searchUiState = searchUiState.copy(
                        bookShelfItems = searchUiState.bookShelfItems + bookShelfItems
                    )
                } else {
                    searchUiState = searchUiState.copy(
                        bookShelfItems = bookShelfItems
                    )
                    screenUiState2 = ScreenUiState2.Success
                }
            }catch (e: IOException) {
                screenUiState2 = ScreenUiState2.Error("IOException", "$e")
            }catch (e: retrofit2.HttpException){
                screenUiState2 = ScreenUiState2.Error("HttpException", "$e")
            }
            Log.d("ScreenUiState", "$screenUiState2")
        }
    }

    private fun judgeIsNoInput() {
        searchUiState = searchUiState.copy(
            judgeIsNoInput = !(searchUiState.titleSearch == "" &&
                    searchUiState.authorSearch == "" &&
                    searchUiState.publishingCompany == "" &&
                    searchUiState.fullTextSearch == "")
        )
        Log.d("bookShelfContentUiState","$searchUiState")
    }
}

@Immutable
data class SearchUiState(
    val fullTextSearch: String = "",
    val titleSearch: String = "",
    val authorSearch: String = "",
    val publishingCompany: String = "",
    val judgeIsNoInput: Boolean = true,
    val firstSearch: Boolean = false,
    val bookShelfItems: List<Items?> = emptyList()
)