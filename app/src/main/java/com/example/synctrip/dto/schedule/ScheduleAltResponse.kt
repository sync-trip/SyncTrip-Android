package com.example.synctrip.dto.schedule

data class ScheduleAltResponse(
    val scheduleAltId: Long,
    val category: String,
    val priorityScore: Float,
    val place: SchedulePlaceInfo
)
