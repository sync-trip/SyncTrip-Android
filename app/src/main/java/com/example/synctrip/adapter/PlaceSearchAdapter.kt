package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.synctrip.R
import com.example.synctrip.dto.kakao.PlaceDocument
import com.google.android.material.button.MaterialButton

class PlaceSearchAdapter(
    private val places: List<PlaceDocument>,
    private val onPickClick: (PlaceDocument) -> Unit
) : RecyclerView.Adapter<PlaceSearchAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val tvName: TextView = view.findViewById(R.id.tvPlaceName)
        val tvAddress: TextView = view.findViewById(R.id.tvPlaceAddress)
        val tvCategory: TextView = view.findViewById(R.id.tvPlaceCategory)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
        val btnAdd: MaterialButton = view.findViewById(R.id.btnAddPlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_place_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place = places[position]
        holder.tvName.text = place.place_name
        holder.tvAddress.text = place.road_address_name.ifEmpty { place.address_name }
        holder.tvCategory.text = shortenCategory(place.category_name)
        holder.btnAdd.setOnClickListener { onPickClick(place) }

        if (!place.place_url.isNullOrBlank()) {
            Glide.with(holder.ivThumbnail)
                .load(place.place_url)
                .placeholder(R.drawable.bg_place_placeholder)
                .error(R.drawable.bg_place_placeholder)
                .centerCrop()
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.setImageDrawable(null)
            holder.ivThumbnail.setBackgroundResource(R.drawable.bg_place_placeholder)
        }

        val rating = place.rating
        if (rating != null && rating > 0f) {
            holder.tvRating.text = "★ %.1f".format(rating)
            holder.tvRating.visibility = View.VISIBLE
        } else {
            holder.tvRating.visibility = View.GONE
        }
    }

    override fun getItemCount() = places.size

    private fun shortenCategory(full: String): String {
        val parts = full.split(" > ")
        return if (parts.size >= 2) parts.last() else full
    }
}
