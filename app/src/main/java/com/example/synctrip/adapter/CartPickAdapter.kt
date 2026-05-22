package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.group.PlacePickResponse

class CartPickAdapter(
    private val items: MutableList<PlacePickResponse>,
    private val onDelete: (PlacePickResponse) -> Unit
) : RecyclerView.Adapter<CartPickAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvPickName)
        val tvCategory: TextView = view.findViewById(R.id.tvPickCategory)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_cart_pick, parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pick = items[position]
        holder.tvName.text = pick.name
        holder.tvCategory.text = pick.category
        holder.btnDelete.setOnClickListener { onDelete(pick) }
    }

    override fun getItemCount() = items.size

    fun removeItem(placeId: Long) {
        val idx = items.indexOfFirst { it.placeId == placeId }
        if (idx != -1) {
            items.removeAt(idx)
            notifyItemRemoved(idx)
        }
    }
}
