package com.example.synctrip.dto.destination

data class DestinationResponse(
    val name: String,
    val country: String,
    val countryCode: String?,
    val lat: Double,
    val lng: Double,
    val overseas: Boolean,
    val region: String,
    val description: String?,
    val thumbnailUrl: String?
)
