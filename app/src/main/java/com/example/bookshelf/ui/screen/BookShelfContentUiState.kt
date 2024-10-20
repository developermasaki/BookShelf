package com.example.bookshelf.ui.screen

import androidx.compose.runtime.Immutable
import com.example.bookshelf.network.Items
import kotlinx.serialization.Serializable

@Immutable
data class BookShelfContentUiState(
    val showOnSearchScreen: Boolean = false,
    val showOnDetailsScreen: Boolean = false,
    val showOnFavoriteScreen: Boolean = true,
    val judgeIsNoInput: Boolean = false,
    val firstSearch: Boolean = false,
    val fullTextSearch: String = "",
    val titleSearch: String = "",
    val authorSearch: String = "",
    val publishingCompany: String = "",
    val bookShelfItems: List<Items?> = emptyList()
)

@Immutable
enum class EditTextField {
    FullTextSearch, TitleSearch, AuthorSearch, PublishingCompany
}

@Immutable
interface Route {
    @Serializable object Search
    @Serializable object Grid
//    data class Details(val items: Items? = null)
}