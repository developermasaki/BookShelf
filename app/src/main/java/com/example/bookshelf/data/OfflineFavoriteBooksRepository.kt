package com.example.bookshelf.data

import kotlinx.coroutines.flow.Flow

class OfflineFavoriteBooksRepository(private val favoriteBookDao: FavoriteBookDao): FavoriteBookRepository {
    override fun getAllFavoriteBooks(): Flow<List<FavoriteBook>> = favoriteBookDao.getAllFavoriteBooks()

    override suspend fun deleteFavoriteBook(favoriteBook: FavoriteBook) = favoriteBookDao.delete(favoriteBook)

    override suspend fun insertFavoriteBook(favoriteBook: FavoriteBook) = favoriteBookDao.insert(favoriteBook)
}