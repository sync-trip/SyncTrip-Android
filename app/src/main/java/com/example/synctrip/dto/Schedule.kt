package com.example.synctrip.dto

data class Schedule(
    val time: String,
    val placeName: String,
    val duration: Int,
    val travelTime: Int,
    val warning: String = "",
    val category: String = "ETC"
)
