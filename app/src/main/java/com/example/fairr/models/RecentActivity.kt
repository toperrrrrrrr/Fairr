package com.example.fairr.models

import androidx.compose.ui.graphics.vector.ImageVector

enum class ActivityType {
    EXPENSE, PAYMENT, GROUP
}

data class RecentActivity(
    val title: String,
    val amount: String,
    val timestamp: String,
    val icon: ImageVector,
    val type: ActivityType,
    val subtitle: String = "",
    val groupId: String = ""
) 