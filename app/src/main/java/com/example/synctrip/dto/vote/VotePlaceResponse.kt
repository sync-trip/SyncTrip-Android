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
    val myBookmark: Boolean,
    val myVoteResult: Int?  // null=미투표, 1=좋아요, -1=싫어요, 0=자동좋아요(내 북마크)
)
