package com.example.synctrip.fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.adapter.VoteOption
import com.example.synctrip.adapter.VoteOptionAdapter

class VoteFragment : Fragment() {

    private var countDownTimer: CountDownTimer? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTimer = view.findViewById<TextView>(R.id.tvVoteTimer)
        val rv = view.findViewById<RecyclerView>(R.id.rvVoteOptions)

        val options = listOf(
            VoteOption("A", "📍 서울 강남구", "💰 1박 ₩85,000", "⭐ 4.7", "깔끔한 시설과 좋은 접근성을 가진 숙소입니다."),
            VoteOption("B", "📍 서울 마포구", "💰 1박 ₩72,000", "⭐ 4.5", "합리적인 가격에 아늑한 분위기의 숙소입니다."),
            VoteOption("C", "📍 서울 용산구", "💰 1박 ₩95,000", "⭐ 4.8", "프리미엄 시설과 탁월한 뷰를 자랑하는 숙소입니다.")
        )

        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = VoteOptionAdapter(options) { option ->
            Toast.makeText(requireContext(), "후보 ${option.label}에 투표했어요!", Toast.LENGTH_SHORT).show()
        }

        startTimer(tvTimer, 165_000L)
    }

    private fun startTimer(tvTimer: TextView, millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(remaining: Long) {
                val minutes = remaining / 60000
                val seconds = (remaining % 60000) / 1000
                tvTimer.text = "투표 종료까지 %02d:%02d".format(minutes, seconds)
            }
            override fun onFinish() {
                tvTimer.text = "투표가 종료되었습니다"
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        countDownTimer?.cancel()
    }
}
