package com.example.synctrip.dto.group

data class CreateBandRequest(
    val name: String,
    val startDate: String,
    val endDate: String,
    val destination: String,
    val destinationLat: Double,
    val destinationLng: Double,
    val countryCode: String?,
    val overseas: Boolean,
    val travelStyle: String  // "RELAXED" or "PACKED"
)