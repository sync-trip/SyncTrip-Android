package com.example.synctrip.dto

data class Expense(
    val itemName: String,
    val amount: Int,
    val payer: String,
    val members: List<String>,
    val category: String = "FOOD"
)