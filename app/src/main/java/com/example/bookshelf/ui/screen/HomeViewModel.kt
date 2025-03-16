package com.example.bookshelf.ui.screen

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Immutable
sealed interface FavoriteScreenUiState{
    data object Success: FavoriteScreenUiState
    data class Error(val errorType: String, val errorDetails: String) : FavoriteScreenUiState
    data object Loading: FavoriteScreenUiState
}

// favoriteBookのデータ保存や検索を担当
class HomeViewModel(
    private val favoriteBookRepository: FavoriteBookRepository,
) : ViewModel(){
    var homeUiState: HomeUiState by mutableStateOf(HomeUiState())
        private set

    val favoriteBookList: StateFlow<List<Pair<String, Items?>>> =
        favoriteBookRepository.getAllFavoriteBooks()
            .map {
                homeUiState = homeUiState.copy(screenUiState = FavoriteScreenUiState.Success)
                val bookShelfItemsList = mutableListOf<Pair<String, Items?>>()
                val favoriteBookItems = it.map {favoriteBook ->
                    favoriteBook.toItems()
                }
                for(favoriteBook in favoriteBookItems){
                    val bookShelfItemId = UUID.randomUUID().toString()
                    bookShelfItemsList.add(Pair(bookShelfItemId, favoriteBook))
                }
                bookShelfItemsList
            }
            .catch { e->
                homeUiState = homeUiState.copy(screenUiState = FavoriteScreenUiState.Error("favoriteBookList", e.message.toString()))
            }
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )


    fun isShowFavoriteScreen(favorite: Boolean) {
        homeUiState = homeUiState.copy(showOnFavoriteScreen = favorite)
    }

    fun pickUpItemBookShelf(key: String?): Items {
        return favoriteBookList.value.firstOrNull {
            it.second?.id == key
        }?.second ?: Items()
    }

    suspend fun toggleFavoriteBook(items: Items) {
        viewModelScope.launch {
            withContext(Dispatchers.IO)  {
                val list: List<FavoriteBook> = getFavoriteBooks().first()
                if(list.any{it.title == items.volumeInfo?.title}) {
                    deleteFavoriteBook(items)
                } else {
                    saveFavoriteBook(items)
                }
            }
        }
    }

    private suspend fun saveFavoriteBook(items: Items) {
        favoriteBookRepository.insertFavoriteBook(items.toFavoriteBook())
    }

    private suspend fun deleteFavoriteBook(items: Items) {
        favoriteBookRepository.deleteFavoriteBook(items.toFavoriteBook())
    }

    private fun getFavoriteBooks(): Flow<List<FavoriteBook>> = favoriteBookRepository.getAllFavoriteBooks()
}

@Immutable
data class HomeUiState(
    val showOnFavoriteScreen: Boolean = true,
    val toDetailsScreen: Boolean = false,
    val screenUiState: FavoriteScreenUiState = FavoriteScreenUiState.Loading
)

fun Items.toFavoriteBook(): FavoriteBook = FavoriteBook(
    id = this.id ?: UUID.randomUUID().toString(),
    title = this.volumeInfo?.title,
    authors = authorsListUp(this.volumeInfo?.authors),
    publisher = this.volumeInfo?.publisher,
    publishedDate = this.volumeInfo?.publishedDate,
    description = this.volumeInfo?.description,
    type = this.volumeInfo?.industryIdentifiers?.find {it?.type == "ISBN_13"}?.type
        ?: this.volumeInfo?.industryIdentifiers?.find { it?.type == "ISBN_10" }?.type,
    identifier = this.volumeInfo?.industryIdentifiers?.find {it?.type == "ISBN_13"}?.identifier
        ?: this.volumeInfo?.industryIdentifiers?.find { it?.type == "ISBN_10" }?.identifier,
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