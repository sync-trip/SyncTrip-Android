package com.example.synctrip.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.PlaceSearchActivity
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.adapter.PlaceAdapter
import com.example.synctrip.dto.Place
import com.example.synctrip.dto.group.BandInviteCodeResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    companion object {
        fun newInstance(bandId: Long, roomName: String, inviteCode: String, startDate: String, endDate: String): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putLong("BAND_ID", bandId)
                    putString("ROOM_NAME", roomName)
                    putString("INVITE_CODE", inviteCode)
                    putString("START_DATE", startDate)
                    putString("END_DATE", endDate)
                }
            }
        }
    }

    enum class GroupStatus {
        PLANNING, VOTING, GENERATING, TRAVELLING, DONE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bandId = arguments?.getLong("BAND_ID") ?: -1L
        val roomName = arguments?.getString("ROOM_NAME") ?: ""
        val inviteCode = arguments?.getString("INVITE_CODE") ?: ""
        val startDate = arguments?.getString("START_DATE") ?: ""
        val endDate = arguments?.getString("END_DATE") ?: ""

        view.findViewById<TextView>(R.id.tvTripTitle).text = roomName.ifEmpty { "여행" }
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            view.findViewById<TextView>(R.id.tvTripDate).text = "$startDate ~ $endDate"
        }
        view.findViewById<TextView>(R.id.tvMemberCount).text = "멤버 1명"

        val tvInviteCode = view.findViewById<TextView>(R.id.tvInviteCode)
        val layoutInviteCode = view.findViewById<LinearLayout>(R.id.layoutInviteCode)

        val myPlaces = mutableListOf<Place>()
        val currentStatus = GroupStatus.PLANNING

        val tvMyPlaceTitle = view.findViewById<TextView>(R.id.tvMyPlaceTitle)
        tvMyPlaceTitle.text = "📍 내가 담은 장소 (${myPlaces.size}/5)"

        val rvMyPlaces = view.findViewById<RecyclerView>(R.id.rvMyPlaces)
        rvMyPlaces.layoutManager = LinearLayoutManager(requireContext())
        rvMyPlaces.adapter = PlaceAdapter(myPlaces)

        // 초대하기 버튼 - 누르면 서버에서 코드 받아와서 표시
        val btnInvite = view.findViewById<Button>(R.id.btnInvite)
        btnInvite.setOnClickListener {
            if (bandId == -1L) return@setOnClickListener
            btnInvite.isEnabled = false
            RetrofitClient.api.getInviteCode(bandId)
                .enqueue(object : Callback<BandInviteCodeResponse> {
                    override fun onResponse(call: Call<BandInviteCodeResponse>, response: Response<BandInviteCodeResponse>) {
                        btnInvite.isEnabled = true
                        val code = response.body()?.inviteCode ?: return
                        tvInviteCode.text = code
                        layoutInviteCode.visibility = View.VISIBLE
                    }
                    override fun onFailure(call: Call<BandInviteCodeResponse>, t: Throwable) {
                        btnInvite.isEnabled = true
                        android.widget.Toast.makeText(requireContext(), "코드를 불러오지 못했어요", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 장소 담기 버튼
        val btnAddPlace = view.findViewById<Button>(R.id.btnAddPlace)
        btnAddPlace.setOnClickListener {
            val intent = Intent(requireContext(), PlaceSearchActivity::class.java)
            startActivity(intent)
        }

        // 투표 시작 버튼
        val btnStartVote = view.findViewById<Button>(R.id.btnStartVote)
        btnStartVote.setOnClickListener {
            android.widget.Toast.makeText(
                requireContext(),
                "투표 시작 준비 중!",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }

        // 복사 버튼 - 이미 표시된 코드를 클립보드에 복사
        val btnCopyCode = view.findViewById<Button>(R.id.btnCopyCode)
        btnCopyCode.setOnClickListener {
            val code = tvInviteCode.text.toString()
            if (code.isEmpty()) return@setOnClickListener
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("초대 코드", code))
            android.widget.Toast.makeText(requireContext(), "초대 코드가 복사됐어요!", android.widget.Toast.LENGTH_SHORT).show()
        }

        val tvGroupStatus = view.findViewById<TextView>(R.id.tvGroupStatus)
        val btnGenerateSchedule = view.findViewById<Button>(R.id.btnGenerateSchedule)

        updateStatusUI(
            currentStatus,
            tvGroupStatus,
            btnAddPlace,
            btnStartVote,
            btnGenerateSchedule
        )
    }

    private fun updateStatusUI(
        status: GroupStatus,
        tvGroupStatus: TextView,
        btnAddPlace: Button,
        btnStartVote: Button,
        btnGenerateSchedule: Button
    ) {
        when (status) {
            GroupStatus.PLANNING -> {
                tvGroupStatus.text = "📋 현재 상태: 장소 담기 중"
                btnAddPlace.isEnabled = true
                btnAddPlace.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
                btnStartVote.isEnabled = true
                btnStartVote.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF2196F3.toInt())
                btnGenerateSchedule.isEnabled = false
                btnGenerateSchedule.text = "✨ 일정 생성하기 (투표 후 활성화)"
                btnGenerateSchedule.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
            }
            GroupStatus.VOTING -> {
                tvGroupStatus.text = "🗳️ 현재 상태: 투표 진행 중"
                btnAddPlace.isEnabled = false
                btnAddPlace.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnStartVote.isEnabled = false
                btnStartVote.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnGenerateSchedule.isEnabled = false
                btnGenerateSchedule.text = "✨ 일정 생성하기 (투표 완료 후 활성화)"
                btnGenerateSchedule.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
            }
            GroupStatus.GENERATING -> {
                tvGroupStatus.text = "⏳ 현재 상태: 일정 생성 중..."
                btnAddPlace.isEnabled = false
                btnAddPlace.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnStartVote.isEnabled = false
                btnStartVote.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnGenerateSchedule.isEnabled = false
                btnGenerateSchedule.text = "⏳ 일정 생성 중..."
                btnGenerateSchedule.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFFFF9800.toInt())
            }
            GroupStatus.TRAVELLING -> {
                tvGroupStatus.text = "✈️ 현재 상태: 여행 중"
                btnAddPlace.isEnabled = false
                btnAddPlace.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnStartVote.isEnabled = false
                btnStartVote.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnGenerateSchedule.isEnabled = true
                btnGenerateSchedule.text = "📅 일정 보기"
                btnGenerateSchedule.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50.toInt())
                btnGenerateSchedule.setOnClickListener {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "일정 화면으로 이동!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
            GroupStatus.DONE -> {
                tvGroupStatus.text = "🎉 현재 상태: 여행 완료"
                btnAddPlace.isEnabled = false
                btnAddPlace.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnStartVote.isEnabled = false
                btnStartVote.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
                btnGenerateSchedule.isEnabled = false
                btnGenerateSchedule.text = "🎉 여행 완료"
                btnGenerateSchedule.backgroundTintList =
                    android.content.res.ColorStateList.valueOf(0xFF9E9E9E.toInt())
            }
        }
    }
}