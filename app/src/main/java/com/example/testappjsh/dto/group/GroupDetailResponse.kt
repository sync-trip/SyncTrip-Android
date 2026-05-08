package com.example.testappjsh.dto

data class GroupDetailResponse(
    val groupId: Long,
    val title: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val memberCount: Int,
    val maxMembers: Int,
    val inviteCode: String,
    val travelStyle: String,
    val status: String
)