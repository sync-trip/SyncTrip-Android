package com.example.synctrip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.TokenManager
import com.example.synctrip.VoteStompClient
import com.example.synctrip.adapter.VotePlaceAdapter
import com.example.synctrip.dto.vote.VotePlaceResponse
import com.example.synctrip.dto.vote.VoteRequest
import com.example.synctrip.dto.vote.VoteResponse
import com.example.synctrip.dto.vote.VoteStatusResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VoteFragment : Fragment() {

    private var bandId: Long = -1L
    private val places = mutableListOf<VotePlaceResponse>()
    private val votedMap = mutableMapOf<Long, Int>()
    private lateinit var adapter: VotePlaceAdapter
    private var stompClient: VoteStompClient? = null

    companion object {
        fun newInstance(bandId: Long): VoteFragment {
            return VoteFragment().apply {
                arguments = Bundle().apply { putLong("BAND_ID", bandId) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bandId = arguments?.getLong("BAND_ID") ?: -1L

        val tvTimer = view.findViewById<TextView>(R.id.tvVoteTimer)
        val rv = view.findViewById<RecyclerView>(R.id.rvVoteOptions)

        rv.layoutManager = LinearLayoutManager(requireContext())
        adapter = VotePlaceAdapter(places, votedMap) { placeId, result ->
            submitVote(placeId, result, tvTimer)
        }
        rv.adapter = adapter

        if (bandId == -1L) {
            tvTimer.text = "밴드 정보를 불러올 수 없어요"
            return
        }

        loadVotePlaces(tvTimer)
        connectWebSocket()
    }

    private fun loadVotePlaces(tvTimer: TextView) {
        tvTimer.text = "장소 목록 불러오는 중..."

        RetrofitClient.api.getVotePlaces(bandId)
            .enqueue(object : Callback<List<VotePlaceResponse>> {
                override fun onResponse(call: Call<List<VotePlaceResponse>>, response: Response<List<VotePlaceResponse>>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        places.clear()
                        places.addAll(response.body() ?: emptyList())

                        // 내가 담은 장소 → UI 자동 좋아요 + 서버에도 투표 전송
                        places.filter { it.myBookmark }.forEach { place ->
                            votedMap[place.placeId] = 1
                            autoLike(place.placeId)
                        }

                        adapter.notifyDataSetChanged()
                        loadMyVoteStatus(tvTimer)
                    } else when (response.code()) {
                        403 -> tvTimer.text = "⏳ 아직 투표 단계가 아니에요. 방장이 '투표 시작하기'를 눌러야 해요."
                        404 -> tvTimer.text = "밴드 정보를 찾을 수 없어요."
                        else -> tvTimer.text = "장소 목록 로드 실패 (${response.code()})"
                    }
                }
                override fun onFailure(call: Call<List<VotePlaceResponse>>, t: Throwable) {
                    if (!isAdded) return
                    tvTimer.text = "서버 연결 실패"
                }
            })
    }

    private fun loadMyVoteStatus(tvTimer: TextView) {
        RetrofitClient.api.getMyVoteStatus(bandId)
            .enqueue(object : Callback<VoteStatusResponse> {
                override fun onResponse(call: Call<VoteStatusResponse>, response: Response<VoteStatusResponse>) {
                    if (!isAdded) return
                    val status = response.body() ?: return
                    updateProgressText(tvTimer, status.myVotedCount, status.totalPlaces)
                }
                override fun onFailure(call: Call<VoteStatusResponse>, t: Throwable) {}
            })
    }

    private fun submitVote(placeId: Long, result: Int, tvTimer: TextView) {
        RetrofitClient.api.vote(bandId, VoteRequest(placeId, result))
            .enqueue(object : Callback<VoteResponse> {
                override fun onResponse(call: Call<VoteResponse>, response: Response<VoteResponse>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        votedMap[placeId] = result
                        adapter.notifyVoted(placeId)
                        val voted = votedMap.size
                        val total = places.size
                        updateProgressText(tvTimer, voted, total)
                    } else {
                        Toast.makeText(requireContext(), "투표 실패 (${response.code()})", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<VoteResponse>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateProgressText(tvTimer: TextView, voted: Int, total: Int) {
        tvTimer.text = if (voted >= total && total > 0) "✅  투표 완료! ($voted/$total)" else "내 투표: $voted / $total 장소"
    }

    private fun autoLike(placeId: Long) {
        RetrofitClient.api.vote(bandId, VoteRequest(placeId, 1))
            .enqueue(object : Callback<VoteResponse> {
                override fun onResponse(call: Call<VoteResponse>, response: Response<VoteResponse>) {}
                override fun onFailure(call: Call<VoteResponse>, t: Throwable) {}
            })
    }

    private fun connectWebSocket() {
        val ctx = context ?: return
        val token = TokenManager.getToken(ctx) ?: return
        stompClient = VoteStompClient(
            token = token,
            bandId = bandId,
            onEvent = { event ->
                // UI 업데이트는 메인 스레드에서
                activity?.runOnUiThread {
                    if (!isAdded) return@runOnUiThread
                    val tvTimer = view?.findViewById<TextView>(R.id.tvVoteTimer) ?: return@runOnUiThread
                    updateProgressText(tvTimer, event.myVotedCount, event.totalPlaces)
                }
            }
        )
        stompClient?.connect()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stompClient?.disconnect()
        stompClient = null
    }
}
