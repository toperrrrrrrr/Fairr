package com.example.fairr.ui.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Immutable
data class Friend(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val status: FriendStatus = FriendStatus.PENDING,
    val addedAt: Long = System.currentTimeMillis()
)

@Stable
enum class FriendStatus {
    PENDING,    // Friend request sent but not accepted
    ACCEPTED    // Friend request accepted
}

@Immutable
data class FriendRequest(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderEmail: String,
    val senderPhotoUrl: String? = null,
    val receiverId: String,
    val status: FriendStatus = FriendStatus.PENDING,
    val sentAt: Long = System.currentTimeMillis()
) 