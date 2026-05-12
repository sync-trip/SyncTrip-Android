package com.example.synctrip

import com.example.synctrip.dto.kakao.PlaceSearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface KakaoApiService {

    @GET("v2/local/search/keyword.json")
    fun searchPlaces(
        @Header("Authorization") auth: String,
        @Query("query") query: String
    ): Call<PlaceSearchResponse>
}