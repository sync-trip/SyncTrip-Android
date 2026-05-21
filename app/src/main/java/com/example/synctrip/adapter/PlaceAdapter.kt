package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.group.PlacePickResponse

class PlaceAdapter(
    private val placeList: MutableList<PlacePickResponse>,
    private val onDelete: (PlacePickResponse, Int) -> Unit
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
        holder.tvCategory.text = categoryLabel(place.category)

        holder.tvDelete.setOnClickListener {
            onDelete(place, position)
        }
    }

    override fun getItemCount(): Int = placeList.size

    fun removeAt(position: Int) {
        placeList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, placeList.size)
    }

    private fun categoryLabel(category: String): String = when (category) {
        "FOOD" -> "음식"
        "CULTURE" -> "문화"
        "ACTIVITY" -> "액티비티"
        "SHOPPING" -> "쇼핑"
        "NATURE" -> "자연"
        else -> "기타"
    }
}
