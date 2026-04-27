package com.example.testappjsh

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 카카오 로그인 버튼
        val btnKakaoLogin = findViewById<Button>(R.id.btnKakaoLogin)
        btnKakaoLogin.setOnClickListener {
            // 나중에 실제 카카오 로그인으로 교체
            Toast.makeText(this, "카카오 로그인 준비 중!", Toast.LENGTH_SHORT).show()
            goToMain()
        }

        // 구글 로그인 버튼
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            // 나중에 실제 구글 로그인으로 교체
            Toast.makeText(this, "구글 로그인 준비 중!", Toast.LENGTH_SHORT).show()
            goToMain()
        }
    }

    // MainActivity로 이동
    private fun goToMain() {
        val intent = android.content.Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 로그인 화면 닫기 (뒤로가기로 못 돌아오게)
    }
}