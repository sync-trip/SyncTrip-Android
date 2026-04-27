package com.example.testappjsh.dto

data class Expense(
    val itemName: String,      // 항목명
    val amount: Int,           // 금액
    val payer: String,         // 결제자
    val members: List<String>  // 분담자 목록
)