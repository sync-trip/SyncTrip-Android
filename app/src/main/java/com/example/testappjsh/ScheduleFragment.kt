package com.example.testappjsh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.dto.Schedule
import com.google.android.material.tabs.TabLayout

class ScheduleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 임시 일정 데이터 (나중에 서버에서 받아올 것)
        val day1 = listOf(
            Schedule("09:00", "경복궁", 90, 15),
            Schedule("11:00", "점심식사", 60, 20),
            Schedule("14:00", "남산타워", 120, 30, "🌙 늦은 일정"),
            Schedule("17:00", "명동쇼핑", 60, 0)
        )

        val day2 = listOf(
            Schedule("09:00", "북촌한옥마을", 90, 20),
            Schedule("11:30", "점심식사", 60, 15),
            Schedule("14:00", "인사동", 90, 0)
        )

        val day3 = listOf(
            Schedule("10:00", "한강공원", 120, 25),
            Schedule("13:00", "점심식사", 60, 20),
            Schedule("15:00", "롯데월드", 180, 0)
        )

        val allDays = listOf(day1, day2, day3)

        // RecyclerView 설정
        val rvScheduleList = view.findViewById<RecyclerView>(R.id.rvScheduleList)
        rvScheduleList.layoutManager = LinearLayoutManager(requireContext())
        rvScheduleList.adapter = ScheduleAdapter(day1) // 처음엔 1일차

        // TabLayout 설정
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        allDays.forEachIndexed { index, _ ->
            tabLayout.addTab(tabLayout.newTab().setText("${index + 1}일차"))
        }

        // 탭 클릭 시 해당 일차 일정으로 변경
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                rvScheduleList.adapter = ScheduleAdapter(allDays[tab.position])
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}