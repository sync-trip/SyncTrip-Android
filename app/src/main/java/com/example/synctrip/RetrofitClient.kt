package com.example.synctrip

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // 테스트 서버 주소
    private const val BASE_URL = "https://test.sync-trip.app/"

    // Retrofit 인스턴스 생성
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API 서비스 인스턴스
    val api: ApiService = retrofit.create(ApiService::class.java)
}