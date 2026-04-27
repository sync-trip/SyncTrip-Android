package com.example.testappjsh

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.dto.Room

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 임시 방 목록 데이터 (나중에 서버에서 받아올 것)
        val roomList = mutableListOf(
            Room("제주도 여행", "한국", "제주", 4),
            Room("도쿄 여행", "일본", "도쿄", 3),
            Room("뉴욕 여행", "미국", "뉴욕", 5)
        )

        // 빈 화면 텍스트
        val tvEmpty = findViewById<android.widget.TextView>(R.id.tvEmpty)

        // RecyclerView 설정
        val rvRoomList = findViewById<RecyclerView>(R.id.rvRoomList)
        rvRoomList.layoutManager = LinearLayoutManager(this)
        rvRoomList.adapter = RoomAdapter(roomList) { room ->
            // 카드 클릭 시 SubActivity로 이동
            val intent = android.content.Intent(this, SubActivity::class.java)
            intent.putExtra("ROOM_NAME", room.roomName)
            startActivity(intent)
        }

        // 방 있으면 빈 화면 숨기기
        if (roomList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRoomList.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRoomList.visibility = View.VISIBLE
        }

        // 방 만들기 버튼
        val myButton = findViewById<android.widget.Button>(R.id.btnNext)
        myButton.setOnClickListener {
            val intent = android.content.Intent(this, CreateRoomActivity::class.java)
            startActivity(intent)
        }
    }
}