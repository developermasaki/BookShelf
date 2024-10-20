package com.example.bookshelf.fake

import com.example.bookshelf.network.BookShelfItems
import com.example.bookshelf.network.IndustryIdentifiers
import com.example.bookshelf.network.Items
import com.example.bookshelf.network.RetailPrice
import com.example.bookshelf.network.SaleInfo
import com.example.bookshelf.network.VolumeInfo

object FakeDataSource {
    val BookShelfItems = BookShelfItems(
        totalItems = 1,
        items = listOf(
            Items(
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
                    )),
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