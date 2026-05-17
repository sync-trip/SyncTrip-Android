package com.example.synctrip.dto.band

data class BandStatusTransitionResponse(
    val bandId: Long,
    val previousStatus: String,
    val currentStatus: String
)
