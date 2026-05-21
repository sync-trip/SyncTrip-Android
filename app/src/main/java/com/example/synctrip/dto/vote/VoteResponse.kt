package com.example.synctrip.dto.vote

data class VoteResponse(
    val voteId: Long,
    val placeId: Long,
    val result: Int,
    val votedAt: String
)
