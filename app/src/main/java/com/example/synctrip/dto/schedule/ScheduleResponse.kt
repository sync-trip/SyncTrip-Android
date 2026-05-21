package com.example.synctrip.dto.schedule

data class ScheduleResponse(
    val bandId: Long,
    val startDate: String,
    val endDate: String,
    val days: List<ScheduleDayResponse>
)
