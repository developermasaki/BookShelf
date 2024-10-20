package com.example.bookshelf.network

import kotlinx.serialization.Serializable

@Serializable
data class BookShelfItems (
    val totalItems: Int? = null,
    val items: List<Items?>? = null
)

@Serializable
data class Items(
    val id: String? = null,
    val volumeInfo: VolumeInfo? = null,
    val saleInfo: SaleInfo? = null
)

@Serializable
data class VolumeInfo (
    val title: String? = null,
    val authors: List<String?>? = null,
    val publisher: String? = null,
    val publishedDate: String? = null,
    val description: String? = null,
    val industryIdentifiers: List<IndustryIdentifiers?>? = null,
    val pageCount: Int? = null,
    val categories: List<String?>? = null,
    val imageLinks: ImageLinks? = null,
)

@Serializable
data class SaleInfo (
    val retailPrice: RetailPrice? = null
)

@Serializable
data class RetailPrice (
    val amount: Double? = null,
    val currencyCode: String? = null
)

@Serializable
data class IndustryIdentifiers (
    val type: String? = null,
    val identifier: String? = null
)

@Serializable
data class ImageLinks (
    val smallThumbnail: String? = null,
    val thumbnail: String? = null,
    val small: String? = null,
    val medium: String? = null,
    val large: String? = null,
    val extraLarge: String? = null
)