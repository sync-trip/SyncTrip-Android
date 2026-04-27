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
        val btnCreateDone = findViewById<android.widget.Button>(R.id.btnCreateDone)

        btnCreateDone.setOnClickListener {
            // 1. 입력값 가져오기
            val roomName = findViewById<android.widget.EditText>(R.id.etRoomName).text.toString()
            val country = findViewById<android.widget.EditText>(R.id.etCountry).text.toString()
            val city = findViewById<android.widget.EditText>(R.id.etCity).text.toString()

            // 2. Toast
            android.widget.Toast.makeText(this, "여행 방이 생성되었습니다!", android.widget.Toast.LENGTH_SHORT).show()

            // 3. Intent에 담아서 출발
            val intent = android.content.Intent(this, SubActivity::class.java)
            intent.putExtra("ROOM_NAME", roomName)  // 키="ROOM_NAME", 값=입력한 방이름
            intent.putExtra("COUNTRY", country)
            intent.putExtra("CITY", city)
            startActivity(intent)

            finish()
        }
    }
}