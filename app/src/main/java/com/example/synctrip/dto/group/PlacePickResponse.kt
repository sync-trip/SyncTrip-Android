package com.example.synctrip.dto.group

data class PlacePickResponse(
    val placeBookmarkId: Long,
    val placeId: Long,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float?,
    val thumbnailUrl: String?,
    val estimatedDuration: Int
)

data class PlacePickListResponse(
    val currentCount: Int,
    val maxCount: Int,
    val items: List<PlacePickResponse>
)
