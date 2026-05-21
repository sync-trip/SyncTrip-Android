package com.example.synctrip.dto.group

data class PlacePickRequest(
    val apiSource: String = "KAKAO",
    val externalId: String,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val rating: Float? = null,
    val thumbnailUrl: String? = null,
    val openingHoursJson: String? = null,
    val estimatedDuration: Int = 60
)
