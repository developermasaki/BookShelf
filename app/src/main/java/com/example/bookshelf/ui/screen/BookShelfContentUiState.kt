package com.example.bookshelf.ui.screen

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
enum class EditTextField {
    FullTextSearch, TitleSearch, AuthorSearch, PublishingCompany
}

@Immutable
interface Route {
    @Serializable object Search
    @Serializable object Home
    @Serializable object Parent
}