package com.example.bookshelf.data.local

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.bookshelf.R

data class DialogListModel(
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
)

object LocalDataProvider {
    val list: List<DialogListModel> = listOf(
        DialogListModel(
            name = R.string.fullTextSearch,
            icon = R.drawable.rounded_quick_reference_all_24
        ),
        DialogListModel(
            name = R.string.title,
            icon = R.drawable.rounded_book_3_24
        ),
        DialogListModel(
            name = R.string.author,
            icon = R.drawable.rounded_person_search_24
        ),
        DialogListModel(
            name = R.string.publisher,
            icon = R.drawable.rounded_apartment_24
        )
    )
}