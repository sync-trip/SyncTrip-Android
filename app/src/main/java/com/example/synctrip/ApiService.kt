package com.example.synctrip

import com.example.synctrip.dto.TestResponse
import com.example.synctrip.dto.kakao.KakaoLoginRequest
import com.example.synctrip.dto.kakao.KakaoLoginResponse
import com.example.synctrip.dto.group.BandInviteCodeResponse
import com.example.synctrip.dto.group.BandJoinRequest
import com.example.synctrip.dto.group.BandMemberResponse
import com.example.synctrip.dto.group.BandSummary
import com.example.synctrip.dto.group.CreateBandRequest
import com.example.synctrip.dto.group.CreateBandResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("api/test")
    fun getTest(): Call<TestResponse>

    @GET("hello")
    fun getHello(): Call<TestResponse>

    @POST("auth/kakao/login")
    fun kakaoLogin(@Body request: KakaoLoginRequest): Call<KakaoLoginResponse>

    @GET("api/bands")
    fun getMyBands(): Call<List<BandSummary>>

    @POST("api/bands")
    fun createBand(@Body request: CreateBandRequest): Call<CreateBandResponse>

    @POST("api/bands/join")
    fun joinBand(@Body request: BandJoinRequest): Call<Void>

    @POST("api/bands/{bandId}/invite-code")
    fun getInviteCode(@Path("bandId") bandId: Long): Call<BandInviteCodeResponse>

    @GET("api/bands/{bandId}/members")
    fun getBandMembers(@Path("bandId") bandId: Long): Call<List<BandMemberResponse>>
}
