package com.example.testappjsh

import com.example.testappjsh.dto.TestResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {

    @GET("api/test")
    fun getTest(): Call<TestResponse>

    @GET("hello")
    fun getHello(): Call<TestResponse>
}