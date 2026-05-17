package com.example.synctrip

import android.content.Context
import android.content.Intent
import okhttp3.OkHttpClient
import okhttp3.Interceptor
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
            val response = chain.proceed(chain.request())
            // 토큰 만료: 인증이 필요한 엔드포인트에서 401 응답 시 로그인 화면으로
            if (response.code == 401) {
                appContext?.let { ctx ->
                    TokenManager.clear(ctx)
                    val intent = Intent(ctx, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    ctx.startActivity(intent)
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
}
