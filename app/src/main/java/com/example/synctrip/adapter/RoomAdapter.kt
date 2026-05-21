package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.Room

class RoomAdapter(
    private val roomList: List<Room>,
    private val onItemClick: (Room) -> Unit,
    private val onOptionsClick: (Room, View) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    private val gradients = listOf(
        R.drawable.bg_hero_gradient,
        R.drawable.bg_gradient_green,
        R.drawable.bg_gradient_purple,
        R.drawable.bg_gradient_orange
    )

    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)
        val tvRoomDate: TextView = itemView.findViewById(R.id.tvRoomDate)
        val tvRoomEmoji: TextView = itemView.findViewById(R.id.tvRoomEmoji)
        val tvMemberBadge: TextView = itemView.findViewById(R.id.tvMemberBadge)
        val flCardBanner: FrameLayout = itemView.findViewById(R.id.flCardBanner)
        val btnViewItinerary: View = itemView.findViewById(R.id.btnViewItinerary)
        val ibRoomOptions: View = itemView.findViewById(R.id.ibRoomOptions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.tvRoomName.text = room.roomName
        holder.tvRoomInfo.text = room.city.ifEmpty { room.country }
        holder.tvRoomDate.text = formatDateRange(room.startDate, room.endDate)
        holder.tvRoomEmoji.text = destinationEmoji(room.city, room.country)
        holder.flCardBanner.setBackgroundResource(gradients[position % gradients.size])
        holder.tvMemberBadge.text = if (room.memberCount > 0) "멤버 ${room.memberCount}명" else "멤버"

        holder.btnViewItinerary.setOnClickListener { onItemClick(room) }
        holder.itemView.setOnClickListener { onItemClick(room) }
        holder.ibRoomOptions.setOnClickListener { onOptionsClick(room, it) }
    }

    override fun getItemCount(): Int = roomList.size

    private fun formatDateRange(start: String, end: String): String {
        if (start.length < 7 || end.length < 7) return ""
        val s = start.substring(5).replace("-", ".")
        val e = end.substring(5).replace("-", ".")
        return "$s – $e"
    }

    private fun destinationEmoji(city: String, country: String): String {
        val target = (city + country).lowercase()
        return when {
            target.contains("제주") -> "🌊"
            target.contains("부산") -> "🌉"
            target.contains("강릉") || target.contains("속초") -> "🏔️"
            target.contains("일본") || target.contains("도쿄") || target.contains("오사카") -> "🗾"
            target.contains("미국") || target.contains("뉴욕") -> "🗽"
            target.contains("유럽") || target.contains("파리") -> "🗼"
            target.contains("태국") || target.contains("방콕") -> "🌴"
            target.contains("베트남") || target.contains("다낭") -> "🌺"
            else -> "✈️"
        }
    }
}
