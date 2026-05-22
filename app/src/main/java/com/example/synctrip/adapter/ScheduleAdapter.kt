package com.example.synctrip.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.Schedule
import com.google.android.material.card.MaterialCardView

class ScheduleAdapter(
    private val scheduleList: List<Schedule>
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvTravelTime: TextView = itemView.findViewById(R.id.tvTravelTime)
        val tvWarning: TextView = itemView.findViewById(R.id.tvWarning)
        val tvCategoryIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        val cardIcon: MaterialCardView = itemView.findViewById(R.id.cardIcon)
        val viewTimeLine: View = itemView.findViewById(R.id.viewTimeLine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]

        holder.tvTime.text = schedule.time
        holder.tvPlaceName.text = schedule.placeName
        holder.tvDuration.text = "체류 ${schedule.duration}분"
        holder.tvTravelTime.text = "↓ 이동 ${schedule.travelTime}분"

        val (icon, color) = categoryStyle(schedule.category)
        holder.tvCategoryIcon.text = icon
        try { holder.cardIcon.setCardBackgroundColor(Color.parseColor(color)) } catch (_: Exception) {}

        if (schedule.warning.isNotEmpty()) {
            holder.tvWarning.text = schedule.warning
            holder.tvWarning.visibility = View.VISIBLE
        } else {
            holder.tvWarning.visibility = View.GONE
        }

        // 마지막 아이템 이동시간 + 연결선 숨기기
        if (position == scheduleList.size - 1) {
            holder.tvTravelTime.visibility = View.GONE
            holder.viewTimeLine.visibility = View.GONE
        } else {
            holder.tvTravelTime.visibility = View.VISIBLE
            holder.viewTimeLine.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = scheduleList.size

    private fun categoryStyle(category: String): Pair<String, String> = when (category.uppercase()) {
        "FOOD"     -> "🍽️" to "#FF9500"
        "CULTURE"  -> "🏛️" to "#006492"
        "ACTIVITY" -> "🎯" to "#34C759"
        "SHOPPING" -> "🛍️" to "#735C00"
        "NATURE"   -> "🌿" to "#2E7D32"
        "HOTEL", "ACCOMMODATION" -> "🏨" to "#735C00"
        "TRANSPORT", "AIRPORT"   -> "✈️" to "#006492"
        else       -> "📍" to "#006492"
    }
}
