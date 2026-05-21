package com.example.synctrip.dto.vote

data class VoteStatusResponse(
    val totalPlaces: Int,
    val myVotedCount: Int,
    val myComplete: Boolean
)
