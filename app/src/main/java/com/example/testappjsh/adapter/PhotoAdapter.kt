package com.example.testappjsh.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.R

class PhotoAdapter(
    private val photoList: MutableList<Uri>,
    private val onItemClick: (Uri) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val uri = photoList[position]
        holder.ivPhoto.setImageURI(uri)

        holder.itemView.setOnClickListener {
            onItemClick(uri)
        }
    }

    override fun getItemCount(): Int = photoList.size

    fun addPhoto(uri: Uri) {
        photoList.add(uri)
        notifyItemInserted(photoList.size - 1)
    }
}