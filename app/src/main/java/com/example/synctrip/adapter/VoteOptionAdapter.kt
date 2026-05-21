package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.google.android.material.button.MaterialButton

data class VoteOption(
    val label: String,
    val tag1: String,
    val tag2: String,
    val tag3: String,
    val description: String
)

class VoteOptionAdapter(
    private val options: List<VoteOption>,
    private val onVote: (VoteOption) -> Unit
) : RecyclerView.Adapter<VoteOptionAdapter.VH>() {

    private var votedIndex = -1

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTag1: TextView = view.findViewById(R.id.tvTag1)
        val tvTag2: TextView = view.findViewById(R.id.tvTag2)
        val tvTag3: TextView = view.findViewById(R.id.tvTag3)
        val tvTitle: TextView = view.findViewById(R.id.tvOptionTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvOptionDesc)
        val btnVote: MaterialButton = view.findViewById(R.id.btnVote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vote_option, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val option = options[position]
        holder.tvTag1.text = option.tag1
        holder.tvTag2.text = option.tag2
        holder.tvTag3.text = option.tag3
        holder.tvTitle.text = "숙소 후보 ${option.label}"
        holder.tvDesc.text = option.description

        if (votedIndex == position) {
            holder.btnVote.text = "✓ 투표 완료"
            holder.btnVote.isEnabled = false
            holder.btnVote.alpha = 0.6f
        } else {
            holder.btnVote.text = "투표하기"
            holder.btnVote.isEnabled = votedIndex == -1
            holder.btnVote.alpha = if (votedIndex == -1) 1f else 0.4f
        }

        holder.btnVote.setOnClickListener {
            val prev = votedIndex
            votedIndex = position
            notifyItemChanged(position)
            if (prev != -1) notifyItemChanged(prev)
            onVote(option)
        }
    }

    override fun getItemCount() = options.size
}
