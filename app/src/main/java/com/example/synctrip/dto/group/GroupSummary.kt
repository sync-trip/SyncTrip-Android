package com.example.synctrip.dto

data class GroupSummary(
    val groupId: Long,
    val title: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val memberCount: Int,
    val status: String
)