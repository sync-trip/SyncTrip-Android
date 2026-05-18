package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.synctrip.R
import com.example.synctrip.dto.destination.DestinationCatalog
import com.example.synctrip.dto.destination.DestinationResponse

class PopularDestinationAdapter(
    private val items: List<DestinationResponse>,
    private val onClick: (DestinationResponse) -> Unit
) : RecyclerView.Adapter<PopularDestinationAdapter.VH>() {

    var selectedName: String? = null
        set(value) {
            val old = field
            field = value
            items.forEachIndexed { idx, d ->
                if (d.name == old || d.name == value) notifyItemChanged(idx)
            }
        }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val tvFlag: TextView = view.findViewById(R.id.tvFlag)
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
        val tvCountry: TextView = view.findViewById(R.id.tvCountry)
        val overlay: View = view.findViewById(R.id.selectionOverlay)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_popular_destination, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        holder.tvFlag.text = DestinationCatalog.flagOf(d.countryCode)
        holder.tvCityName.text = d.name
        holder.tvCountry.text = d.country
        holder.overlay.visibility = if (d.name == selectedName) View.VISIBLE else View.GONE

        if (!d.thumbnailUrl.isNullOrEmpty()) {
            Glide.with(holder.ivThumb).load(d.thumbnailUrl).centerCrop().into(holder.ivThumb)
        } else {
            holder.ivThumb.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener { onClick(d) }
    }

    override fun getItemCount(): Int = items.size
}
