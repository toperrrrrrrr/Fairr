package com.example.fairr.data.groups

import android.util.Log
import com.example.fairr.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GroupInviteService"

sealed class InviteResult {
    data class Success(val inviteId: String) : InviteResult()
    data class Error(val message: String) : InviteResult()
}

@Singleton
class GroupInviteService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val invitesCollection = firestore.collection("groupInvites")
    private val notificationsCollection = firestore.collection("notifications")
    private val usersCollection = firestore.collection("users")
    private val groupsCollection = firestore.collection("groups")

    suspend fun sendGroupInvite(groupId: String, inviteeEmail: String): InviteResult {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Failed to send invite: User not authenticated")
                return InviteResult.Error("User not authenticated")
            }

            Log.d(TAG, "Starting group invite process - From: ${currentUser.email}, To: $inviteeEmail, Group: $groupId")

            // Get group information
            val groupDoc = try {
                groupsCollection.document(groupId).get().await()
            } catch (e: Exception) {
                Log.e(TAG, "Network error while fetching group", e)
                return InviteResult.Error("Network error. Please check your internet connection and try again.")
            }

            if (!groupDoc.exists()) {
                Log.e(TAG, "Failed to send invite: Group $groupId not found")
                return InviteResult.Error("Group not found")
            }

            val groupData = groupDoc.data
            if (groupData == null) {
                Log.e(TAG, "Failed to send invite: Group data is null for group $groupId")
                return InviteResult.Error("Group data not found")
            }

            val groupName = groupData["name"] as? String ?: "Unknown Group"
            Log.d(TAG, "Found group: $groupName")

            // Check if user is admin of the group
            val membersMap = groupData["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
            val currentUserMember = membersMap[currentUser.uid]
            if (currentUserMember?.get("isAdmin") != true) {
                Log.e(TAG, "Failed to send invite: User ${currentUser.uid} is not admin of group $groupId")
                return InviteResult.Error("You don't have permission to invite members to this group")
            }

            // Find the user by email
            Log.d(TAG, "Searching for user with email: $inviteeEmail")
            val userQuery = try {
                usersCollection
                    .whereEqualTo("email", inviteeEmail)
                    .limit(1)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Network error while searching for user", e)
                return InviteResult.Error("Network error. Please check your internet connection and try again.")
            }

            if (userQuery.isEmpty) {
                Log.e(TAG, "Failed to send invite: User with email $inviteeEmail not found")
                return InviteResult.Error("User with email $inviteeEmail not found")
            }

            val inviteeDoc = userQuery.documents.first()
            val inviteeId = inviteeDoc.id
            val inviteeName = inviteeDoc.getString("displayName") ?: inviteeEmail.substringBefore("@")
            Log.d(TAG, "Found invitee: $inviteeName (ID: $inviteeId)")

            // Check if user is already a member
            if (membersMap.containsKey(inviteeId)) {
                Log.e(TAG, "Failed to send invite: User $inviteeId is already a member of group $groupId")
                return InviteResult.Error("User is already a member of this group")
            }

            // Check if there's already a pending invite
            Log.d(TAG, "Checking for existing invites...")
            val existingInvite = try {
                invitesCollection
                    .whereEqualTo("groupId", groupId)
                    .whereEqualTo("inviteeId", inviteeId)
                    .whereEqualTo("status", GroupInviteStatus.PENDING.name)
                    .get()
                    .await()
            } catch (e: Exception) {
                Log.e(TAG, "Network error while checking existing invites", e)
                return InviteResult.Error("Network error. Please check your internet connection and try again.")
            }

            if (!existingInvite.isEmpty) {
                Log.e(TAG, "Failed to send invite: User $inviteeId already has a pending invite for group $groupId")
                return InviteResult.Error("User already has a pending invite for this group")
            }

            // Create the invite
            val inviteId = invitesCollection.document().id
            Log.d(TAG, "Creating new invite with ID: $inviteId")
            
            val invite = GroupInvite(
                id = inviteId,
                groupId = groupId,
                groupName = groupName,
                inviterId = currentUser.uid,
                inviterName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown",
                inviteeId = inviteeId,
                inviteeName = inviteeName,
                inviteeEmail = inviteeEmail,
                status = GroupInviteStatus.PENDING,
                sentAt = Timestamp.now()
            )

            val inviteData = mapOf(
                "groupId" to invite.groupId,
                "groupName" to invite.groupName,
                "inviterId" to invite.inviterId,
                "inviterName" to invite.inviterName,
                "inviteeId" to invite.inviteeId,
                "inviteeName" to invite.inviteeName,
                "inviteeEmail" to invite.inviteeEmail,
                "status" to invite.status.name,
                "sentAt" to invite.sentAt
            )

            try {
                Log.d(TAG, "Saving invite to Firestore...")
                invitesCollection.document(inviteId).set(inviteData).await()
                Log.d(TAG, "Invite saved successfully")

                // Create notification for the invitee
                Log.d(TAG, "Creating notification for invitee...")
                createInviteNotification(invite)
                Log.d(TAG, "Notification created successfully")

                Log.d(TAG, "Group invite process completed successfully")
                InviteResult.Success(inviteId)
            } catch (e: Exception) {
                Log.e(TAG, "Network error while saving invite", e)
                return InviteResult.Error("Network error. Please check your internet connection and try again.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending group invite", e)
            InviteResult.Error(e.message ?: "Failed to send group invite")
        }
    }

    suspend fun respondToInvite(inviteId: String, accept: Boolean): InviteResult {
        return try {
            val currentUser = auth.currentUser
                ?: return InviteResult.Error("User not authenticated")

            val inviteDoc = invitesCollection.document(inviteId).get().await()
            if (!inviteDoc.exists()) {
                return InviteResult.Error("Invite not found")
            }

            val inviteData = inviteDoc.data ?: return InviteResult.Error("Invite data not found")
            val inviteeId = inviteData["inviteeId"] as? String

            if (inviteeId != currentUser.uid) {
                return InviteResult.Error("You are not authorized to respond to this invite")
            }

            val status = if (accept) GroupInviteStatus.ACCEPTED else GroupInviteStatus.REJECTED

            // Update invite status
            invitesCollection.document(inviteId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "respondedAt" to Timestamp.now()
                    )
                )
                .await()

            if (accept) {
                // Add user to group
                val groupId = inviteData["groupId"] as? String ?: return InviteResult.Error("Group ID not found")
                val inviteeName = inviteData["inviteeName"] as? String ?: "Unknown"
                val inviteeEmail = inviteData["inviteeEmail"] as? String ?: ""

                val newMember = mapOf(
                    "name" to inviteeName,
                    "email" to inviteeEmail,
                    "isAdmin" to false,
                    "joinedAt" to Timestamp.now()
                )

                // Get existing memberIds array or create new one
                val groupDoc = groupsCollection.document(groupId).get().await()
                val groupData = groupDoc.data ?: emptyMap()
                val memberIds = (groupData["memberIds"] as? List<String> ?: emptyList()) + currentUser.uid

                // Update both members map and memberIds array
                groupsCollection.document(groupId)
                    .update(
                        mapOf(
                            "members.${currentUser.uid}" to newMember,
                            "memberIds" to memberIds
                        )
                    )
                    .await()
            }

            InviteResult.Success(inviteId)
        } catch (e: Exception) {
            Log.e(TAG, "Error responding to invite", e)
            InviteResult.Error(e.message ?: "Failed to respond to invite")
        }
    }

    fun getInvitesForUser(): Flow<List<GroupInvite>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = invitesCollection
            .whereEqualTo("inviteeId", currentUser.uid)
            .whereEqualTo("status", GroupInviteStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val invites = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        GroupInvite(
                            id = doc.id,
                            groupId = data["groupId"] as? String ?: "",
                            groupName = data["groupName"] as? String ?: "",
                            inviterId = data["inviterId"] as? String ?: "",
                            inviterName = data["inviterName"] as? String ?: "",
                            inviteeId = data["inviteeId"] as? String ?: "",
                            inviteeName = data["inviteeName"] as? String ?: "",
                            inviteeEmail = data["inviteeEmail"] as? String ?: "",
                            status = GroupInviteStatus.valueOf(data["status"] as? String ?: "PENDING"),
                            sentAt = data["sentAt"] as? Timestamp ?: Timestamp.now(),
                            respondedAt = data["respondedAt"] as? Timestamp
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing invite", e)
                        null
                    }
                } ?: emptyList()

                trySend(invites)
            }

        awaitClose { subscription.remove() }
    }

    private suspend fun createInviteNotification(invite: GroupInvite) {
        try {
            val notificationId = notificationsCollection.document().id
            val notification = Notification(
                id = notificationId,
                type = NotificationType.GROUP_INVITATION,
                title = "Group Invitation",
                message = "${invite.inviterName} invited you to join ${invite.groupName}",
                recipientId = invite.inviteeId,
                data = mapOf(
                    "inviteId" to invite.id,
                    "groupId" to invite.groupId,
                    "inviterId" to invite.inviterId
                ),
                isRead = false,
                createdAt = Timestamp.now()
            )

            val notificationData = mapOf(
                "type" to notification.type.name,
                "title" to notification.title,
                "message" to notification.message,
                "recipientId" to notification.recipientId,
                "data" to notification.data,
                "isRead" to notification.isRead,
                "createdAt" to notification.createdAt
            )

            notificationsCollection.document(notificationId).set(notificationData).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
        }
    }
} 