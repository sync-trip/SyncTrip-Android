package com.example.synctrip

import android.content.Context
import android.content.Intent
import com.example.synctrip.dto.kakao.KakaoLoginResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://test.sync-trip.app/"
    private var retrofit: Retrofit? = null
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext

        val authInterceptor = Interceptor { chain ->
            val token = TokenManager.getToken(context)
            android.util.Log.d("RetrofitClient", "${chain.request().method} ${chain.request().url} | token=${if (token != null) "있음(${token.take(20)}...)" else "없음(null)"}")
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                chain.request()
            }
            val response = chain.proceed(request)
            if (!response.isSuccessful) {
                val body = response.peekBody(Long.MAX_VALUE).string()
                android.util.Log.e("RetrofitClient", "실패 ${response.code} | body=$body")
            }
            response
        }

        val sessionInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            var response = chain.proceed(originalRequest)

            if (response.code == 401) {
                val ctx = appContext
                val refreshToken = if (ctx != null) TokenManager.getRefreshToken(ctx) else null

                if (ctx != null && refreshToken != null) {
                    response.close()
                    val newTokens = tryRefreshToken(refreshToken)
                    if (newTokens != null) {
                        TokenManager.saveLoginResponse(ctx, newTokens.accessToken, newTokens.refreshToken,
                            newTokens.userId, newTokens.accessTokenExpiresIn)
                        val retryRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                        response = chain.proceed(retryRequest)
                    } else {
                        redirectToLogin(ctx)
                    }
                } else if (ctx != null) {
                    redirectToLogin(ctx)
                }
            }
            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(sessionInterceptor)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService
        get() = retrofit!!.create(ApiService::class.java)

    private fun tryRefreshToken(refreshToken: String): KakaoLoginResponse? {
        return try {
            val body = """{"refreshToken":"$refreshToken"}"""
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("${BASE_URL}auth/kakao/refresh")
                .post(body)
                .build()
            val response = OkHttpClient().newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: return null
                Gson().fromJson(json, KakaoLoginResponse::class.java)
            } else null
        } catch (e: Exception) {
            android.util.Log.e("RetrofitClient", "토큰 재발급 실패: ${e.message}")
            null
        }
    }

    private fun redirectToLogin(ctx: Context) {
        TokenManager.clear(ctx)
        val intent = Intent(ctx, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        ctx.startActivity(intent)
    }
}
