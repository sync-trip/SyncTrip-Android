package com.example.synctrip.dto.schedule

data class ScheduleDayResponse(
    val dayNumber: Int,
    val date: String,
    val slots: List<ScheduleSlotResponse>
)
