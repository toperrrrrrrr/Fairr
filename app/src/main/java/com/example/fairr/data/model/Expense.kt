package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "PHP",
    val date: Timestamp = Timestamp.now(),
    val paidBy: String = "", // userId
    val paidByName: String = "", // user's name
    val splitBetween: List<ExpenseSplit> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val notes: String = "",
    val attachments: List<String> = emptyList() // URLs to attachments
)

data class ExpenseSplit(
    val userId: String,
    val userName: String,
    val share: Double,
    val isPaid: Boolean = false
)

enum class ExpenseCategory {
    FOOD,
    TRANSPORTATION,
    ACCOMMODATION,
    ENTERTAINMENT,
    SHOPPING,
    UTILITIES,
    RENT,
    OTHER
} 