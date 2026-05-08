package com.example.testappjsh.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.R
import com.example.testappjsh.dto.kakao.PlaceDocument

class PlaceSearchAdapter(
    private val places: List<PlaceDocument>,
    private val onItemClick: (PlaceDocument) -> Unit
) : RecyclerView.Adapter<PlaceSearchAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvSearchPlaceName)
        val tvAddress: TextView = itemView.findViewById(R.id.tvSearchPlaceAddress)
        val tvCategory: TextView = itemView.findViewById(R.id.tvSearchPlaceCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = places[position]
        holder.tvName.text = place.place_name
        holder.tvAddress.text = place.road_address_name.ifEmpty { place.address_name }
        holder.tvCategory.text = place.category_name
        holder.itemView.setOnClickListener { onItemClick(place) }
    }

    override fun getItemCount(): Int = places.size
}