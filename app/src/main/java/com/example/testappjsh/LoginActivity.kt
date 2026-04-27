package com.example.testappjsh

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 카카오 SDK 초기화
        KakaoSdk.init(this, "5215e216aaf94a9f15cb57f5256a1765")

        // 카카오 로그인 버튼
        val btnKakaoLogin = findViewById<Button>(R.id.btnKakaoLogin)
        btnKakaoLogin.setOnClickListener {
            // 카카오톡 설치 여부 확인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                // 카카오톡으로 로그인
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Toast.makeText(this, "카카오톡 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else if (token != null) {
                        Toast.makeText(this, "카카오톡 로그인 성공!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    }
                }
            } else {
                // 카카오 계정으로 로그인 (카카오톡 미설치 시)
                UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
                    if (error != null) {
                        Toast.makeText(this, "카카오 로그인 실패: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else if (token != null) {
                        Toast.makeText(this, "카카오 로그인 성공!", Toast.LENGTH_SHORT).show()
                        goToMain()
                    }
                }
            }
        }

        // 구글 로그인 버튼
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "구글 로그인 준비 중!", Toast.LENGTH_SHORT).show()
            goToMain()
        }
    }

    private fun goToMain() {
        val intent = android.content.Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}