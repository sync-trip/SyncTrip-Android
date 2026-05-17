package com.example.synctrip.dto.vote

data class VotePlaceResponse(
    val placeId: Long,
    val apiSource: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float?,
    val thumbnailUrl: String?,
    val myBookmark: Boolean
)
