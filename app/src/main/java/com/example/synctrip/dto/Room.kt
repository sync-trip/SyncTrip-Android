package com.example.synctrip.dto

data class Room(
    val bandId: Long = 0,
    val roomName: String,
    val country: String,
    val city: String,
    val memberCount: Int,
    val status: String = "PLANNING",
    val inviteCode: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isOwner: Boolean = false,
    val isOverseas: Boolean = false
)
