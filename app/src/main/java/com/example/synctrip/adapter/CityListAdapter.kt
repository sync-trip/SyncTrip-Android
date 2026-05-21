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
import com.google.android.material.button.MaterialButton

class CityListAdapter(
    private val items: MutableList<DestinationResponse>,
    private val onSelect: (DestinationResponse) -> Unit
) : RecyclerView.Adapter<CityListAdapter.VH>() {

    var selectedName: String? = null
        set(value) {
            val old = field
            field = value
            items.forEachIndexed { idx, d ->
                if (d.name == old || d.name == value) notifyItemChanged(idx)
            }
        }

    fun submit(list: List<DestinationResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val tvFlag: TextView = view.findViewById(R.id.tvFlag)
        val tvFlagFallback: TextView = view.findViewById(R.id.tvFlagFallback)
        val tvCityName: TextView = view.findViewById(R.id.tvCityName)
        val tvCountry: TextView = view.findViewById(R.id.tvCountry)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val btnSelect: MaterialButton = view.findViewById(R.id.btnSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city_row, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = items[position]
        val flag = DestinationCatalog.flagOf(d.countryCode)
        holder.tvFlag.text = flag
        holder.tvCityName.text = d.name
        holder.tvCountry.text = d.country
        holder.tvDescription.text = d.description ?: ""

        if (!d.thumbnailUrl.isNullOrEmpty()) {
            holder.tvFlagFallback.visibility = View.GONE
            Glide.with(holder.ivThumb).load(d.thumbnailUrl).centerCrop().into(holder.ivThumb)
        } else {
            holder.ivThumb.setImageDrawable(null)
            holder.tvFlagFallback.visibility = View.VISIBLE
            holder.tvFlagFallback.text = flag
        }

        val selected = d.name == selectedName
        holder.btnSelect.text = if (selected) "선택됨" else "선택"

        val click = View.OnClickListener { onSelect(d) }
        holder.itemView.setOnClickListener(click)
        holder.btnSelect.setOnClickListener(click)
    }

    override fun getItemCount(): Int = items.size
}
