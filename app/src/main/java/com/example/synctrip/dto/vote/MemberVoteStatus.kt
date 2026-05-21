package com.example.synctrip.dto.vote

data class MemberVoteStatus(
    val userId: Long,
    val name: String,
    val votedCount: Int,
    val complete: Boolean
)
