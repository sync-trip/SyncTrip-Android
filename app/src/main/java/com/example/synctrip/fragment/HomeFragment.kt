package com.example.synctrip.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.PlaceSearchActivity
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.SubActivity
import com.example.synctrip.TokenManager
import com.example.synctrip.adapter.MemberAdapter
import com.example.synctrip.dto.band.BandStatusTransitionResponse
import com.example.synctrip.dto.group.BandInviteCodeResponse
import com.example.synctrip.dto.group.BandMemberResponse
import com.example.synctrip.dto.group.PlacePickListResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    companion object {
        fun newInstance(bandId: Long, roomName: String, inviteCode: String, startDate: String, endDate: String, bandStatus: String = "PLANNING"): HomeFragment {
            return HomeFragment().apply {
                arguments = Bundle().apply {
                    putLong("BAND_ID", bandId)
                    putString("ROOM_NAME", roomName)
                    putString("INVITE_CODE", inviteCode)
                    putString("START_DATE", startDate)
                    putString("END_DATE", endDate)
                    putString("BAND_STATUS", bandStatus)
                }
            }
        }
    }

    private var bandId: Long = -1L
    private var bandStatus: String = "PLANNING"
    private var maxPickCount: Int = 5
    private var currentPickCount: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bandId = arguments?.getLong("BAND_ID") ?: -1L
        bandStatus = arguments?.getString("BAND_STATUS") ?: "PLANNING"
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

        // 클릭 리스너 설정 후 밴드 상태 반영 (순서 중요: showVotingState가 layoutParticipate 리스너를 덮어씀)
        if (bandStatus != "PLANNING") {
            applyBandStatus(view, bandStatus)
        }

        loadMembers(rvMembers, view)
        loadMyPicks(view)
    }

    override fun onResume() {
        super.onResume()
        val v = view ?: return
        loadMyPicks(v)
        val rv = v.findViewById<RecyclerView>(R.id.rvMembers) ?: return
        loadMembers(rv, v)
    }

    private fun loadMembers(rvMembers: RecyclerView, rootView: View) {
        if (bandId == -1L) return
        RetrofitClient.api.getBandMembers(bandId)
            .enqueue(object : Callback<List<BandMemberResponse>> {
                override fun onResponse(call: Call<List<BandMemberResponse>>, response: Response<List<BandMemberResponse>>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val members = response.body() ?: emptyList()
                        rvMembers.adapter = MemberAdapter(members)
                        rootView.findViewById<TextView>(R.id.tvMemberCountBadge)?.text = "👥 ${members.size} 명"
                        showHostActionIfNeeded(rootView, members)
                    }
                }
                override fun onFailure(call: Call<List<BandMemberResponse>>, t: Throwable) {
                    android.util.Log.e("HomeFragment", "멤버 로드 실패: ${t.message}")
                }
            })
    }

    private fun showHostActionIfNeeded(rootView: View, members: List<BandMemberResponse>) {
        val myUserId = TokenManager.getUserId(requireContext())
val isHost = members.any { it.role == "OWNER" && it.userId == myUserId }
        val cardHostAction = rootView.findViewById<MaterialCardView>(R.id.cardHostAction) ?: return
        if (isHost) {
            cardHostAction.visibility = View.VISIBLE
            rootView.findViewById<MaterialButton>(R.id.btnAdvanceStatus)?.setOnClickListener {
                advanceBandStatus()
            }
        } else {
            cardHostAction.visibility = View.GONE
        }
    }

    private fun advanceBandStatus() {
        if (bandId == -1L) return
        val btn = view?.findViewById<MaterialButton>(R.id.btnAdvanceStatus) ?: return
        btn.isEnabled = false
        btn.text = "처리 중..."

        RetrofitClient.api.advanceBandStatus(bandId)
            .enqueue(object : Callback<BandStatusTransitionResponse> {
                override fun onResponse(call: Call<BandStatusTransitionResponse>, response: Response<BandStatusTransitionResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val next = response.body()?.currentStatus ?: ""
                        bandStatus = next
                        val v = view ?: return
                        applyBandStatus(v, next)
                        val toastMsg = when (next) {
                            "VOTING"     -> "투표가 시작됐어요!"
                            "GENERATING" -> "일정 생성 중이에요!"
                            "TRAVELLING" -> "여행 시작!"
                            "DONE"       -> "여행이 끝났어요!"
                            else         -> "상태가 변경됐어요"
                        }
                        Toast.makeText(requireContext(), toastMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "상태 전환 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                        btn.isEnabled = true
                        btn.text = "투표 시작하기 →"
                    }
                }
                override fun onFailure(call: Call<BandStatusTransitionResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                    btn.isEnabled = true
                    btn.text = "투표 시작하기 →"
                }
            })
    }

    private fun applyBandStatus(view: View, status: String) {
        val btnAdvance = view.findViewById<MaterialButton>(R.id.btnAdvanceStatus)
        val tvBadge = view.findViewById<TextView>(R.id.tvStatusBadge)
        when (status) {
            "VOTING" -> {
                showVotingState(view)
            }
            "GENERATING" -> {
                tvBadge?.text = "⚙️  일정 생성 중"
                btnAdvance?.apply { isEnabled = false; text = "⚙️  일정 생성 중" }
                view.findViewById<TextView>(R.id.tvProgressText).text = "장소 담기 완료"
                view.findViewById<TextView>(R.id.tvCurrentStep).text = "일정 생성 중"
            }
            "TRAVELLING" -> {
                tvBadge?.text = "✈️  여행 중"
                btnAdvance?.apply { isEnabled = false; text = "✈️  여행 중" }
                view.findViewById<TextView>(R.id.tvCurrentStep).text = "여행 중"
            }
            "DONE" -> {
                tvBadge?.text = "✅  완료"
                btnAdvance?.apply { isEnabled = false; text = "✅  완료" }
                view.findViewById<TextView>(R.id.tvCurrentStep).text = "완료"
            }
        }
    }

    private fun loadMyPicks(view: View) {
        if (bandId == -1L) return
        RetrofitClient.api.getPicks(bandId)
            .enqueue(object : Callback<PlacePickListResponse> {
                override fun onResponse(call: Call<PlacePickListResponse>, response: Response<PlacePickListResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val data = response.body() ?: return
                        currentPickCount = data.currentCount
                        maxPickCount = data.maxCount
                        updateProgressUI(view, data.currentCount, data.maxCount)
                    } else if (response.code() == 403) {
                        // 투표 단계 이후 → 장바구니 잠김
                        if (bandStatus == "PLANNING") bandStatus = "VOTING"
                        showVotingState(view)
                    }
                }
                override fun onFailure(call: Call<PlacePickListResponse>, t: Throwable) {
                    android.util.Log.e("HomeFragment", "장소 목록 로드 실패: ${t.message}")
                }
            })
    }

    private fun showVotingState(view: View) {
        view.findViewById<TextView>(R.id.tvProgressText).text = "장소 담기 완료"
        view.findViewById<ProgressBar>(R.id.progressPicks).progress = view.findViewById<ProgressBar>(R.id.progressPicks).max
        view.findViewById<TextView>(R.id.tvCurrentStep).text = "투표하기"
        view.findViewById<TextView>(R.id.tvStatusBadge).text = "🗳️  투표 진행 중"
        view.findViewById<View>(R.id.layoutParticipate).setOnClickListener {
            (activity as? SubActivity)?.switchToVoteTab()
        }
        // 방장 버튼도 현재 상태에 맞게 업데이트
        view.findViewById<MaterialButton>(R.id.btnAdvanceStatus)?.apply {
            isEnabled = true
            text = "일정 생성하기 →"
        }
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
