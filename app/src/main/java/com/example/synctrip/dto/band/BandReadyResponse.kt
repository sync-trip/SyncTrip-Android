package com.example.synctrip.dto.band

data class BandReadyResponse(
    val bandId: Long,
    val userId: Long,
    val isReady: Boolean,
    val readyCount: Long,
    val totalCount: Long,
    val allReady: Boolean,
    val bandStatus: String
)
