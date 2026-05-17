package com.example.synctrip.dto.vote

data class GroupVoteStatusResponse(
    val totalPlaces: Int,
    val totalVotingMembers: Int,
    val memberStatuses: List<MemberVoteStatus>
)
