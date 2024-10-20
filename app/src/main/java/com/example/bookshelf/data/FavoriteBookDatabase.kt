package com.example.bookshelf.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteBook::class], version = 2, exportSchema = true)
abstract class FavoriteBookDatabase: RoomDatabase() {
    abstract fun favoriteBookDao(): FavoriteBookDao

    companion object {
        @Volatile
        private var Instance: FavoriteBookDatabase? = null

        fun getDatabase(context: Context): FavoriteBookDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, FavoriteBookDatabase::class.java, "favoriteBook_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}