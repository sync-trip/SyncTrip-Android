package com.example.testappjsh.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testappjsh.adapter.ExpenseAdapter
import com.example.testappjsh.R
import com.example.testappjsh.dto.Expense

class MoneyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_money, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 임시 지출 데이터 (나중에 서버에서 받아올 것)
        val expenseList = mutableListOf(
            Expense("점심식사", 45000, "김철수", listOf("김철수", "이영희", "박민수")),
            Expense("택시비", 12000, "이영희", listOf("김철수", "이영희")),
            Expense("저녁식사", 60000, "박민수", listOf("김철수", "이영희", "박민수"))
        )

        // RecyclerView 연결
        val rvExpenseList = view.findViewById<RecyclerView>(R.id.rvExpenseList)
        rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        rvExpenseList.adapter = ExpenseAdapter(expenseList)
    }
}