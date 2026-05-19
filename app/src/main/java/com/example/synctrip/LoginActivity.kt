package com.example.synctrip

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.example.synctrip.dto.kakao.KakaoLoginRequest
import com.kakao.vectormap.KakaoMapSdk
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.synctrip.dto.kakao.KakaoLoginResponse
import com.example.synctrip.BuildConfig

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // 카카오 SDK 초기화 (자동 로그인 분기 전에 항상 먼저 실행)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        RetrofitClient.init(this)  // ← 여기 추가
        // 자동 로그인 체크
        if (TokenManager.isLoggedIn(this)) {
            if (TokenManager.isAccessTokenExpired(this)) {
                // accessToken 만료 → refreshToken으로 자동 재발급
                val refreshToken = TokenManager.getRefreshToken(this)
                if (refreshToken != null) {
                    refreshAndGoToMain(refreshToken)
                } else {
                    goToMain() // 기존 사용자 (refreshToken 없음) → 그냥 진입
                }
            } else {
                goToMain()
            }
            return
        }

        val btnKakaoLogin = findViewById<Button>(R.id.btnKakaoLogin)
        btnKakaoLogin.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Toast.makeText(this, "카카오톡 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else if (token != null) {
                        sendTokenToServer(token.accessToken)
                    }
                }
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    if (error != null) {
                        Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else if (token != null) {
                        sendTokenToServer(token.accessToken)
                    }
                }
            }
        }

        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "구글 로그인 준비 중!", Toast.LENGTH_SHORT).show()
            goToMain()
        }
    }

    private fun sendTokenToServer(accessToken: String) {
        // 1️⃣ [추가한 로그] 핑(Ping) 날리기 직전 확인
        android.util.Log.d("KakaoToken", "1. 안드로이드 -> 서버로 토큰 던지기 시작! (무한대기 타는지 감시 시작)")

        RetrofitClient.api.kakaoLogin(KakaoLoginRequest(accessToken))
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(call: Call<KakaoLoginResponse>, response: Response<KakaoLoginResponse>) {
                    // 2️⃣ [추가한 로그] 서버가 응답을 주긴 줬을 때 (성공이든 에러든 무조건 찍힘)
                    android.util.Log.d("KakaoToken", "2. 서버 응답 도착! HTTP 상태 코드: ${response.code()}")

                    if (response.isSuccessful) {
                        val body = response.body()
                        val responseAccessToken = body?.accessToken
                        val userId = body?.userId

                        if (responseAccessToken != null && userId != null) {
                            TokenManager.saveLoginResponse(
                                this@LoginActivity,
                                responseAccessToken,
                                body?.refreshToken ?: "",
                                userId,
                                body?.accessTokenExpiresIn ?: 3600L
                            )
                        }

                        android.util.Log.d("KakaoToken", "3. JWT 받음 완료: $responseAccessToken")
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    } else {
                        android.util.Log.e("KakaoToken", "서버 로그인 실패 내용: ${response.errorBody()?.string()}")
                        Toast.makeText(this@LoginActivity, "서버 로그인 에러: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    // 3️⃣ [수정한 로그] 타임아웃 나거나 서버 문이 닫혀있을 때
                    android.util.Log.e("KakaoToken", "2. 타임아웃/서버 다운! (서버가 끝까지 대답 안 함): ${t.message}")
                    Toast.makeText(this@LoginActivity, "서버 연결 아예 실패!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun refreshAndGoToMain(refreshToken: String) {
        RetrofitClient.api.refreshToken(com.example.synctrip.dto.kakao.TokenRefreshRequest(refreshToken))
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(call: Call<KakaoLoginResponse>, response: Response<KakaoLoginResponse>) {
                    val body = response.body()
                    if (response.isSuccessful && body != null) {
                        TokenManager.saveLoginResponse(
                            this@LoginActivity,
                            body.accessToken,
                            body.refreshToken,
                            body.userId,
                            body.accessTokenExpiresIn
                        )
                        goToMain()
                    } else {
                        // 재발급 실패 → 로그인 화면 유지
                        TokenManager.clear(this@LoginActivity)
                    }
                }
                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    // 네트워크 오류 → 일단 기존 토큰으로 진입 시도
                    goToMain()
                }
            })
    }

    private fun goToMain() {
        val intent = android.content.Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}