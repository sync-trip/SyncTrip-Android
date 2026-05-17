package com.example.synctrip.dto.schedule

data class ScheduleSlotResponse(
    val scheduleId: Long,
    val slotOrder: Int,
    val startTime: String?,       // "HH:mm:ss"
    val durationMinutes: Int?,
    val travelTimeFromPrev: Int?,
    val place: SchedulePlaceInfo
)
