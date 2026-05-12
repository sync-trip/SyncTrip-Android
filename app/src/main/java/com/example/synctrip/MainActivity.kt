package com.example.synctrip

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.adapter.RoomAdapter
import com.example.synctrip.dto.GroupListResponse
import com.example.synctrip.dto.Room
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var rvRoomList: RecyclerView
    private lateinit var tvEmpty: android.widget.TextView
    private val roomList = mutableListOf<Room>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvEmpty = findViewById(R.id.tvEmpty)
        rvRoomList = findViewById(R.id.rvRoomList)
        rvRoomList.layoutManager = GridLayoutManager(this, 2)
        rvRoomList.adapter = RoomAdapter(roomList) { room ->
            val intent = Intent(this, SubActivity::class.java)
            intent.putExtra("ROOM_NAME", room.roomName)
            intent.putExtra("GROUP_ID", room.groupId)
            startActivity(intent)
        }

        // 방 만들기 버튼
        findViewById<android.widget.Button>(R.id.btnNext).setOnClickListener {
            startActivity(Intent(this, CreateRoomActivity::class.java))
        }

        // 초대 코드로 참여 버튼
        findViewById<android.widget.Button>(R.id.btnJoinRoom).setOnClickListener {
            val input = android.widget.EditText(this)
            input.hint = "초대 코드 6자리 입력"
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT

            android.app.AlertDialog.Builder(this)
                .setTitle("초대 코드로 참여")
                .setView(input)
                .setPositiveButton("참여하기") { _, _ ->
                    val code = input.text.toString()
                    if (code.length == 6) {
                        android.widget.Toast.makeText(this, "코드 [$code] 확인 중...", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        android.widget.Toast.makeText(this, "6자리 코드를 입력해주세요!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("취소", null)
                .show()
        }

        // 설정 버튼 (로그아웃)
        findViewById<android.widget.ImageButton>(R.id.btnSettings).setOnClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("설정")
                .setItems(arrayOf("로그아웃")) { _, which ->
                    when (which) {
                        0 -> {
                            TokenManager.clear(this)
                            val intent = Intent(this, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
                .show()
        }

        // 그룹 목록 불러오기
        loadMyGroups()
    }

    override fun onResume() {
        super.onResume()
        loadMyGroups()
    }

    private fun loadMyGroups() {
        RetrofitClient.api.getMyGroups()
            .enqueue(object : Callback<GroupListResponse> {
                override fun onResponse(call: Call<GroupListResponse>, response: Response<GroupListResponse>) {
                    if (response.isSuccessful) {
                        val groups = response.body()?.groups ?: emptyList()
                        roomList.clear()
                        groups.forEach {
                            roomList.add(Room(
                                groupId = it.groupId,
                                roomName = it.title,
                                country = "",
                                city = it.destination,
                                memberCount = it.memberCount,
                                status = it.status
                            ))
                        }
                        rvRoomList.adapter?.notifyDataSetChanged()
                        updateEmptyView()
                        android.util.Log.d("GroupList", "그룹 목록 로드 성공! ${groups.size}개")
                    } else {
                        android.util.Log.e("GroupList", "그룹 목록 로드 실패: ${response.code()}")
                        showTempData()
                    }
                }

                override fun onFailure(call: Call<GroupListResponse>, t: Throwable) {
                    android.util.Log.e("GroupList", "서버 연결 실패: ${t.message}")
                    showTempData()
                }
            })
    }

    private fun showTempData() {
        roomList.clear()
        roomList.addAll(mutableListOf(
            Room(1, "제주도 여행", "한국", "제주", 4),
            Room(2, "도쿄 여행", "일본", "도쿄", 3),
            Room(3, "뉴욕 여행", "미국", "뉴욕", 5)
        ))
        rvRoomList.adapter?.notifyDataSetChanged()
        updateEmptyView()
    }

    private fun updateEmptyView() {
        if (roomList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRoomList.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRoomList.visibility = View.VISIBLE
        }
    }
}