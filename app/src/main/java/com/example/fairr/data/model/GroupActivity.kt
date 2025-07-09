package com.example.fairr.data.model

data class GroupActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val amount: Double? = null,
    val userName: String,
    val userInitials: String,
    val timestamp: Long,
    val isPositive: Boolean = true,
    val expenseId: String? = null
) 