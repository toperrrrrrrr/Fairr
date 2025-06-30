package com.example.fairr.data.model

import com.google.firebase.Timestamp

data class FriendGroup(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val color: String = "#FF6B6B", // Hex color for visual identification
    val emoji: String = "👥", // Emoji icon for the group
    val createdBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val memberIds: List<String> = emptyList(), // List of friend IDs in this group
    val isDefault: Boolean = false // Default groups like "All Friends", "Family", etc.
)

data class FriendGroupMembership(
    val id: String = "",
    val friendId: String = "",
    val groupId: String = "",
    val addedAt: Timestamp = Timestamp.now(),
    val addedBy: String = ""
)

enum class DefaultFriendGroupType(val displayName: String, val emoji: String, val color: String) {
    ALL_FRIENDS("All Friends", "👥", "#4A90E2"),
    FAMILY("Family", "👪", "#FF6B6B"),
    WORK("Work", "💼", "#50C878"),
    SCHOOL("School", "🎓", "#FFB347"),
    CLOSE_FRIENDS("Close Friends", "❤️", "#FF69B4"),
    ACQUAINTANCES("Acquaintances", "🤝", "#87CEEB")
} 