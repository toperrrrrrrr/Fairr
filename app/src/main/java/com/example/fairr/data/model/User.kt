package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class BlockedUser(
    val id: String = "",
    val blockedUserId: String = "",
    val blockedUserName: String = "",
    val blockedUserEmail: String = "",
    val blockedBy: String = "", // Current user's ID
    val blockedAt: Timestamp = Timestamp.now(),
    val reason: String = ""
)

data class UserReport(
    val id: String = "",
    val reportedUserId: String = "",
    val reportedUserName: String = "",
    val reportedUserEmail: String = "",
    val reportedBy: String = "", // Current user's ID
    val reportType: UserReportType,
    val reason: String = "",
    val description: String = "",
    val reportedAt: Timestamp = Timestamp.now(),
    val status: ReportStatus = ReportStatus.PENDING
)

enum class UserReportType(val displayName: String) {
    INAPPROPRIATE_BEHAVIOR("Inappropriate Behavior"),
    SPAM("Spam"),
    HARASSMENT("Harassment"),
    FAKE_ACCOUNT("Fake Account"),
    FRAUD("Fraud"),
    OTHER("Other")
}

enum class ReportStatus {
    PENDING,
    REVIEWED,
    RESOLVED,
    DISMISSED
} 