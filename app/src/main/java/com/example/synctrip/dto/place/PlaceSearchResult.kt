package com.example.synctrip.dto.place

data class PlaceSearchResult(
    val placeId: Long,
    val apiSource: String,
    val externalId: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float?,
    val thumbnailUrl: String,
    val isBookmarked: Boolean
)
