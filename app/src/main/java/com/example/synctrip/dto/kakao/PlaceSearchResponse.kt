package com.example.synctrip.dto.kakao

data class PlaceSearchResponse(
    val documents: List<PlaceDocument>
)

data class PlaceDocument(
    val id: String,
    val place_name: String,
    val category_name: String,
    val address_name: String,
    val road_address_name: String,
    val x: String,
    val y: String,
    val phone: String?,
    val place_url: String?,
    val rating: Float? = null
)