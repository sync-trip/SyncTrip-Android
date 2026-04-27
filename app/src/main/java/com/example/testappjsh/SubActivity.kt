package com.example.testappjsh

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class SubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        // 처음 시작할 때 일정 탭 보여주기
        loadFragment(ScheduleFragment())
        val roomName = intent.getStringExtra("ROOM_NAME")
        val tvRoomName = findViewById<android.widget.TextView>(R.id.textView)
        tvRoomName.text = roomName

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_schedule -> loadFragment(ScheduleFragment())
                R.id.tab_money   -> loadFragment(MoneyFragment())
                R.id.tab_photo   -> loadFragment(PhotoFragment())
            }
            true
        }

    }

    // Fragment를 FrameLayout 안에 교체해주는 함수
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}