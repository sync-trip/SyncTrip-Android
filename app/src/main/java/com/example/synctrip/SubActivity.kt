package com.example.synctrip

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.example.synctrip.fragment.HomeFragment
import com.example.synctrip.fragment.MoneyFragment
import com.example.synctrip.fragment.PassportFragment
import com.example.synctrip.fragment.PhotoFragment
import com.example.synctrip.fragment.ScheduleFragment
import com.example.synctrip.fragment.VoteFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class SubActivity : AppCompatActivity() {

    private var bandId: Long = -1L
    private var bandStatus: String = "PLANNING"
    private var overseas: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sub)

        // 상태바 → Toolbar, 네비게이션바 → BottomNav에 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.toolbar)) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation)) { v, insets ->
            v.updatePadding(bottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }

        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        bandId = intent.getLongExtra("BAND_ID", -1L)
        val inviteCode = intent.getStringExtra("INVITE_CODE") ?: ""
        val startDate = intent.getStringExtra("START_DATE") ?: ""
        val endDate = intent.getStringExtra("END_DATE") ?: ""
        bandStatus = intent.getStringExtra("BAND_STATUS") ?: "PLANNING"
        overseas = intent.getBooleanExtra("OVERSEAS", false)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = roomName
        toolbar.setNavigationOnClickListener { finish() }

        loadFragment(HomeFragment.newInstance(bandId, roomName, inviteCode, startDate, endDate, bandStatus))

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_home     -> loadFragment(HomeFragment.newInstance(bandId, roomName, inviteCode, startDate, endDate, bandStatus))
                R.id.tab_schedule -> loadFragment(ScheduleFragment.newInstance(bandId))
                R.id.tab_passport -> loadFragment(PassportFragment())
                R.id.tab_money    -> loadFragment(MoneyFragment())
                R.id.tab_photo    -> loadFragment(PhotoFragment())
            }
            true
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    fun isOverseas(): Boolean = overseas

    fun updateBandStatus(status: String) {
        bandStatus = status
    }

    // 홈에서 "투표하기" 클릭 시 VoteFragment를 백스택에 올림 → 뒤로가기로 복귀
    fun switchToVoteTab() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, VoteFragment.newInstance(bandId))
            .addToBackStack("vote")
            .commit()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
