package com.example.synctrip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.synctrip.R
import com.example.synctrip.dto.Place

class VoteFragment : Fragment() {

    // 임시 투표 장소 목록 (나중에 서버에서 받아올 것)
    private val placeList = mutableListOf(
        Place("경복궁", "문화"),
        Place("남산타워", "관광"),
        Place("명동", "쇼핑"),
        Place("한강공원", "자연"),
        Place("롯데월드", "액티비티")
    )

    private var currentIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_vote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvVoteCount = view.findViewById<TextView>(R.id.tvVoteCount)
        val tvPlaceName = view.findViewById<TextView>(R.id.tvVotePlaceName)
        val tvCategory = view.findViewById<TextView>(R.id.tvVoteCategory)
        val btnLike = view.findViewById<Button>(R.id.btnLike)
        val btnDislike = view.findViewById<Button>(R.id.btnDislike)

        // 첫 번째 장소 표시
        showPlace(tvVoteCount, tvPlaceName, tvCategory)

        // 좋아요 버튼
        btnLike.setOnClickListener {
            Toast.makeText(requireContext(), "👍 좋아요!", Toast.LENGTH_SHORT).show()
            nextPlace(tvVoteCount, tvPlaceName, tvCategory)
        }

        // 싫어요 버튼
        btnDislike.setOnClickListener {
            Toast.makeText(requireContext(), "❌ 싫어요!", Toast.LENGTH_SHORT).show()
            nextPlace(tvVoteCount, tvPlaceName, tvCategory)
        }
    }

    // 현재 장소 표시
    private fun showPlace(
        tvVoteCount: TextView,
        tvPlaceName: TextView,
        tvCategory: TextView
    ) {
        val place = placeList[currentIndex]
        tvPlaceName.text = place.name
        tvCategory.text = place.category
        tvVoteCount.text = "남은 장소: ${placeList.size - currentIndex}개"
    }

    // 다음 장소로 이동
    private fun nextPlace(
        tvVoteCount: TextView,
        tvPlaceName: TextView,
        tvCategory: TextView
    ) {
        currentIndex++
        if (currentIndex < placeList.size) {
            showPlace(tvVoteCount, tvPlaceName, tvCategory)
        } else {
            // 모든 장소 투표 완료
            tvPlaceName.text = "투표 완료!"
            tvCategory.text = ""
            tvVoteCount.text = "남은 장소: 0개"
            Toast.makeText(requireContext(), "🎉 모든 장소 투표 완료!", Toast.LENGTH_SHORT).show()
        }
    }
}