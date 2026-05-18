package com.example.synctrip.dto.group

data class BandSummary(
    val id: Long,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val inviteCode: String,
    val status: String? = null,
    val isOwner: Boolean = false
)
