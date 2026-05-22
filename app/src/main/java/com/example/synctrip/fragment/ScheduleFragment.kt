package com.example.synctrip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.adapter.ScheduleAdapter
import com.example.synctrip.dto.Schedule
import com.example.synctrip.dto.destination.DestinationCatalog
import com.example.synctrip.dto.schedule.ScheduleDayResponse
import com.example.synctrip.dto.schedule.ScheduleResponse
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScheduleFragment : Fragment() {

    private var bandId: Long = -1L
    private var destination: String = ""
    private var startDate: String = ""
    private var endDate: String = ""
    private var bandName: String = ""

    companion object {
        fun newInstance(
            bandId: Long,
            destination: String = "",
            startDate: String = "",
            endDate: String = "",
            bandName: String = ""
        ): ScheduleFragment {
            return ScheduleFragment().apply {
                arguments = Bundle().apply {
                    putLong("BAND_ID", bandId)
                    putString("DESTINATION", destination)
                    putString("START_DATE", startDate)
                    putString("END_DATE", endDate)
                    putString("BAND_NAME", bandName)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bandId = arguments?.getLong("BAND_ID") ?: -1L
        destination = arguments?.getString("DESTINATION") ?: ""
        startDate = arguments?.getString("START_DATE") ?: ""
        endDate = arguments?.getString("END_DATE") ?: ""
        bandName = arguments?.getString("BAND_NAME") ?: ""

        setupHeader(view)

        val rvScheduleList = view.findViewById<RecyclerView>(R.id.rvScheduleList)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        rvScheduleList.layoutManager = LinearLayoutManager(requireContext())

        if (bandId == -1L) {
            rvScheduleList.adapter = ScheduleAdapter(emptyList())
            return
        }

        val tvEmpty = view.findViewById<TextView>(R.id.tvScheduleEmpty)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        swipeRefresh?.setColorSchemeResources(R.color.primary)
        swipeRefresh?.setOnRefreshListener {
            loadSchedule(rvScheduleList, tabLayout, tvEmpty) { swipeRefresh.isRefreshing = false }
        }

        loadSchedule(rvScheduleList, tabLayout, tvEmpty)
    }

    private fun setupHeader(view: View) {
        val tvTitle = view.findViewById<TextView>(R.id.tvScheduleTitle)
        val tvDates = view.findViewById<TextView>(R.id.tvScheduleHeaderDates)
        val ivHero = view.findViewById<ImageView>(R.id.ivScheduleHero)

        // 제목: 밴드명 or 목적지 도시명 기반
        val cityName = if (destination.isNotEmpty()) {
            val parts = destination.split(" ")
            if (parts.size >= 2) parts.drop(1).joinToString(" ") else destination
        } else bandName
        tvTitle.text = if (cityName.isNotEmpty()) "$cityName 여행 일정" else "여행 일정"

        // 날짜 표시
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val s = startDate.take(10).replace("-", ".")
            val e = endDate.take(10).replace("-", ".")
            tvDates.text = "$s – $e"
        }

        // 목적지 썸네일 (DestinationCatalog에서 검색)
        if (cityName.isNotEmpty()) {
            val dest = DestinationCatalog.ALL.firstOrNull { it.name == cityName }
            if (dest?.thumbnailUrl != null) {
                Glide.with(this)
                    .load(dest.thumbnailUrl)
                    .centerCrop()
                    .into(ivHero)
            }
        }
    }

    private fun loadSchedule(rvScheduleList: RecyclerView, tabLayout: TabLayout, tvEmpty: TextView, onDone: (() -> Unit)? = null) {
        RetrofitClient.api.getSchedule(bandId)
            .enqueue(object : Callback<ScheduleResponse> {
                override fun onResponse(call: Call<ScheduleResponse>, response: Response<ScheduleResponse>) {
                    if (!isAdded) return
                    onDone?.invoke()
                    if (response.isSuccessful) {
                        val scheduleResponse = response.body() ?: return
                        val allDays = scheduleResponse.days

                        if (allDays.isEmpty()) {
                            showEmpty(rvScheduleList, tvEmpty)
                            return
                        }

                        // 날짜 헤더 업데이트 (인텐트로 받은 날짜가 없을 때 API 응답으로 갱신)
                        if (startDate.isEmpty() && scheduleResponse.startDate.isNotEmpty()) {
                            view?.findViewById<TextView>(R.id.tvScheduleHeaderDates)?.let { tv ->
                                val s = scheduleResponse.startDate.take(10).replace("-", ".")
                                val e = scheduleResponse.endDate.take(10).replace("-", ".")
                                tv.text = "$s – $e"
                            }
                        }

                        tvEmpty.visibility = View.GONE
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
                        showEmpty(rvScheduleList, tvEmpty,
                            if (response.code() == 404) "아직 생성된 일정이 없어요\n투표 완료 후 방장이 일정을 생성해주세요"
                            else "일정을 불러오지 못했어요 (${response.code()})"
                        )
                    }
                }
                override fun onFailure(call: Call<ScheduleResponse>, t: Throwable) {
                    if (!isAdded) return
                    onDone?.invoke()
                    showEmpty(rvScheduleList, tvEmpty, "서버 연결 실패")
                }
            })
    }

    private fun showEmpty(rv: RecyclerView, tv: TextView, msg: String = tv.text.toString()) {
        rv.adapter = ScheduleAdapter(emptyList())
        tv.text = msg
        tv.visibility = View.VISIBLE
    }

    private fun dayToSchedule(day: ScheduleDayResponse): List<Schedule> {
        return day.slots.map { slot ->
            Schedule(
                time = slot.startTime?.take(5) ?: "--:--",
                placeName = slot.place.name,
                duration = slot.durationMinutes ?: 60,
                travelTime = slot.travelTimeFromPrev ?: 0,
                category = slot.place.category
            )
        }
    }
}
