package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "USD",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val members: List<GroupMember> = emptyList()
)

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