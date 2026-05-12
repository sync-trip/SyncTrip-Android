package com.example.synctrip

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object KakaoRetrofitClient {

    private const val BASE_URL = "https://dapi.kakao.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: KakaoApiService = retrofit.create(KakaoApiService::class.java)
}