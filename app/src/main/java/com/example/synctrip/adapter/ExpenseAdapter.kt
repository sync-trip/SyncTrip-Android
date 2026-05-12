package com.example.synctrip.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.synctrip.R
import com.example.synctrip.dto.Expense

class ExpenseAdapter(
    private val expenseList: List<Expense>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        holder.tvAmount.text = "${expense.amount}원"
        holder.tvPayer.text = "결제: ${expense.payer}"
        holder.tvMembers.text = "분담: ${expense.members.joinToString(", ")}"
    }

    override fun getItemCount(): Int = expenseList.size
}