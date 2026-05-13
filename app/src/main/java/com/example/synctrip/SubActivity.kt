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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        val roomName = intent.getStringExtra("ROOM_NAME")
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = roomName
        toolbar.setNavigationOnClickListener { finish() }

        // 처음 시작할 때 홈 탭 보여주기
        loadFragment(HomeFragment())

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_home     -> loadFragment(HomeFragment())
                R.id.tab_schedule -> loadFragment(ScheduleFragment())
                R.id.tab_vote     -> loadFragment(VoteFragment())
                R.id.tab_money    -> loadFragment(MoneyFragment())
                R.id.tab_photo    -> loadFragment(PhotoFragment())
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}