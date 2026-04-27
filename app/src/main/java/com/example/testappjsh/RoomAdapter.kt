package com.example.testappjsh

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.dto.Room

class RoomAdapter(
    private val roomList: List<Room>,  // 방 목록 데이터
    private val onItemClick: (Room) -> Unit  // 카드 클릭 시 동작
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    // 카드 하나의 뷰를 담는 그릇
    class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvRoomName: TextView = itemView.findViewById(R.id.tvRoomName)
        val tvRoomInfo: TextView = itemView.findViewById(R.id.tvRoomInfo)
    }

    // 카드 레이아웃 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_room, parent, false)
        return RoomViewHolder(view)
    }

    // 카드에 데이터 연결
    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.tvRoomName.text = room.roomName
        holder.tvRoomInfo.text = "${room.country} · ${room.city} · ${room.memberCount}명"

        // 카드 클릭 시
        holder.itemView.setOnClickListener {
            onItemClick(room)
        }
    }

    // 목록 개수
    override fun getItemCount(): Int = roomList.size
}