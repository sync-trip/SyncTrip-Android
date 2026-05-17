package com.example.synctrip.dto.kakao

data class PlaceSearchResponse(
    val documents: List<PlaceDocument>
)

data class PlaceDocument(
    val id: String,             // 카카오 장소 ID
    val place_name: String,
    val category_name: String,
    val address_name: String,
    val road_address_name: String,
    val x: String,              // 경도
    val y: String,              // 위도
    val phone: String?,
    val place_url: String?
)