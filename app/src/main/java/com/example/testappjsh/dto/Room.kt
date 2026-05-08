package com.example.testappjsh.dto

data class Room(
    val groupId: Long = 0,
    val roomName: String,
    val country: String,
    val city: String,
    val memberCount: Int,
    val status: String = "PLANNING"
)