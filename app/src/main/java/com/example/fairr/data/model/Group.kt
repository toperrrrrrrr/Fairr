package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "PHP",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val inviteCode: String = "",
    val members: List<GroupMember> = emptyList(),
    val avatar: String = "",
    val avatarType: AvatarType = AvatarType.EMOJI,
    val isArchived: Boolean = false
) {
    val isUserAdmin: Boolean
        get() = members.any { it.role == GroupRole.ADMIN }
}

data class GroupMember(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val joinedAt: Timestamp = Timestamp.now(),
    val role: GroupRole = GroupRole.MEMBER
)

enum class GroupRole {
    ADMIN,
    MEMBER
}

enum class AvatarType {
    EMOJI,    // Single emoji character
    IMAGE     // Image URL
} 