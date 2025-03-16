package com.example.bookshelf.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bookshelf.model.FavoriteBook
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteBookDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favoriteBook: FavoriteBook)

    @Delete
    suspend fun delete(favoriteBook: FavoriteBook)

    @Update
    suspend fun update(favoriteBook: FavoriteBook)

    @Query("SELECT * from favoriteBook WHERE id = :id")
    fun getItem(id: String): Flow<FavoriteBook>

    @Query("SELECT * from favoriteBook ORDER BY title")
    fun getAllFavoriteBooks(): Flow<List<FavoriteBook>>
}