package com.example.bookshelf.ui.screen

import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bookshelf.BookShelfApplication
import com.example.bookshelf.data.BookShelfRepository
import com.example.bookshelf.data.FavoriteBook
import com.example.bookshelf.data.FavoriteBookRepository
import com.example.bookshelf.data.UserPreferencesRepository
import com.example.bookshelf.network.ImageLinks
import com.example.bookshelf.network.IndustryIdentifiers
import com.example.bookshelf.network.Items
import com.example.bookshelf.network.VolumeInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.example.bookshelf.network.SaleInfo
import com.example.bookshelf.network.RetailPrice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException
import java.util.Currency

@Immutable
sealed interface ScreenUiState{
    data object Success: ScreenUiState
    data class Error(val errorType: String, val errorDetails: String) : ScreenUiState
    data object Loading: ScreenUiState
}

class BookShelfViewModel(
    private val bookShelfRepository: BookShelfRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val favoriteBookRepository: FavoriteBookRepository,
): ViewModel() {
    var screenUiState: ScreenUiState by mutableStateOf(ScreenUiState.Success)
        private set
    var bookShelfContentUiState by mutableStateOf(BookShelfContentUiState())
        private set
    val favoriteBookList: StateFlow<List<Items?>> =
        favoriteBookRepository.getAllFavoriteBooks()
            .map {
                val bookShelfItems:List<Items> = it.map { favoriteBook ->
                    favoriteBook.toItems()
                }
                bookShelfItems
            }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )
    private var index by mutableIntStateOf(0)

    init {
        viewModelScope.launch {
            bookShelfContentUiState = bookShelfContentUiState.copy(
                fullTextSearch = userPreferencesRepository.searchKeyword.first(),
                titleSearch = userPreferencesRepository.title.first(),
                authorSearch = userPreferencesRepository.author.first(),
                publishingCompany = userPreferencesRepository.publisher.first()
            )
            judgeIsNoInput()
        }
    }

    fun isShowSearchScreen() {
        bookShelfContentUiState = bookShelfContentUiState.copy(showOnSearchScreen = !bookShelfContentUiState.showOnSearchScreen)
    }

    fun isShowDetailsScreen() {
        bookShelfContentUiState = bookShelfContentUiState.copy(showOnDetailsScreen = !bookShelfContentUiState.showOnDetailsScreen)
    }

    fun isShowFavoriteScreen() {
        bookShelfContentUiState = bookShelfContentUiState.copy(showOnFavoriteScreen = !bookShelfContentUiState.showOnFavoriteScreen)
    }

    fun updateEditTextField(key: EditTextField, value: String) {
         viewModelScope.launch{
            when(key) {
                EditTextField.FullTextSearch -> {
                    bookShelfContentUiState = bookShelfContentUiState.copy(fullTextSearch = value)
                    userPreferencesRepository.saveSearchKeyword(value)
                }
                EditTextField.TitleSearch -> {
                    bookShelfContentUiState = bookShelfContentUiState.copy(titleSearch = value)
                    userPreferencesRepository.saveTitle(value)
                }
                EditTextField.AuthorSearch -> {
                    bookShelfContentUiState = bookShelfContentUiState.copy(authorSearch = value)
                    userPreferencesRepository.saveAuthor(value)
                }
                EditTextField.PublishingCompany -> {
                    bookShelfContentUiState = bookShelfContentUiState.copy(publishingCompany = value)
                    userPreferencesRepository.savePublishingCompany(value)
                }
            }
        }
        Log.d("EditText",value)
        judgeIsNoInput()
    }

    fun pickUpItem(key: String?): Items {
        if(bookShelfContentUiState.showOnFavoriteScreen) {
            Log.d("pickup", "${favoriteBookList.value}")
            return favoriteBookList.value.firstOrNull {
                it?.id == key
            } ?: Items()
        } else {
            val returnItems = bookShelfContentUiState.bookShelfItems.filter {
                it?.id == key
            }
            return if(returnItems.isEmpty()) Items() else returnItems.first()!!
        }
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

//    fun checkFavoriteBook(items: Items?): Boolean {
//        var favorite = false
//        viewModelScope.launch {
//            val list: List<FavoriteBook> = getFavoriteBooks().first()
//            list.forEach {
//                if(it.bookId == items?.id) {
//                    favorite = true
//                }
//            }
//        }
//    }

    suspend fun toggleFavoriteBook(items: Items) {
        val list: List<FavoriteBook> = getFavoriteBooks().first()
        Log.d("toggle1", "$list")
        if(list.any{it.title == items.volumeInfo?.title}) {
            deleteFavoriteBook(items)
        } else {
            saveFavoriteBook(items)
        }
    }

    private suspend fun saveFavoriteBook(items: Items) {
        Log.d("toggle4", "${items.toFavoriteBook()}")
        favoriteBookRepository.insertFavoriteBook(items.toFavoriteBook())
    }

    private suspend fun deleteFavoriteBook(items: Items) {
        Log.d("toggle5", "${items.toFavoriteBook()}")
        favoriteBookRepository.deleteFavoriteBook(items.toFavoriteBook())
    }

    private fun getFavoriteBooks(): Flow<List<FavoriteBook>> = favoriteBookRepository.getAllFavoriteBooks()

    private fun judgeIsNoInput() {
        bookShelfContentUiState = bookShelfContentUiState.copy(
                judgeIsNoInput = !(bookShelfContentUiState.titleSearch == "" &&
                        bookShelfContentUiState.authorSearch == "" &&
                        bookShelfContentUiState.publishingCompany == "" &&
                        bookShelfContentUiState.fullTextSearch == "")
        )
        Log.d("bookShelfContentUiState","$bookShelfContentUiState")
    }

    fun getBookShelfItems(isFirstSearch: Boolean = false) {
        bookShelfContentUiState = bookShelfContentUiState.copy(showOnFavoriteScreen = false)
        if (isFirstSearch) {
            index = 0
            screenUiState = ScreenUiState.Loading
            bookShelfContentUiState = bookShelfContentUiState.copy(firstSearch = true)
        } else {
            index += 38
            bookShelfContentUiState = bookShelfContentUiState.copy(firstSearch = false)
        }
        bookShelfContentUiState = bookShelfContentUiState.copy(showOnSearchScreen = false)
        viewModelScope.launch {
            var searchText = ""
            if(bookShelfContentUiState.fullTextSearch != "") {
                searchText+= bookShelfContentUiState.fullTextSearch
            }
            if(bookShelfContentUiState.titleSearch != "") {
                searchText += "+intitle:${bookShelfContentUiState.titleSearch}"
            }
            if(bookShelfContentUiState.authorSearch != "") {
                searchText += "+inauthor:${bookShelfContentUiState.authorSearch}"
            }
            if (bookShelfContentUiState.publishingCompany != "") {
                searchText += "+inpublisher:${bookShelfContentUiState.publishingCompany}"
            }
            Log.d("searchText", searchText)

            try {
                val bookShelfItems: List<Items?> = bookShelfRepository.getBookShelfItems(
                    search = searchText,
                    index = index
                ).items ?: emptyList()
//                val keyAndBookShelfItems: MutableMap<String?, Items?> = mutableMapOf()
//                bookShelfItems.items?.forEach { bookShelfItem ->
//                    keyAndBookShelfItems[key()] = bookShelfItem
//                }
                Log.d("index","$index")
                if(screenUiState is ScreenUiState.Success) {
                    bookShelfContentUiState = bookShelfContentUiState.copy(
                        bookShelfItems = bookShelfContentUiState.bookShelfItems + bookShelfItems
                    )
                } else {
                    bookShelfContentUiState = bookShelfContentUiState.copy(
                        bookShelfItems = bookShelfItems
                    )
                    screenUiState = ScreenUiState.Success
                }
            }catch (e: IOException) {
                screenUiState = ScreenUiState.Error("IOException", "$e")
            }catch (e: retrofit2.HttpException){
                screenUiState = ScreenUiState.Error("HttpException", "$e")
            }
            Log.d("ScreenUiState", "$screenUiState")
        }
    }

    companion object{
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BookShelfApplication)
                val bookShelfRepository = application.container.bookShelfRepository
                val userPreferencesRepository = application .userPreferencesRepository
                val favoriteBookRepository = application.favoriteAppContainer.favoriteBookRepository
                BookShelfViewModel(
                    bookShelfRepository = bookShelfRepository,
                    userPreferencesRepository = userPreferencesRepository,
                    favoriteBookRepository = favoriteBookRepository
                )
            }
        }
    }
}

fun Items.toFavoriteBook(): FavoriteBook = FavoriteBook(
    id = this.id!!,
    title = this.volumeInfo?.title,
    authors = this.volumeInfo?.authors?.get(0),
    publisher = this.volumeInfo?.publisher,
    publishedDate = this.volumeInfo?.publishedDate,
    description = this.volumeInfo?.description,
    type = this.saleInfo?.retailPrice?.currencyCode,
    identifier = this.volumeInfo?.industryIdentifiers?.get(0)?.identifier,
    pageCount = this.volumeInfo?.pageCount,
    categories = this.volumeInfo?.categories?.get(0),
    imageLinks = this.volumeInfo?.imageLinks?.thumbnail,
    amount = this.saleInfo?.retailPrice?.amount,
    currencyCode = this.saleInfo?.retailPrice?.currencyCode
)

fun FavoriteBook.toItems(): Items = Items(
    id = id,
    volumeInfo = VolumeInfo(
        title = title,
        authors = listOf(authors),
        publisher = publisher,
        publishedDate = publishedDate,
        description = description,
        industryIdentifiers = listOf(
            IndustryIdentifiers(
                type = type,
                identifier = identifier
            ),
            ),
        pageCount = pageCount,
        categories = listOf(categories),
        imageLinks = ImageLinks(
            thumbnail = imageLinks
        ),
        ),
    saleInfo = SaleInfo(
        retailPrice = RetailPrice(
            amount = amount,
            currencyCode = currencyCode
        )
    )
)