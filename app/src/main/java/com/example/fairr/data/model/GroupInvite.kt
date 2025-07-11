package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class GroupInvite(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val groupAvatar: String = "",
    val inviterId: String = "", // Admin who sent the invite
    val inviterName: String = "",
    val inviteeEmail: String = "", // Email of person being invited
    val inviteeId: String = "", // User ID if they have an account
    val inviteCode: String = "", // 6-digit code for easy sharing
    val message: String = "", // Optional personal message
    val status: GroupInviteStatus = GroupInviteStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp? = null, // Optional expiration
    val acceptedAt: Timestamp? = null,
    val rejectedAt: Timestamp? = null
)

enum class GroupInviteStatus(val displayName: String) {
    PENDING("Pending"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    EXPIRED("Expired"),
    CANCELLED("Cancelled")
}
