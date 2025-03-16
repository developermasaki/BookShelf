package com.example.bookshelf.data

import com.example.bookshelf.model.FavoriteBook
import kotlinx.coroutines.flow.Flow

class OfflineFavoriteBooksRepository(private val favoriteBookDao: FavoriteBookDao): FavoriteBookRepository {
    override fun getAllFavoriteBooks(): Flow<List<FavoriteBook>> = favoriteBookDao.getAllFavoriteBooks()

    override fun getFavoriteBook(id: String): Flow<FavoriteBook?> = favoriteBookDao.getItem(id)

    override suspend fun deleteFavoriteBook(favoriteBook: FavoriteBook) = favoriteBookDao.delete(favoriteBook)

    override suspend fun insertFavoriteBook(favoriteBook: FavoriteBook) = favoriteBookDao.insert(favoriteBook)

    override suspend fun updateFavoriteBook(favoriteBook: FavoriteBook) = favoriteBookDao.update(favoriteBook)
}