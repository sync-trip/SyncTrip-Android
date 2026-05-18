package com.example.synctrip.dto.group

data class PlacePickResponse(
    val placeBookmarkId: Long,
    val placeId: Long,
    val apiSource: String,
    val externalId: String,
    val name: String,
    val category: String,
    val densityPoint: Int,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float?,
    val thumbnailUrl: String?,
    val openingHoursJson: String?,
    val estimatedDuration: Int,
    val createdAt: String?
)

data class PlacePickListResponse(
    val currentCount: Int,
    val maxCount: Int,
    val items: List<PlacePickResponse>
)
