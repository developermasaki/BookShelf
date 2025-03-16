package com.example.bookshelf.fake

import com.example.bookshelf.model.BookShelfItems
import com.example.bookshelf.model.IndustryIdentifiers
import com.example.bookshelf.model.Items
import com.example.bookshelf.model.RetailPrice
import com.example.bookshelf.model.SaleInfo
import com.example.bookshelf.model.VolumeInfo

object FakeDataSource {
    val BookShelfItems = BookShelfItems(
        totalItems = 1,
        items = listOf(
            Items(
                id = "1",
                volumeInfo = VolumeInfo(
                    title = "The Great Gatsby",
                    authors = listOf("Mas"),
                    publisher = "Jane Austen",
                    publishedDate = "2023-09",
                    description = "The Great Gatsby is a novel by American author F. Scott Fitzgerald.",
                    industryIdentifiers = listOf(
                        IndustryIdentifiers(
                            type = "ISBN_10",
                            identifier = "9780743273565"
                    )
                    ),
                    pageCount = 180,
                    categories = listOf("Fiction"),
                    imageLinks = null
                ),
                saleInfo = SaleInfo(
                    retailPrice = RetailPrice(
                        amount = 12.99,
                        currencyCode = "USD"
                    )
                )
            )
        )
    )
}