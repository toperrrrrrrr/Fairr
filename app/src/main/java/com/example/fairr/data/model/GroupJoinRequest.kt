package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class GroupJoinRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val groupCreatorId: String = "",
    val status: JoinRequestStatus = JoinRequestStatus.PENDING,
    val requestedAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null,
    val inviteCode: String = ""
)

enum class JoinRequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.GROUP_JOIN_REQUEST,
    val title: String = "",
    val message: String = "",
    val recipientId: String = "",
    val data: Map<String, Any> = emptyMap(),
    val isRead: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class NotificationType {
    GROUP_JOIN_REQUEST,
    FRIEND_REQUEST,
    EXPENSE_ADDED,
    SETTLEMENT_REMINDER,
    GROUP_INVITATION,
    UNKNOWN
}

data class GroupInvite(
    val id: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val inviterId: String = "",
    val inviterName: String = "",
    val inviteeId: String = "",
    val inviteeName: String = "",
    val inviteeEmail: String = "",
    val status: GroupInviteStatus = GroupInviteStatus.PENDING,
    val sentAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null
)

enum class GroupInviteStatus {
    PENDING,
    ACCEPTED,
    REJECTED
} 