package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class FriendActivity(
    val id: String = "",
    val userId: String = "", // The user who performed the action
    val userName: String = "",
    val userInitials: String = "",
    val friendId: String = "", // The friend involved in the activity
    val friendName: String = "",
    val type: FriendActivityType,
    val title: String = "",
    val description: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val amount: Double? = null, // For settlement activities
    val groupId: String? = null, // If activity is related to a group
    val groupName: String? = null
)

enum class FriendActivityType(val displayName: String, val icon: String) {
    FRIEND_ADDED("Friend Added", "👋"),
    FRIEND_REMOVED("Friend Removed", "👎"),
    SETTLEMENT_COMPLETED("Settlement Completed", "💰"),
    EXPENSE_SHARED("Expense Shared", "🧾"),
    GROUP_JOINED_TOGETHER("Joined Group Together", "👥"),
    PAYMENT_RECEIVED("Payment Received", "💵"),
    PAYMENT_SENT("Payment Sent", "💸")
} 