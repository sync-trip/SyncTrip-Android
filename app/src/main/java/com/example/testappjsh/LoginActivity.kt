package com.example.testappjsh

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import com.example.testappjsh.dto.KakaoLoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.testappjsh.dto.KakaoLoginResponse

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        KakaoSdk.init(this, "5215e216aaf94a9f15cb57f5256a1765")

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
        android.util.Log.d("KakaoToken", "토큰 서버 전송 중: $accessToken")

        RetrofitClient.api.kakaoLogin(KakaoLoginRequest(accessToken))
            .enqueue(object : Callback<KakaoLoginResponse> {
                override fun onResponse(call: Call<KakaoLoginResponse>, response: Response<KakaoLoginResponse>) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        val accessToken = body?.accessToken
                        val userId = body?.userId

                        if (accessToken != null && userId != null) {
                            TokenManager.saveToken(this@LoginActivity, accessToken)
                            TokenManager.saveUserId(this@LoginActivity, userId)
                        }

                        android.util.Log.d("KakaoToken", "JWT 받음: $accessToken")
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, "서버 로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<KakaoLoginResponse>, t: Throwable) {
                    android.util.Log.e("KakaoToken", "서버 전송 실패: ${t.message}")
                    Toast.makeText(this@LoginActivity, "서버 연결 실패!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun goToMain() {
        val intent = android.content.Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}