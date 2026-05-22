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

    private val borderColors = listOf(
        "#006492", "#FF9500", "#BA1A1A", "#34C759",
        "#735C00", "#006492", "#FF9500", "#BA1A1A"
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
        // destination 형식: "일본 도쿄" or "대한민국 서울"
        val parts = trip.destination.split(" ")
        val countryName = if (parts.size >= 2) parts[0] else ""
        val cityName = if (parts.size >= 2) parts.drop(1).joinToString(" ") else trip.destination

        holder.tvIcon.text = flagEmoji(countryName)
        holder.tvCity.text = cityName.take(6)
        holder.tvCountry.text = countryName
        holder.tvDate.text = trip.startDate.take(7).replace("-", ".")

        try {
            holder.stampCard.strokeColor = Color.parseColor(borderColors[position % borderColors.size])
        } catch (_: Exception) {}

        holder.container.rotation = rotations[position % rotations.size]

        holder.itemView.post {
            val w = holder.itemView.width
            if (w > 0) {
                holder.stampCard.layoutParams.height = w - 16
                holder.stampCard.requestLayout()
            }
        }
    }

    override fun getItemCount() = trips.size

    private fun flagEmoji(country: String): String = when {
        country.contains("일본") -> "🇯🇵"
        country.contains("한국") || country.contains("대한") -> "🇰🇷"
        country.contains("미국") -> "🇺🇸"
        country.contains("프랑스") -> "🇫🇷"
        country.contains("영국") -> "🇬🇧"
        country.contains("태국") -> "🇹🇭"
        country.contains("싱가포르") -> "🇸🇬"
        country.contains("인도네시아") -> "🇮🇩"
        country.contains("베트남") -> "🇻🇳"
        country.contains("필리핀") -> "🇵🇭"
        country.contains("스페인") -> "🇪🇸"
        country.contains("이탈리아") -> "🇮🇹"
        country.contains("네덜란드") -> "🇳🇱"
        country.contains("호주") -> "🇦🇺"
        country.contains("홍콩") -> "🇭🇰"
        country.contains("대만") -> "🇹🇼"
        else -> "✈️"
    }
}
