package com.example.synctrip

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.synctrip.adapter.RoomAdapter
import com.example.synctrip.dto.group.BandJoinRequest
import com.example.synctrip.dto.group.BandSummary
import com.example.synctrip.dto.Room
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
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

        handleDeepLink(intent)

        drawerLayout = findViewById(R.id.drawerLayout)

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
                intent.putExtra("DESTINATION", room.city)
                startActivity(intent)
            },
            onOptionsClick = { room, anchor -> showRoomOptions(room, anchor) }
        )

        findViewById<View>(R.id.btnNext).setOnClickListener {
            startActivity(Intent(this, CreateRoomActivity::class.java))
        }

        findViewById<View>(R.id.btnJoinRoom).setOnClickListener {
            showJoinCodeSheet()
        }

        findViewById<View>(R.id.btnMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        setupDrawer()
    }

    private fun setupDrawer() {
        // 네비게이션 바 높이만큼 드로어 하단 패딩 적용
        val drawerRoot = drawerLayout.findViewById<View>(R.id.navDrawerRoot)
        ViewCompat.setOnApplyWindowInsetsListener(drawerRoot) { v, insets ->
            val navBar = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            v.setPadding(0, 0, 0, navBar.bottom)
            insets
        }

        // 프로필 정보 연동
        val userName = TokenManager.getUserName(this)
        val profileImageUrl = TokenManager.getProfileImageUrl(this)
        val userEmail = TokenManager.getUserEmail(this)

        drawerLayout.findViewById<TextView>(R.id.tvUserName).text =
            if (userName.isNullOrEmpty()) "SyncTrip 멤버" else userName

        val tvUserId = drawerLayout.findViewById<TextView>(R.id.tvUserId)
        if (!userEmail.isNullOrEmpty()) {
            tvUserId.text = userEmail
            tvUserId.visibility = android.view.View.VISIBLE
        } else {
            tvUserId.visibility = android.view.View.GONE
        }

        val ivProfile = drawerLayout.findViewById<ImageView>(R.id.ivUserProfile)
        if (!profileImageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(profileImageUrl)
                .circleCrop()
                .placeholder(R.drawable.ic_avatar_default)
                .error(R.drawable.ic_avatar_default)
                .into(ivProfile)
        }

        setupDrawerMenuListeners()
    }

    private fun setupDrawerMenuListeners() {
        drawerLayout.findViewById<View>(R.id.menuNavProfile).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            android.widget.Toast.makeText(this, "준비 중입니다", android.widget.Toast.LENGTH_SHORT).show()
        }

        drawerLayout.findViewById<View>(R.id.menuNavPassport).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            android.widget.Toast.makeText(this, "준비 중입니다", android.widget.Toast.LENGTH_SHORT).show()
        }

        drawerLayout.findViewById<View>(R.id.menuNavNotifications).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            android.widget.Toast.makeText(this, "준비 중입니다", android.widget.Toast.LENGTH_SHORT).show()
        }

        drawerLayout.findViewById<View>(R.id.menuNavSettings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            android.widget.Toast.makeText(this, "준비 중입니다", android.widget.Toast.LENGTH_SHORT).show()
        }

        drawerLayout.findViewById<View>(R.id.menuNavLogout).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            showLogoutDialog()
        }

        drawerLayout.findViewById<View>(R.id.menuNavWithdraw).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            showWithdrawDialog()
        }
    }

    private fun showJoinCodeSheet() {
        val sheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_join, null)
        sheet.setContentView(view)

        val et = view.findViewById<TextInputEditText>(R.id.etInviteCode)
        view.findViewById<View>(R.id.btnJoinConfirm).setOnClickListener {
            val code = et.text?.toString()?.trim() ?: ""
            if (code.isEmpty()) {
                view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilInviteCode)
                    .error = "초대 코드를 입력해주세요"
                return@setOnClickListener
            }
            RetrofitClient.api.joinBand(BandJoinRequest(code))
                .enqueue(object : Callback<BandSummary> {
                    override fun onResponse(call: Call<BandSummary>, response: Response<BandSummary>) {
                        if (response.isSuccessful) {
                            sheet.dismiss()
                            android.widget.Toast.makeText(this@MainActivity, "여행 방에 참여했어요!", android.widget.Toast.LENGTH_SHORT).show()
                            loadMyBands()
                        } else {
                            val msg = when (response.code()) {
                                401, 403 -> { redirectToLogin(); return }
                                404 -> "유효하지 않은 초대 코드예요"
                                409 -> "이미 참여 중인 방이에요"
                                410 -> "만료된 초대 코드예요. 방장한테 다시 받아보세요"
                                else -> "참여 실패 (${response.code()})"
                            }
                            view.findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilInviteCode)
                                .error = msg
                        }
                    }
                    override fun onFailure(call: Call<BandSummary>, t: Throwable) {
                        android.widget.Toast.makeText(this@MainActivity, "서버 연결 실패", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
        }
        sheet.show()
    }

    private fun showLogoutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("로그아웃")
            .setMessage("정말 로그아웃 할까요?")
            .setPositiveButton("로그아웃") { _, _ ->
                RetrofitClient.api.logout().enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {}
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
                TokenManager.clear(this)
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showWithdrawDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("회원 탈퇴")
            .setMessage("정말 탈퇴할까요?\n탈퇴하면 모든 여행 데이터가 삭제되며 복구할 수 없어요.")
            .setPositiveButton("탈퇴") { _, _ ->
                RetrofitClient.api.withdraw().enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        TokenManager.clear(this@MainActivity)
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        android.widget.Toast.makeText(this@MainActivity, "탈퇴 처리 실패. 다시 시도해주세요.", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
            }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: return
        val code = data.getQueryParameter("code") ?: return
        if (code.isEmpty()) return
        intent.data = null
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

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
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
        val popup = android.widget.PopupMenu(this, anchor)
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
