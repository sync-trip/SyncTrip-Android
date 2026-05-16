package com.example.synctrip.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.PlaceSearchActivity
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.SubActivity
import com.example.synctrip.adapter.MemberAdapter
import com.example.synctrip.dto.group.BandInviteCodeResponse
import com.example.synctrip.dto.group.BandMemberResponse
import com.example.synctrip.dto.group.PlacePickListResponse
import com.google.android.material.card.MaterialCardView
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

    private var bandId: Long = -1L
    private var maxPickCount: Int = 5
    private var currentPickCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bandId = arguments?.getLong("BAND_ID") ?: -1L
        val roomName = arguments?.getString("ROOM_NAME") ?: ""
        val startDate = arguments?.getString("START_DATE") ?: ""
        val endDate = arguments?.getString("END_DATE") ?: ""

        // Hero content
        view.findViewById<TextView>(R.id.tvTripTitle).text = roomName.ifEmpty { "여행" }
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val s = startDate.replace("-", ". ")
            val e = endDate.replace("-", ". ")
            view.findViewById<TextView>(R.id.tvTripDate).text = "$s - $e"
        }

        // Member RecyclerView
        val rvMembers = view.findViewById<RecyclerView>(R.id.rvMembers)
        rvMembers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Invite FAB
        val cardInviteCode = view.findViewById<MaterialCardView>(R.id.cardInviteCode)
        val tvInviteCode = view.findViewById<TextView>(R.id.tvInviteCode)

        view.findViewById<View>(R.id.btnInvite).setOnClickListener {
            if (cardInviteCode.visibility == View.VISIBLE) {
                cardInviteCode.visibility = View.GONE
                return@setOnClickListener
            }
            if (bandId == -1L) return@setOnClickListener
            RetrofitClient.api.getInviteCode(bandId)
                .enqueue(object : Callback<BandInviteCodeResponse> {
                    override fun onResponse(call: Call<BandInviteCodeResponse>, response: Response<BandInviteCodeResponse>) {
                        val code = response.body()?.inviteCode ?: return
                        tvInviteCode.text = code
                        cardInviteCode.visibility = View.VISIBLE
                    }
                    override fun onFailure(call: Call<BandInviteCodeResponse>, t: Throwable) {
                        android.widget.Toast.makeText(requireContext(), "코드를 불러오지 못했어요", android.widget.Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // Copy invite code
        view.findViewById<View>(R.id.btnCopyCode).setOnClickListener {
            val code = tvInviteCode.text.toString()
            if (code.isEmpty()) return@setOnClickListener
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("초대 코드", code))
            android.widget.Toast.makeText(requireContext(), "초대 코드가 복사됐어요!", android.widget.Toast.LENGTH_SHORT).show()
        }

        // "참여하기" → PlaceSearch OR vote tab depending on picks count
        view.findViewById<View>(R.id.layoutParticipate).setOnClickListener {
            if (currentPickCount >= maxPickCount) {
                (activity as? SubActivity)?.switchToVoteTab()
            } else {
                startActivity(Intent(requireContext(), PlaceSearchActivity::class.java).apply {
                    putExtra("BAND_ID", bandId)
                })
            }
        }

        loadMembers(rvMembers)
        loadMyPicks(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadMyPicks(it) }
    }

    private fun loadMembers(rvMembers: RecyclerView) {
        if (bandId == -1L) return
        RetrofitClient.api.getBandMembers(bandId)
            .enqueue(object : Callback<List<BandMemberResponse>> {
                override fun onResponse(call: Call<List<BandMemberResponse>>, response: Response<List<BandMemberResponse>>) {
                    if (response.isSuccessful) {
                        val members = response.body() ?: emptyList()
                        rvMembers.adapter = MemberAdapter(members)
                        view?.findViewById<TextView>(R.id.tvMemberCountBadge)?.text = "👥 ${members.size} 명"
                    }
                }
                override fun onFailure(call: Call<List<BandMemberResponse>>, t: Throwable) {
                    android.util.Log.e("HomeFragment", "멤버 로드 실패: ${t.message}")
                }
            })
    }

    private fun loadMyPicks(view: View) {
        if (bandId == -1L) return
        RetrofitClient.api.getPicks(bandId)
            .enqueue(object : Callback<PlacePickListResponse> {
                override fun onResponse(call: Call<PlacePickListResponse>, response: Response<PlacePickListResponse>) {
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        currentPickCount = data.currentCount
                        maxPickCount = data.maxCount
                        updateProgressUI(view, data.currentCount, data.maxCount)
                    }
                }
                override fun onFailure(call: Call<PlacePickListResponse>, t: Throwable) {
                    android.util.Log.e("HomeFragment", "장소 목록 로드 실패: ${t.message}")
                }
            })
    }

    private fun updateProgressUI(view: View, current: Int, max: Int) {
        view.findViewById<TextView>(R.id.tvProgressText).text = "$current / $max 장소"
        val progressBar = view.findViewById<ProgressBar>(R.id.progressPicks)
        progressBar.max = max
        progressBar.progress = current

        // Update current step label
        val tvCurrentStep = view.findViewById<TextView>(R.id.tvCurrentStep)
        if (current >= max) {
            tvCurrentStep.text = "투표하기"
            view.findViewById<TextView>(R.id.tvStatusBadge).text = "🗳️  투표 진행 중"
        } else {
            tvCurrentStep.text = "장소 담기"
        }
    }
}
