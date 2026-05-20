package com.example.synctrip

import com.example.synctrip.dto.TestResponse
import com.example.synctrip.dto.band.BandReadyResponse
import com.example.synctrip.dto.band.BandStatusTransitionResponse
import com.example.synctrip.dto.destination.DestinationResponse
import com.example.synctrip.dto.kakao.KakaoLoginRequest
import com.example.synctrip.dto.kakao.KakaoLoginResponse
import com.example.synctrip.dto.kakao.TokenRefreshRequest
import com.example.synctrip.dto.group.BandInviteCodeResponse
import com.example.synctrip.dto.group.BandJoinRequest
import com.example.synctrip.dto.group.BandMemberResponse
import com.example.synctrip.dto.group.BandSummary
import com.example.synctrip.dto.group.CreateBandRequest
import com.example.synctrip.dto.group.PlacePickListResponse
import com.example.synctrip.dto.group.PlacePickRequest
import com.example.synctrip.dto.group.PlacePickResponse
import com.example.synctrip.dto.place.PlaceSearchResult
import com.example.synctrip.dto.schedule.ScheduleAltResponse
import com.example.synctrip.dto.schedule.ScheduleResponse
import com.example.synctrip.dto.vote.GroupVoteStatusResponse
import com.example.synctrip.dto.vote.VotePlaceResponse
import com.example.synctrip.dto.vote.VoteRequest
import com.example.synctrip.dto.vote.VoteResponse
import com.example.synctrip.dto.vote.VoteStatusResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/test")
    fun getTest(): Call<TestResponse>

    @GET("hello")
    fun getHello(): Call<TestResponse>

    // ─── Destinations ────────────────────────────────────────────────────────

    @GET("api/destinations/popular")
    fun getPopularDestinations(): Call<List<DestinationResponse>>

    @GET("api/destinations/search")
    fun searchDestinations(@Query("query") query: String): Call<List<DestinationResponse>>

    @GET("api/bands/{bandId}/places/search")
    fun searchOverseasPlaces(
        @Path("bandId") bandId: Long,
        @Query("keyword") keyword: String? = null,
        @Query("category") category: String? = null
    ): Call<List<PlaceSearchResult>>

    // ─── Auth ───────────────────────────────────────────────────────────────

    @POST("auth/kakao/login")
    fun kakaoLogin(@Body request: KakaoLoginRequest): Call<KakaoLoginResponse>

    @POST("auth/kakao/refresh")
    fun refreshToken(@Body request: TokenRefreshRequest): Call<KakaoLoginResponse>

    @POST("auth/kakao/logout")
    fun logout(): Call<Void>

    @DELETE("auth/kakao/withdraw")
    fun withdraw(): Call<Void>

    // ─── Band ───────────────────────────────────────────────────────────────

    @GET("api/bands")
    fun getMyBands(): Call<List<BandSummary>>

    @POST("api/bands")
    fun createBand(@Body request: CreateBandRequest): Call<BandSummary>

    @POST("api/bands/join")
    fun joinBand(@Body request: BandJoinRequest): Call<BandSummary>

    @POST("api/bands/{bandId}/invite-code")
    fun getInviteCode(@Path("bandId") bandId: Long): Call<BandInviteCodeResponse>

    @POST("api/bands/{bandId}/invite-code/reissue")
    fun reissueInviteCode(@Path("bandId") bandId: Long): Call<BandInviteCodeResponse>

    @GET("api/bands/{bandId}/members")
    fun getBandMembers(@Path("bandId") bandId: Long): Call<List<BandMemberResponse>>

    @POST("api/bands/{bandId}/ready")
    fun setReady(@Path("bandId") bandId: Long): Call<BandReadyResponse>

    @DELETE("api/bands/{bandId}/ready")
    fun cancelReady(@Path("bandId") bandId: Long): Call<BandReadyResponse>

    @POST("api/bands/{bandId}/status/advance")
    fun advanceBandStatus(@Path("bandId") bandId: Long): Call<BandStatusTransitionResponse>

    @DELETE("api/bands/{bandId}")
    fun deleteBand(@Path("bandId") bandId: Long): Call<Void>

    // ─── Picks ──────────────────────────────────────────────────────────────

    @GET("api/bands/{bandId}/picks")
    fun getPicks(@Path("bandId") bandId: Long): Call<PlacePickListResponse>

    @POST("api/bands/{bandId}/picks")
    fun addPick(@Path("bandId") bandId: Long, @Body request: PlacePickRequest): Call<PlacePickResponse>

    @DELETE("api/bands/{bandId}/picks/{placeId}")
    fun deletePick(@Path("bandId") bandId: Long, @Path("placeId") placeId: Long): Call<Void>

    // ─── Vote ────────────────────────────────────────────────────────────────

    @GET("api/bands/{bandId}/votes/places")
    fun getVotePlaces(@Path("bandId") bandId: Long): Call<List<VotePlaceResponse>>

    @POST("api/bands/{bandId}/votes")
    fun vote(@Path("bandId") bandId: Long, @Body request: VoteRequest): Call<VoteResponse>

    @GET("api/bands/{bandId}/votes/status")
    fun getMyVoteStatus(@Path("bandId") bandId: Long): Call<VoteStatusResponse>

    @GET("api/bands/{bandId}/votes/status/group")
    fun getGroupVoteStatus(@Path("bandId") bandId: Long): Call<GroupVoteStatusResponse>

    // ─── Schedule ────────────────────────────────────────────────────────────

    @POST("api/bands/{bandId}/schedule/generate")
    fun generateSchedule(@Path("bandId") bandId: Long): Call<Void>

    @GET("api/bands/{bandId}/schedule")
    fun getSchedule(@Path("bandId") bandId: Long): Call<ScheduleResponse>

    @GET("api/bands/{bandId}/schedule/alts")
    fun getScheduleAlts(@Path("bandId") bandId: Long): Call<List<ScheduleAltResponse>>
}
