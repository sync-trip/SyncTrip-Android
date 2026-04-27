package com.example.testappjsh.dto

data class Schedule(
    val time: String,           // 방문 시각 (예: "09:00")
    val placeName: String,      // 장소명
    val duration: Int,          // 체류시간 (분)
    val travelTime: Int,        // 이동시간 (분)
    val warning: String = ""    // 경고 배지 (없으면 빈 문자열)
)