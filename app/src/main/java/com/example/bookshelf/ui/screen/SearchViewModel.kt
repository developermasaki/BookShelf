package com.example.bookshelf.ui.screen

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookshelf.data.remote.BookShelfRepository
import com.example.bookshelf.data.UserPreferencesRepository
import com.example.bookshelf.model.Items
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Currency
import java.util.UUID

@Immutable
sealed interface SearchScreenUiState{
    data object Success: SearchScreenUiState
    data class Error(val errorType: String, val errorDetails: String) : SearchScreenUiState
    data class Loading(val noSearched: Boolean): SearchScreenUiState
}

class SearchViewModel(
    private val bookShelfRepository: BookShelfRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
): ViewModel() {
    var searchUiState by mutableStateOf(SearchUiState())
        private set
    private var _showedBookShelfItems = MutableStateFlow<List<Pair<String, Items?>>>(listOf())
    val showedBookShelfItems: StateFlow<List<Pair<String, Items?>>> = _showedBookShelfItems.asStateFlow()
    private var index by mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            searchUiState = searchUiState.copy(
                fullTextSearch = userPreferencesRepository.searchKeyword.first(),
                titleSearch = userPreferencesRepository.title.first(),
                authorSearch = userPreferencesRepository.author.first(),
                publishingCompany = userPreferencesRepository.publisher.first(),
                screenUiState = SearchScreenUiState.Loading(noSearched = true)
            )
            judgeIsNoInput()
        }
    }

    fun updateEditTextField(key: EditFieldInSearchScreen, value: String) {
        viewModelScope.launch{
            when(key) {
                EditFieldInSearchScreen.FullTextSearch -> {
                    searchUiState = searchUiState.copy(fullTextSearch = value)
                    withContext(Dispatchers.IO){ userPreferencesRepository.saveSearchKeyword(value) }
                }
                EditFieldInSearchScreen.TitleSearch -> {
                    searchUiState = searchUiState.copy(titleSearch = value)
                    withContext(Dispatchers.IO) { userPreferencesRepository.saveTitle(value) }
                }
                EditFieldInSearchScreen.AuthorSearch -> {
                    searchUiState = searchUiState.copy(authorSearch = value)
                    withContext(Dispatchers.IO) { userPreferencesRepository.saveAuthor(value) }
                }
                EditFieldInSearchScreen.PublishingCompany -> {
                    searchUiState = searchUiState.copy(publishingCompany = value)
                    withContext(Dispatchers.IO) { userPreferencesRepository.savePublishingCompany(value) }
                }
            }
        }
        judgeIsNoInput()
    }

    fun isFirstSearch(firstSearch: Boolean){
        searchUiState = searchUiState.copy(firstSearch = firstSearch)
    }

    suspend fun getBookShelfItems(isFirstSearch: Boolean = false) {
        if (isFirstSearch) {
            index = 0
            _showedBookShelfItems.update {
                emptyList()
            }
            searchUiState = searchUiState.copy(screenUiState = SearchScreenUiState.Loading(noSearched = false))
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

            try {
                val bookShelfItems: List<Items?> = if(isFirstSearch) {
                    bookShelfRepository.getSearchBookShelfItems(
                        refresh = isFirstSearch,
                        search = searchText
                    ).items
                } else {
                    bookShelfRepository.getAddSearchBookShelfItems(
                        search = searchText,
                        index = index
                    ).items
                }
                Log.d("SearchViewModel","bookShelfItems: $bookShelfItems")
                val bookShelfItemsList = mutableListOf<Pair<String, Items?>>()
                /* 時々、OriginalLazyVerticalGridをスクロールしていると、なぜか同じBookCardの内容が二つ作られていることがあった。
                そこでGoogleBooksAPIから送信されるデータに重複があるのではないかと考え、コードを作った。
                 */
                for(bookShelfItem in bookShelfItems) {
                    if (
                        !bookShelfItemsList.any { it.second?.id == bookShelfItem?.id } &&
                        showedBookShelfItems.value.none { it.second?.id == bookShelfItem?.id }
                        ){
                        val bookShelfItemId = UUID.randomUUID().toString()
                        bookShelfItemsList.add(Pair(bookShelfItemId, bookShelfItem))
                    } else{
                        Log.d("bookShelfItems","含まれていた")
                    }
                }
                if(searchUiState.screenUiState is SearchScreenUiState.Success) {
                    _showedBookShelfItems.update {
                        it + bookShelfItemsList
                    }
                } else {
                    _showedBookShelfItems.update {
                        bookShelfItemsList
                    }
                    searchUiState = searchUiState.copy(screenUiState = SearchScreenUiState.Success)
                }
            }catch (e: IOException) {
                searchUiState = searchUiState.copy(screenUiState = SearchScreenUiState.Error("IOException", "$e"))
            }catch (e: retrofit2.HttpException){
                searchUiState = searchUiState.copy(screenUiState = SearchScreenUiState.Error("HttpException", "$e"))
            }
        }
    }

    private fun judgeIsNoInput() {
        searchUiState = searchUiState.copy(
            judgeIsNoInput = !(searchUiState.titleSearch == "" &&
                    searchUiState.authorSearch == "" &&
                    searchUiState.publishingCompany == "" &&
                    searchUiState.fullTextSearch == "")
        )
    }
}

fun showPrice(price: Double?, country: String?): String? {
    var showedPrice: String? = null
    if(price != null && country != null) {
        val priceMark = Currency.getInstance(country).symbol
        showedPrice = "$priceMark $price"
    }
    return  showedPrice
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

data class SearchUiState(
    val fullTextSearch: String = "",
    val titleSearch: String = "",
    val authorSearch: String = "",
    val publishingCompany: String = "",
    val judgeIsNoInput: Boolean = true,
    val firstSearch: Boolean = false,
    val screenUiState: SearchScreenUiState = SearchScreenUiState.Loading(noSearched = true)
)

@Immutable
enum class EditFieldInSearchScreen {
    FullTextSearch, TitleSearch, AuthorSearch, PublishingCompany
}