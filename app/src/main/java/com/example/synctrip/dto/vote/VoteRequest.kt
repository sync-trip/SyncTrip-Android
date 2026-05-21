package com.example.synctrip.dto.vote

data class VoteRequest(
    val placeId: Long,
    val result: Int   // 1=LIKE, -1=DISLIKE
)
