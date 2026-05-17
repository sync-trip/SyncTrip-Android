package com.example.synctrip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.adapter.ScheduleAdapter
import com.example.synctrip.dto.Schedule
import com.example.synctrip.dto.schedule.ScheduleDayResponse
import com.example.synctrip.dto.schedule.ScheduleResponse
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleFragment : Fragment() {

    private var bandId: Long = -1L

    companion object {
        fun newInstance(bandId: Long): ScheduleFragment {
            return ScheduleFragment().apply {
                arguments = Bundle().apply { putLong("BAND_ID", bandId) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bandId = arguments?.getLong("BAND_ID") ?: -1L

        val rvScheduleList = view.findViewById<RecyclerView>(R.id.rvScheduleList)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        rvScheduleList.layoutManager = LinearLayoutManager(requireContext())

        if (bandId == -1L) {
            rvScheduleList.adapter = ScheduleAdapter(emptyList())
            return
        }

        loadSchedule(rvScheduleList, tabLayout)
    }

    private fun loadSchedule(rvScheduleList: RecyclerView, tabLayout: TabLayout) {
        RetrofitClient.api.getSchedule(bandId)
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val scheduleResponse = response.body() ?: return
                        val allDays = scheduleResponse.days

                        if (allDays.isEmpty()) {
                            rvScheduleList.adapter = ScheduleAdapter(emptyList())
                            return
                        }

                        tabLayout.removeAllTabs()
                        allDays.forEach { day ->
                            tabLayout.addTab(tabLayout.newTab().setText("${day.dayNumber}일차"))
                        }

                        rvScheduleList.adapter = ScheduleAdapter(dayToSchedule(allDays[0]))

                        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab) {
                                rvScheduleList.adapter = ScheduleAdapter(dayToSchedule(allDays[tab.position]))
                            }
                            override fun onTabUnselected(tab: TabLayout.Tab) {}
                            override fun onTabReselected(tab: TabLayout.Tab) {}
                        })
                    } else {
                        // 일정이 아직 없는 경우
                        rvScheduleList.adapter = ScheduleAdapter(emptyList())
                    }
                }
                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    if (!isAdded) return
                    rvScheduleList.adapter = ScheduleAdapter(emptyList())
                }
            })
    }

    private fun dayToSchedule(day: ScheduleDayResponse): List<Schedule> {
        return day.slots.map { slot ->
            Schedule(
                time = slot.startTime?.take(5) ?: "--:--",
                placeName = slot.place.name,
                duration = slot.durationMinutes ?: 60,
                travelTime = slot.travelTimeFromPrev ?: 0
            )
        }
    }
}
