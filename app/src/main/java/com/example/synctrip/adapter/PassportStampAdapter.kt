package com.example.synctrip.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.group.BandSummary
import com.google.android.material.card.MaterialCardView

class PassportStampAdapter(private val trips: List<BandSummary>) :
    RecyclerView.Adapter<PassportStampAdapter.ViewHolder>() {

    private val icons = listOf("✈️", "🚆", "⛵", "🏔️", "☕", "🚗", "🌊", "🗺️", "🎡", "🌺")
    private val borderColors = listOf(
        "#004B6F", "#FF9500", "#BA1A1A", "#34C759", "#735C00", "#004B6F", "#FF9500", "#BA1A1A"
    )
    private val rotations = listOf(-5f, 8f, -12f, 3f, -7f, 10f, -4f, 6f)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stampCard: MaterialCardView = view.findViewById(R.id.stampCard)
        val tvIcon: TextView = view.findViewById(R.id.tvStampIcon)
        val tvCity: TextView = view.findViewById(R.id.tvStampCity)
        val tvCountry: TextView = view.findViewById(R.id.tvStampCountry)
        val tvDate: TextView = view.findViewById(R.id.tvStampDate)
        val container: FrameLayout = view as FrameLayout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_passport_stamp, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trip = trips[position]
        val parts = trip.destination.split(" ")
        val city = if (parts.size >= 2) parts.drop(1).joinToString(" ") else trip.destination
        val country = if (parts.size >= 2) parts[0] else ""

        holder.tvIcon.text = icons[position % icons.size]
        holder.tvCity.text = city.take(6)
        holder.tvCountry.text = country
        holder.tvDate.text = trip.startDate.take(7).replace("-", ".")

        try {
            holder.stampCard.strokeColor = Color.parseColor(borderColors[position % borderColors.size])
        } catch (_: Exception) {}

        holder.container.rotation = rotations[position % rotations.size]

        // 카드 높이 = 카드 너비 (정사각형 → 원형)
        holder.itemView.post {
            val w = holder.itemView.width
            if (w > 0) {
                holder.stampCard.layoutParams.height = w - 16
                holder.stampCard.requestLayout()
            }
        }
    }

    override fun getItemCount() = trips.size
}
