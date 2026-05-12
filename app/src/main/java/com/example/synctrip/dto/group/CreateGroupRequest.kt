package com.example.synctrip.dto

data class CreateGroupRequest(
    val title: String,
    val destination: String,
    val maxMembers: Int,
    val startDate: String,
    val endDate: String,
    val travelStyle: String
)