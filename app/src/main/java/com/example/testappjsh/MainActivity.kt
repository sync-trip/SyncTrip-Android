package com.example.testappjsh

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.adapter.RoomAdapter
import com.example.testappjsh.dto.Room
import com.example.testappjsh.dto.TestResponse

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
        rvRoomList.layoutManager = GridLayoutManager(this, 2)
        rvRoomList.adapter = RoomAdapter(roomList) { room ->
            // 카드 클릭 시 SubActivity로 이동
            val intent = Intent(this, SubActivity::class.java)
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
        // 초대 코드로 참여 버튼
        val btnJoinRoom = findViewById<android.widget.Button>(R.id.btnJoinRoom)
        btnJoinRoom.setOnClickListener {
            // 초대 코드 입력 다이얼로그 표시
            val input = android.widget.EditText(this)
            input.hint = "초대 코드 6자리 입력"
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT

            android.app.AlertDialog.Builder(this)
                .setTitle("초대 코드로 참여")
                .setView(input)
                .setPositiveButton("참여하기") { _, _ ->
                    val code = input.text.toString()
                    if (code.length == 6) {
                        // 나중에 서버에 코드 확인 요청
                        android.widget.Toast.makeText(
                            this,
                            "코드 [$code] 확인 중... (서버 연동 후 완성)",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        android.widget.Toast.makeText(
                            this,
                            "6자리 코드를 입력해주세요!",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }
        // 서버 연결 테스트
        RetrofitClient.api.getTest().enqueue(object : retrofit2.Callback<TestResponse> {
            override fun onResponse(call: retrofit2.Call<TestResponse>, response: retrofit2.Response<TestResponse>) {
                if (response.isSuccessful) {
                    android.util.Log.d("ServerTest", "서버 연결 성공! 응답: ${response.body()}")
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        "서버 연결 성공! 🎉",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            override fun onFailure(call: retrofit2.Call<TestResponse>, t: Throwable) {
                android.util.Log.e("ServerTest", "서버 연결 실패: ${t.message}")
            }
        })
    }
}