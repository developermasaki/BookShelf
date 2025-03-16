package com.example.bookshelf.data

import com.example.bookshelf.model.FavoriteBook
import kotlinx.coroutines.flow.Flow

interface FavoriteBookRepository {
    fun getAllFavoriteBooks(): Flow<List<FavoriteBook>>

    fun getFavoriteBook(id: String): Flow<FavoriteBook?>

    suspend fun insertFavoriteBook(favoriteBook: FavoriteBook)

    suspend fun deleteFavoriteBook(favoriteBook: FavoriteBook)

    suspend fun updateFavoriteBook(favoriteBook: FavoriteBook)
}