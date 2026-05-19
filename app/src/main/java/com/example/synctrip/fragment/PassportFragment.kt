package com.example.synctrip.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.RetrofitClient
import com.example.synctrip.adapter.PassportStampAdapter
import com.example.synctrip.dto.group.BandSummary
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PassportFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_passport, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvStamps = view.findViewById<RecyclerView>(R.id.rvStamps)
        val tvStampCount = view.findViewById<TextView>(R.id.tvStampCount)
        val layoutEmpty = view.findViewById<LinearLayout>(R.id.layoutEmpty)

        rvStamps.layoutManager = GridLayoutManager(requireContext(), 2)

        RetrofitClient.api.getMyBands()
            .enqueue(object : Callback<List<BandSummary>> {
                override fun onResponse(call: Call<List<BandSummary>>, response: Response<List<BandSummary>>) {
                    if (!isAdded) return
                    val completed = (response.body() ?: emptyList())
                        .filter { it.status == "DONE" }

                    tvStampCount.text = "완료한 여행: ${completed.size}개"

                    if (completed.isEmpty()) {
                        rvStamps.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
                    } else {
                        rvStamps.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE
                        rvStamps.adapter = PassportStampAdapter(completed)
                    }
                }

                override fun onFailure(call: Call<List<BandSummary>>, t: Throwable) {
                    if (!isAdded) return
                    rvStamps.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                }
            })
    }
}
