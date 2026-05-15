package com.example.synctrip.dto.group

data class BandMemberResponse(
    val userId: Long,
    val name: String,
    val profileImageUrl: String?,
    val role: String,
    val isReady: Boolean
)
