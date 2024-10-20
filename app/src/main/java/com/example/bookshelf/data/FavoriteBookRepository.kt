package com.example.bookshelf.data

import kotlinx.coroutines.flow.Flow

interface FavoriteBookRepository {
    fun getAllFavoriteBooks(): Flow<List<FavoriteBook>>

    suspend fun insertFavoriteBook(favoriteBook: FavoriteBook)

    suspend fun deleteFavoriteBook(favoriteBook: FavoriteBook)
}