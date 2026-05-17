package com.example.synctrip

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.synctrip.fragment.HomeFragment
import com.example.synctrip.fragment.MoneyFragment
import com.example.synctrip.fragment.PhotoFragment
import com.example.synctrip.fragment.ScheduleFragment
import com.example.synctrip.fragment.VoteFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class SubActivity : AppCompatActivity() {

    private var bandId: Long = -1L
    private var bandStatus: String = "PLANNING"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        val roomName = intent.getStringExtra("ROOM_NAME") ?: ""
        bandId = intent.getLongExtra("BAND_ID", -1L)
        val inviteCode = intent.getStringExtra("INVITE_CODE") ?: ""
        val startDate = intent.getStringExtra("START_DATE") ?: ""
        val endDate = intent.getStringExtra("END_DATE") ?: ""
        bandStatus = intent.getStringExtra("BAND_STATUS") ?: "PLANNING"

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = roomName
        toolbar.setNavigationOnClickListener { finish() }

        loadFragment(HomeFragment.newInstance(bandId, roomName, inviteCode, startDate, endDate, bandStatus))

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_home     -> loadFragment(HomeFragment.newInstance(bandId, roomName, inviteCode, startDate, endDate, bandStatus))
                R.id.tab_schedule -> loadFragment(ScheduleFragment.newInstance(bandId))
                R.id.tab_vote     -> loadFragment(VoteFragment.newInstance(bandId))
                R.id.tab_money    -> loadFragment(MoneyFragment())
                R.id.tab_photo    -> loadFragment(PhotoFragment())
            }
            true
        }
    }

    fun switchToVoteTab() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.tab_vote
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
