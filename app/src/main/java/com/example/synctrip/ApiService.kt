package com.example.synctrip

import com.example.synctrip.dto.TestResponse
import com.example.synctrip.dto.kakao.KakaoLoginRequest
import com.example.synctrip.dto.kakao.KakaoLoginResponse
import com.example.synctrip.dto.CreateGroupRequest
import com.example.synctrip.dto.CreateGroupResponse
import com.example.synctrip.dto.GroupListResponse
import com.example.synctrip.dto.GroupDetailResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/test")
    fun getTest(): Call<TestResponse>

    @GET("hello")
    fun getHello(): Call<TestResponse>

    @POST("auth/kakao/login")
    fun kakaoLogin(@Body request: KakaoLoginRequest): Call<KakaoLoginResponse>

    // 그룹 생성
    @POST("api/groups")
    fun createGroup(@Body request: CreateGroupRequest): Call<CreateGroupResponse>

    // 내 그룹 목록
    @GET("api/groups")
    fun getMyGroups(): Call<GroupListResponse>

    // 그룹 상세
    @GET("api/groups/{id}")
    fun getGroupDetail(@Path("id") groupId: Long): Call<GroupDetailResponse>
}