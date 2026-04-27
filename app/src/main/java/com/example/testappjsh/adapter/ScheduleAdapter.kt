package com.example.testappjsh.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.R
import com.example.testappjsh.dto.Schedule

class ScheduleAdapter(
    private val scheduleList: List<Schedule>
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvTravelTime: TextView = itemView.findViewById(R.id.tvTravelTime)
        val tvWarning: TextView = itemView.findViewById(R.id.tvWarning)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = scheduleList[position]

        holder.tvTime.text = schedule.time
        holder.tvPlaceName.text = "📍 ${schedule.placeName}"
        holder.tvDuration.text = "체류 ${schedule.duration}분"
        holder.tvTravelTime.text = "↓ 이동 ${schedule.travelTime}분"

        // 경고 배지 표시
        if (schedule.warning.isNotEmpty()) {
            holder.tvWarning.text = schedule.warning
            holder.tvWarning.visibility = View.VISIBLE
        } else {
            holder.tvWarning.visibility = View.GONE
        }

        // 마지막 장소는 이동시간 숨기기
        if (position == scheduleList.size - 1) {
            holder.tvTravelTime.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = scheduleList.size
}