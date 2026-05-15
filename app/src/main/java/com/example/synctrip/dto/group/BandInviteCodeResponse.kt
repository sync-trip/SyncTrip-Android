package com.example.synctrip.dto.group

data class BandInviteCodeResponse(
    val bandId: Long,
    val inviteCode: String,
    val inviteCodeExpiredAt: String,
    val inviteShareLink: String?,
    val inviteDeepLink: String?
)
