package com.example.fairr.ui.model

import androidx.compose.ui.graphics.vector.ImageVector

// Shared data models for group management
data class GroupMember(
    val id: String,
    val name: String,
    val email: String,
    val isAdmin: Boolean = false,
    val isCurrentUser: Boolean = false
)

data class GroupSettingsData(
    val id: String,
    val name: String,
    val description: String,
    val inviteCode: String,
    val currency: String,
    val createdBy: String,
    val isUserAdmin: Boolean,
    val memberCount: Int
)

data class CreateGroupData(
    val name: String,
    val description: String,
    val currency: String,
    val avatar: String = "",
    val avatarType: String = "EMOJI",
    val members: List<GroupMember> = emptyList()
)

data class Group(
    val id: String,
    val name: String,
    val description: String = "",
    val currency: String = "PHP",
    val createdBy: String,
    val members: List<GroupMember> = emptyList(),
    val inviteCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 