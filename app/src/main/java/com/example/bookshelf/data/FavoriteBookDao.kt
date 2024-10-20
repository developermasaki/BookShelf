package com.example.bookshelf.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favoriteBook: FavoriteBook)

    @Delete
    suspend fun delete(favoriteBook: FavoriteBook)

    @Query("SELECT * from favoriteBook ORDER BY title")
    fun getAllFavoriteBooks(): Flow<List<FavoriteBook>>
}