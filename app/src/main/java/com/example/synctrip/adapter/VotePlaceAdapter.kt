package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.synctrip.R
import com.example.synctrip.dto.vote.VotePlaceResponse
import com.google.android.material.button.MaterialButton

class VotePlaceAdapter(
    private val places: List<VotePlaceResponse>,
    private val votedMap: MutableMap<Long, Int>,   // placeId → result(1 or -1)
    private val onVote: (placeId: Long, result: Int) -> Unit
) : RecyclerView.Adapter<VotePlaceAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivVoteThumbnail)
        val tvName: TextView = view.findViewById(R.id.tvVotePlaceName)
        val tvCategory: TextView = view.findViewById(R.id.tvVoteCategory)
        val tvRating: TextView = view.findViewById(R.id.tvVoteRating)
        val tvAddress: TextView = view.findViewById(R.id.tvVoteAddress)
        val tvMyBookmark: TextView = view.findViewById(R.id.tvMyBookmarkBadge)
        val btnLike: MaterialButton = view.findViewById(R.id.btnLike)
        val btnDislike: MaterialButton = view.findViewById(R.id.btnDislike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_vote_place, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val place = places[position]
        val voted = votedMap[place.placeId]

        holder.tvName.text = place.name
        holder.tvCategory.text = categoryLabel(place.category)
        holder.tvAddress.text = "📍 ${place.address}"
        holder.tvMyBookmark.visibility = if (place.myBookmark) View.VISIBLE else View.GONE

        if (place.rating != null) {
            holder.tvRating.text = "⭐ ${"%.1f".format(place.rating)}"
            holder.tvRating.visibility = View.VISIBLE
        } else {
            holder.tvRating.visibility = View.GONE
        }

        if (!place.thumbnailUrl.isNullOrBlank()) {
            holder.ivThumbnail.visibility = View.VISIBLE
            Glide.with(holder.ivThumbnail)
                .load(place.thumbnailUrl)
                .placeholder(R.drawable.bg_place_placeholder)
                .error(R.drawable.bg_place_placeholder)
                .centerCrop()
                .into(holder.ivThumbnail)
        } else {
            holder.ivThumbnail.visibility = View.GONE
        }

        applyVoteState(holder, voted)

        holder.btnLike.setOnClickListener {
            if (votedMap[place.placeId] == 1) return@setOnClickListener
            onVote(place.placeId, 1)
        }
        holder.btnDislike.setOnClickListener {
            if (votedMap[place.placeId] == -1) return@setOnClickListener
            onVote(place.placeId, -1)
        }
    }

    private fun applyVoteState(holder: VH, voted: Int?) {
        val ctx = holder.itemView.context
        when (voted) {
            1 -> {
                holder.btnLike.text = "✓ 좋아요"
                holder.btnLike.isEnabled = false
                holder.btnDislike.isEnabled = false
                holder.btnDislike.alpha = 0.4f
            }
            -1 -> {
                holder.btnDislike.text = "✓ 별로에요"
                holder.btnDislike.isEnabled = false
                holder.btnLike.isEnabled = false
                holder.btnLike.alpha = 0.4f
            }
            else -> {
                holder.btnLike.text = "👍  좋아요"
                holder.btnLike.isEnabled = true
                holder.btnLike.alpha = 1f
                holder.btnDislike.text = "👎  별로에요"
                holder.btnDislike.isEnabled = true
                holder.btnDislike.alpha = 1f
            }
        }
    }

    fun notifyVoted(placeId: Long) {
        val idx = places.indexOfFirst { it.placeId == placeId }
        if (idx != -1) notifyItemChanged(idx)
    }

    override fun getItemCount() = places.size

    private fun categoryLabel(category: String) = when (category) {
        "FOOD"     -> "🍽️  맛집"
        "CULTURE"  -> "🏛️  문화"
        "ACTIVITY" -> "🎯  액티비티"
        "SHOPPING" -> "🛍️  쇼핑"
        "NATURE"   -> "🌿  자연"
        else       -> "📌  기타"
    }
}
