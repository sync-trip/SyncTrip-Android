package com.example.synctrip.dto.group

data class BandDetailResponse(
    val bandId: Long,
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val memberCount: Int,
    val maxMembers: Int,
    val inviteCode: String,
    val countryCode: String,
    val overseas: Boolean,
    val status: String
)
