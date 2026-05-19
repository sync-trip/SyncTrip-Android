package com.example.synctrip

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.synctrip.adapter.RoomAdapter
import com.example.synctrip.dto.group.BandSummary
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

        handleDeepLink(intent)     // ← 앱이 꺼진 상태에서 링크 클릭 시 처리

        tvEmpty = findViewById(R.id.tvEmpty)
        rvRoomList = findViewById(R.id.rvRoomList)
        rvRoomList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvRoomList.adapter = RoomAdapter(
            roomList = roomList,
            onItemClick = { room ->
                val intent = Intent(this, SubActivity::class.java)
                intent.putExtra("ROOM_NAME", room.roomName)
                intent.putExtra("BAND_ID", room.bandId)
                intent.putExtra("INVITE_CODE", room.inviteCode)
                intent.putExtra("START_DATE", room.startDate)
                intent.putExtra("END_DATE", room.endDate)
                intent.putExtra("BAND_STATUS", room.status)
                intent.putExtra("OVERSEAS", room.isOverseas)
                startActivity(intent)
            },
            onOptionsClick = { room, anchor -> showRoomOptions(room, anchor) }
        )

        findViewById<View>(R.id.btnNext).setOnClickListener {
            startActivity(Intent(this, CreateRoomActivity::class.java))
        }

        findViewById<View>(R.id.btnJoinRoom).setOnClickListener {
            val input = android.widget.EditText(this)
            input.hint = "초대 코드 입력"
            input.inputType = android.text.InputType.TYPE_CLASS_TEXT

            android.app.AlertDialog.Builder(this)
                .setTitle("초대 코드로 참여")
                .setView(input)
                .setPositiveButton("참여하기") { _, _ ->
                    val code = input.text.toString().trim()
                    if (code.isEmpty()) {
                        android.widget.Toast.makeText(this, "초대 코드를 입력해주세요!", android.widget.Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    RetrofitClient.api.joinBand(com.example.synctrip.dto.group.BandJoinRequest(code))
                        .enqueue(object : retrofit2.Callback<com.example.synctrip.dto.group.BandSummary> {
                            override fun onResponse(call: retrofit2.Call<com.example.synctrip.dto.group.BandSummary>, response: retrofit2.Response<com.example.synctrip.dto.group.BandSummary>) {
                                if (response.isSuccessful) {
                                    android.widget.Toast.makeText(this@MainActivity, "여행 방에 참여했어요!", android.widget.Toast.LENGTH_SHORT).show()
                                    loadMyBands()
                                } else {
                                    when (response.code()) {
                                        401, 403 -> redirectToLogin()
                                        404 -> android.widget.Toast.makeText(this@MainActivity, "유효하지 않은 초대 코드예요", android.widget.Toast.LENGTH_SHORT).show()
                                        409 -> android.widget.Toast.makeText(this@MainActivity, "이미 참여 중인 방이에요", android.widget.Toast.LENGTH_SHORT).show()
                                        410 -> android.widget.Toast.makeText(this@MainActivity, "만료된 초대 코드예요. 방장한테 다시 받아보세요", android.widget.Toast.LENGTH_SHORT).show()
                                        else -> android.widget.Toast.makeText(this@MainActivity, "참여 실패 (${response.code()})", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            override fun onFailure(call: retrofit2.Call<com.example.synctrip.dto.group.BandSummary>, t: Throwable) {
                                android.widget.Toast.makeText(this@MainActivity, "서버 연결 실패", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                .setNegativeButton("취소", null)
                .show()
        }

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

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)          // ← 현재 intent 최신화 (없으면 구버전 intent로 처리됨)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        val code = data.getQueryParameter("code") ?: return
        if (code.isEmpty()) return
        intent.data = null         // ← 처리 후 즉시 지워서 onResume 재진입 시 재호출 방지
        showJoinDialog(code)
    }

    private fun showJoinDialog(code: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle("초대 링크로 참여")
            .setMessage("초대 코드: $code\n이 여행 방에 참여할까요?")
            .setPositiveButton("참여하기") { _, _ ->
                RetrofitClient.api.joinBand(com.example.synctrip.dto.group.BandJoinRequest(code))
                    .enqueue(object : retrofit2.Callback<com.example.synctrip.dto.group.BandSummary> {
                        override fun onResponse(call: retrofit2.Call<com.example.synctrip.dto.group.BandSummary>, response: retrofit2.Response<com.example.synctrip.dto.group.BandSummary>) {
                            if (response.isSuccessful) {
                                android.widget.Toast.makeText(this@MainActivity, "여행 방에 참여했어요!", android.widget.Toast.LENGTH_SHORT).show()
                                loadMyBands()
                            } else {
                                val msg = when (response.code()) {
                                    404 -> "유효하지 않은 초대 코드예요"
                                    409 -> "이미 참여 중인 방이에요"
                                    410 -> "만료된 초대 코드예요. 방장한테 다시 받아보세요"
                                    else -> "참여 실패 (${response.code()})"
                                }
                                android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: retrofit2.Call<com.example.synctrip.dto.group.BandSummary>, t: Throwable) {
                            android.widget.Toast.makeText(this@MainActivity, "서버 연결 실패", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadMyBands()
        findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)?.apply {
            setColorSchemeResources(R.color.primary)
            setOnRefreshListener { loadMyBands { isRefreshing = false } }
        }
    }

    private fun loadMyBands(onDone: (() -> Unit)? = null) {
        RetrofitClient.api.getMyBands()
            .enqueue(object : Callback<List<BandSummary>> {
                override fun onResponse(call: Call<List<BandSummary>>, response: Response<List<BandSummary>>) {
                    onDone?.invoke()
                    if (response.isSuccessful) {
                        val bands = response.body() ?: emptyList()
                        roomList.clear()
                        bands.forEach {
                            roomList.add(Room(
                                bandId = it.id,
                                roomName = it.name,
                                country = "",
                                city = it.destination,
                                memberCount = 0,
                                status = it.status ?: "PLANNING",
                                inviteCode = it.inviteCode,
                                startDate = it.startDate,
                                endDate = it.endDate,
                                isOwner = it.isOwner,
                                isOverseas = it.isOverseas
                            ))
                        }
                        rvRoomList.adapter?.notifyDataSetChanged()
                        updateEmptyView()
                        android.util.Log.d("BandList", "밴드 목록 로드 성공! ${bands.size}개")
                    } else {
                        android.util.Log.e("BandList", "밴드 목록 로드 실패: ${response.code()}")
                        if (response.code() == 401 || response.code() == 403) {
                            redirectToLogin()
                        } else {
                            updateEmptyView()
                        }
                    }
                }

                override fun onFailure(call: Call<List<BandSummary>>, t: Throwable) {
                    android.util.Log.e("BandList", "서버 연결 실패: ${t.message}")
                    onDone?.invoke()
                    updateEmptyView()
                }
            })
    }

    private fun redirectToLogin() {
        TokenManager.clear(this)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun showRoomOptions(room: Room, anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menu.add(0, 0, 0, "방 삭제")
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == 0) confirmDeleteRoom(room)
            true
        }
        popup.show()
    }

    private fun confirmDeleteRoom(room: Room) {
        android.app.AlertDialog.Builder(this)
            .setTitle("방 삭제")
            .setMessage("'${room.roomName}' 방을 삭제할까요?\n삭제된 방은 복구할 수 없어요.")
            .setPositiveButton("삭제") { _, _ -> deleteRoom(room) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteRoom(room: Room) {
        RetrofitClient.api.deleteBand(room.bandId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    val removeFromList = {
                        val idx = roomList.indexOfFirst { it.bandId == room.bandId }
                        if (idx != -1) {
                            roomList.removeAt(idx)
                            rvRoomList.adapter?.notifyItemRemoved(idx)
                        }
                        updateEmptyView()
                    }
                    when {
                        response.isSuccessful -> {
                            removeFromList()
                            android.widget.Toast.makeText(this@MainActivity, "방이 삭제됐어요", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        response.code() == 404 -> {
                            removeFromList()
                            android.widget.Toast.makeText(this@MainActivity, "이미 삭제된 방이에요", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            val errorBody = response.errorBody()?.string() ?: ""
                            android.util.Log.e("DeleteBand", "삭제 실패 ${response.code()}: $errorBody")
                            val msg = if (response.code() == 403) "방장만 삭제할 수 있어요" else "삭제 실패 (${response.code()})"
                            android.widget.Toast.makeText(this@MainActivity, msg, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    android.widget.Toast.makeText(this@MainActivity, "서버 연결 실패", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
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
