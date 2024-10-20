package com.example.bookshelf.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoriteBook")
data class FavoriteBook(
    @PrimaryKey
    val id: String,
    val title: String?,
    val authors: String?,
    val publisher: String?,
    val publishedDate: String?,
    val description: String?,
    val type: String?,
    val identifier: String?,
    val pageCount: Int?,
    val categories: String?,
    val imageLinks: String?,
    val amount: Double?,
    val currencyCode: String?
)