package com.example.testappjsh

import com.example.testappjsh.dto.TestResponse
import com.example.testappjsh.dto.KakaoLoginRequest
import com.example.testappjsh.dto.KakaoLoginResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("api/test")
    fun getTest(): Call<TestResponse>

    @GET("hello")
    fun getHello(): Call<TestResponse>
    @POST("api/auth/kakao")
    fun kakaoLogin(@Body request: KakaoLoginRequest): Call<KakaoLoginResponse>
}