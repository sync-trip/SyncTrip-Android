package com.example.synctrip.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.group.BandMemberResponse

class MemberAdapter(
    private val members: List<BandMemberResponse>
) : RecyclerView.Adapter<MemberAdapter.VH>() {

    private val avatarColors = listOf(
        Color.parseColor("#004B6F"),
        Color.parseColor("#006492"),
        Color.parseColor("#11998E"),
        Color.parseColor("#7F00FF"),
        Color.parseColor("#F7971E"),
    )

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val vBorder: View = view.findViewById(R.id.vAvatarBorder)
        val flAvatar: FrameLayout = view.findViewById(R.id.flAvatar)
        val tvInitial: TextView = view.findViewById(R.id.tvInitial)
        val layoutBadge: FrameLayout = view.findViewById(R.id.layoutBadge)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
        val tvName: TextView = view.findViewById(R.id.tvMemberName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_member_avatar, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val member = members[position]

        // Avatar background color
        val avatarDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(avatarColors[position % avatarColors.size])
        }
        holder.flAvatar.background = avatarDrawable

        // Border color: primary if host/ready, outline_variant if not ready
        val isHost = member.role == "HOST"
        val borderColor = if (isHost || member.isReady)
            Color.parseColor("#004B6F")
        else
            Color.parseColor("#C0C7D0")
        val borderDrawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(borderColor)
        }
        holder.vBorder.background = borderDrawable

        // Initial
        holder.tvInitial.text = member.name.take(1).uppercase()

        // Not ready → dimmed
        holder.itemView.alpha = if (member.isReady || isHost) 1f else 0.65f

        // Badge
        if (isHost) {
            holder.layoutBadge.visibility = View.VISIBLE
            holder.tvBadge.text = "⭐"
        } else if (member.isReady) {
            holder.layoutBadge.visibility = View.VISIBLE
            holder.tvBadge.text = "✅"
        } else {
            holder.layoutBadge.visibility = View.GONE
        }

        holder.tvName.text = if (isHost) "${member.name} (방장)" else member.name
    }

    override fun getItemCount() = members.size
}
