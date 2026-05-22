package com.example.synctrip.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.Expense
import com.google.android.material.card.MaterialCardView

class ExpenseAdapter(
    private val expenseList: List<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardCategoryIcon: MaterialCardView = itemView.findViewById(R.id.cardCategoryIcon)
        val tvCategoryIcon: TextView = itemView.findViewById(R.id.tvCategoryIcon)
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvPayer: TextView = itemView.findViewById(R.id.tvPayer)
        val tvMembers: TextView = itemView.findViewById(R.id.tvMembers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenseList[position]
        holder.tvItemName.text = expense.itemName
        holder.tvAmount.text = "${String.format("%,d", expense.amount)}원"
        holder.tvPayer.text = "결제: ${expense.payer}"
        holder.tvMembers.text = "분담: ${expense.members.joinToString(", ")}"

        val (icon, color) = categoryStyle(expense.category)
        holder.tvCategoryIcon.text = icon
        try { holder.cardCategoryIcon.setCardBackgroundColor(Color.parseColor(color)) } catch (_: Exception) {}
    }

    override fun getItemCount(): Int = expenseList.size

    private fun categoryStyle(category: String): Pair<String, String> = when (category.uppercase()) {
        "FOOD"     -> "🍽️" to "#FF9500"
        "CULTURE"  -> "🏛️" to "#006492"
        "ACTIVITY" -> "🎯" to "#34C759"
        "SHOPPING" -> "🛍️" to "#735C00"
        "NATURE"   -> "🌿" to "#2E7D32"
        "HOTEL", "ACCOMMODATION" -> "🏨" to "#735C00"
        "TRANSPORT", "AIRPORT"   -> "🚗" to "#6E7882"
        else       -> "💳" to "#006492"
    }
}