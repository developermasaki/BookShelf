package com.example.bookshelf.data

import android.content.Context

interface FavoriteAppContainer {
    val favoriteBookRepository: FavoriteBookRepository
}

class FavoriteBookAppContainer(private val context: Context): FavoriteAppContainer {
    override val favoriteBookRepository: FavoriteBookRepository by lazy {
        OfflineFavoriteBooksRepository(FavoriteBookDatabase.getDatabase(context).favoriteBookDao())
    }
}