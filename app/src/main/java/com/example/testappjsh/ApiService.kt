package com.example.testappjsh

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    // 서버 연결 테스트
    @GET("api/test")
    fun getTest(): Call<String>

    // 헬로 테스트
    @GET("hello")
    fun getHello(): Call<String>

}