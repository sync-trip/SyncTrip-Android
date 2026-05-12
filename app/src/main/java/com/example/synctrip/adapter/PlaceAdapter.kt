package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.Place

class PlaceAdapter(
    private val placeList: MutableList<Place>
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPlaceName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        holder.tvPlaceName.text = place.name
        holder.tvCategory.text = place.category

        // 삭제 버튼
        holder.tvDelete.setOnClickListener {
            placeList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, placeList.size)
        }
    }

    override fun getItemCount(): Int = placeList.size
}