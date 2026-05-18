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
import com.example.synctrip.adapter.PlaceAdapter
import com.example.synctrip.dto.band.BandReadyResponse
import com.example.synctrip.dto.band.BandStatusTransitionResponse
import com.example.synctrip.dto.group.BandInviteCodeResponse
import com.example.synctrip.dto.group.BandMemberResponse
import com.example.synctrip.dto.group.PlacePickListResponse
import com.example.synctrip.dto.group.PlacePickResponse
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
    private var myReadyState: Boolean = false

    private val pickList = mutableListOf<PlacePickResponse>()
    private lateinit var placeAdapter: PlaceAdapter

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

        view.findViewById<TextView>(R.id.tvTripTitle).text = roomName.ifEmpty { "여행" }
        if (startDate.isNotEmpty() && endDate.isNotEmpty()) {
            val s = startDate.replace("-", ". ")
            val e = endDate.replace("-", ". ")
            view.findViewById<TextView>(R.id.tvTripDate).text = "$s - $e"
        }

        // 멤버 RecyclerView
        val rvMembers = view.findViewById<RecyclerView>(R.id.rvMembers)
        rvMembers.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // 담은 장소 RecyclerView
        val rvPicks = view.findViewById<RecyclerView>(R.id.rvPicks)
        rvPicks.layoutManager = LinearLayoutManager(requireContext())
        placeAdapter = PlaceAdapter(pickList) { place, position -> deletePick(place, position) }
        rvPicks.adapter = placeAdapter

        // 초대코드
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
                        Toast.makeText(requireContext(), "코드를 불러오지 못했어요", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        view.findViewById<View>(R.id.btnCopyCode).setOnClickListener {
            val code = tvInviteCode.text.toString()
            if (code.isEmpty()) return@setOnClickListener
            val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("초대 코드", code))
            Toast.makeText(requireContext(), "초대 코드가 복사됐어요!", Toast.LENGTH_SHORT).show()
        }

        // 장소 담기 / 투표 이동
        view.findViewById<View>(R.id.layoutParticipate).setOnClickListener {
            if (currentPickCount >= maxPickCount) {
                Toast.makeText(requireContext(), "장소를 ${maxPickCount}개 모두 담았어요! 투표로 이동할게요.", Toast.LENGTH_SHORT).show()
                (activity as? SubActivity)?.switchToVoteTab()
            } else {
                val overseas = (activity as? SubActivity)?.isOverseas() ?: false
                startActivity(Intent(requireContext(), PlaceSearchActivity::class.java).apply {
                    putExtra("BAND_ID", bandId)
                    putExtra("OVERSEAS", overseas)
                })
            }
        }

        if (bandStatus != "PLANNING") applyBandStatus(view, bandStatus)

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
                        setupReadyButton(rootView, members)
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

    private fun setupReadyButton(rootView: View, members: List<BandMemberResponse>) {
        val cardReady = rootView.findViewById<MaterialCardView>(R.id.cardReady) ?: return
        val btnReady = rootView.findViewById<MaterialButton>(R.id.btnReady) ?: return

        val myUserId = TokenManager.getUserId(requireContext())
        val me = members.firstOrNull { it.userId == myUserId }
        val isHost = me?.role == "OWNER"

        if (bandStatus == "PLANNING" && !isHost) {
            cardReady.visibility = View.VISIBLE
            myReadyState = me?.isReady ?: false
            updateReadyButtonUI(btnReady, myReadyState)
            btnReady.setOnClickListener { toggleReady(btnReady) }
        } else {
            cardReady.visibility = View.GONE
        }
    }

    private fun updateReadyButtonUI(btn: MaterialButton, isReady: Boolean) {
        if (isReady) {
            btn.text = "✅ 준비 완료됨 (취소하기)"
            btn.setBackgroundColor(requireContext().getColor(R.color.surface_container_high))
            btn.setTextColor(requireContext().getColor(R.color.text_secondary))
        } else {
            btn.text = "준비 완료"
            btn.setBackgroundColor(requireContext().getColor(R.color.primary))
            btn.setTextColor(requireContext().getColor(android.R.color.white))
        }
    }

    private fun toggleReady(btn: MaterialButton) {
        if (bandId == -1L) return
        btn.isEnabled = false
        val call = if (myReadyState) RetrofitClient.api.cancelReady(bandId)
                   else RetrofitClient.api.setReady(bandId)
        call.enqueue(object : Callback<BandReadyResponse> {
            override fun onResponse(call: Call<BandReadyResponse>, response: Response<BandReadyResponse>) {
                if (!isAdded) return
                btn.isEnabled = true
                if (response.isSuccessful) {
                    myReadyState = !myReadyState
                    updateReadyButtonUI(btn, myReadyState)
                    Toast.makeText(requireContext(), if (myReadyState) "준비 완료!" else "준비를 취소했어요", Toast.LENGTH_SHORT).show()
                    // 멤버 아바타 배지 새로고침
                    val v = view ?: return
                    val rv = v.findViewById<RecyclerView>(R.id.rvMembers) ?: return
                    loadMembers(rv, v)
                } else {
                    Toast.makeText(requireContext(), "요청 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BandReadyResponse>, t: Throwable) {
                btn.isEnabled = true
                Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
            }
        })
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
        // 상태 전환 후 Ready 버튼 숨김
        view.findViewById<MaterialCardView>(R.id.cardReady)?.visibility = View.GONE
        when (status) {
            "VOTING" -> showVotingState(view)
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

                        pickList.clear()
                        pickList.addAll(data.items)
                        placeAdapter.notifyDataSetChanged()
                        view.findViewById<View>(R.id.layoutPicks).visibility =
                            if (pickList.isNotEmpty()) View.VISIBLE else View.GONE
                    } else if (response.code() == 403) {
                        if (bandStatus == "PLANNING") bandStatus = "VOTING"
                        showVotingState(view)
                    }
                }
                override fun onFailure(call: Call<PlacePickListResponse>, t: Throwable) {
                    android.util.Log.e("HomeFragment", "장소 목록 로드 실패: ${t.message}")
                }
            })
    }

    private fun deletePick(place: PlacePickResponse, position: Int) {
        if (bandId == -1L) return
        RetrofitClient.api.deletePick(bandId, place.placeId)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        placeAdapter.removeAt(position)
                        currentPickCount = maxOf(0, currentPickCount - 1)
                        val v = view ?: return
                        updateProgressUI(v, currentPickCount, maxPickCount)
                        v.findViewById<View>(R.id.layoutPicks).visibility =
                            if (pickList.isEmpty()) View.GONE else View.VISIBLE
                        Toast.makeText(requireContext(), "장소를 삭제했어요", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "삭제 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
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

        val tvCurrentStep = view.findViewById<TextView>(R.id.tvCurrentStep)
        if (current >= max) {
            tvCurrentStep.text = "투표하기"
            view.findViewById<TextView>(R.id.tvStatusBadge).text = "🗳️  투표 진행 중"
        } else {
            tvCurrentStep.text = "장소 담기"
        }
    }
}
