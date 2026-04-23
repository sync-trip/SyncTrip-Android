package com.example.testappjsh

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CreateRoomActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_room)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val btnCreateDone = findViewById<android.widget.Button>(R.id.btnCreateDone)

        btnCreateDone.setOnClickListener {
            // 안내 문구 띄우기
            android.widget.Toast.makeText(this, "여행 방이 생성되었습니다!", android.widget.Toast.LENGTH_SHORT)
                .show()

            // SubActivity(방 내부 화면)로 이동하기
            val intent = android.content.Intent(this, SubActivity::class.java)
            startActivity(intent)

            // 현재 방 만들기 화면은 임무를 다했으니 닫기 (뒤로가기 방지)
            finish()
        }
    }
}