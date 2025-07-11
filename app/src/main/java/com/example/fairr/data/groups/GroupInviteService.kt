package com.example.fairr.data.groups

import android.util.Log
import com.example.fairr.data.model.GroupInvite
import com.example.fairr.data.model.GroupInviteStatus
import com.example.fairr.data.model.NotificationType
import com.example.fairr.data.notifications.NotificationService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

private const val TAG = "GroupInviteService"

sealed class GroupInviteResult {
    data class Success(val message: String, val inviteCode: String? = null) : GroupInviteResult()
    data class Error(val message: String) : GroupInviteResult()
}

@Singleton
class GroupInviteService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val notificationService: NotificationService
) {
    
    /**
     * Send a group invitation by email
     */
    suspend fun sendGroupInvitation(
        groupId: String,
        inviteeEmail: String,
        message: String = ""
    ): GroupInviteResult {
        val currentUser = auth.currentUser ?: return GroupInviteResult.Error("User not authenticated")
        
        try {
            // Check if user is admin of the group
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupInviteResult.Error("Group not found")
            }
            
            val groupData = groupDoc.data ?: return GroupInviteResult.Error("Group data not found")
            val members = groupData["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
            val userMember = members[currentUser.uid] as? Map<String, Any>
            val isAdmin = userMember?.get("isAdmin") as? Boolean ?: false
            
            if (!isAdmin) {
                return GroupInviteResult.Error("Only admins can send invitations")
            }
            
            // Check if user is already a member
            val isAlreadyMember = members.values.any { 
                (it["email"] as? String) == inviteeEmail 
            }
            if (isAlreadyMember) {
                return GroupInviteResult.Error("User is already a member of this group")
            }
            
            // Check if invitation already exists
            val existingInvite = firestore.collection("groupInvites")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("inviteeEmail", inviteeEmail)
                .whereEqualTo("status", GroupInviteStatus.PENDING.name)
                .get()
                .await()
            
            if (!existingInvite.isEmpty) {
                return GroupInviteResult.Error("Invitation already sent to this email")
            }
            
            // Generate unique invite code
            val inviteCode = generateInviteCode()
            
            // Get current user info
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val userName = userDoc.getString("displayName") ?: "Unknown"
            
            // Create invitation
            val invitation = GroupInvite(
                groupId = groupId,
                groupName = groupData["name"] as? String ?: "Unknown Group",
                groupAvatar = groupData["avatar"] as? String ?: "",
                inviterId = currentUser.uid,
                inviterName = userName,
                inviteeEmail = inviteeEmail,
                inviteCode = inviteCode,
                message = message,
                createdAt = Timestamp.now(),
                expiresAt = Timestamp(Timestamp.now().seconds + (7 * 24 * 60 * 60), 0) // 7 days
            )
            
            val inviteDoc = firestore.collection("groupInvites").add(invitation).await()
            
            // Find the user ID by email to send notification
            val userQuery = firestore.collection("users")
                .whereEqualTo("email", inviteeEmail)
                .limit(1)
                .get()
                .await()
            
            if (!userQuery.isEmpty) {
                val inviteeUserId = userQuery.documents.first().id
                
                // Create notification for the invitee
                notificationService.createNotification(
                    type = NotificationType.GROUP_INVITATION,
                    title = "Group Invitation",
                    message = "$userName invited you to join '${invitation.groupName}'",
                    recipientId = inviteeUserId,
                    data = mapOf(
                        "inviteId" to inviteDoc.id,
                        "groupId" to groupId,
                        "inviterId" to currentUser.uid,
                        "groupName" to invitation.groupName
                    )
                )
            }
            
            return GroupInviteResult.Success(
                "Invitation sent successfully",
                inviteCode
            )
            
        } catch (e: Exception) {
            return GroupInviteResult.Error("Failed to send invitation: ${e.message}")
        }
    }
    
    /**
     * Join group using invite code
     */
    suspend fun joinGroupWithCode(inviteCode: String): GroupInviteResult {
        val currentUser = auth.currentUser ?: return GroupInviteResult.Error("User not authenticated")
        
        try {
            // Find invitation by code
            val inviteQuery = firestore.collection("groupInvites")
                .whereEqualTo("inviteCode", inviteCode)
                .whereEqualTo("status", GroupInviteStatus.PENDING.name)
                .get()
                .await()
            
            if (inviteQuery.isEmpty) {
                return GroupInviteResult.Error("Invalid or expired invite code")
            }
            
            val inviteDoc = inviteQuery.documents.first()
            val invite = inviteDoc.toObject(GroupInvite::class.java) ?: return GroupInviteResult.Error("Invalid invitation")
            
            // Check if invitation is expired
            invite.expiresAt?.let { expirationTime ->
                if (expirationTime < Timestamp.now()) {
                    // Update status to expired
                    inviteDoc.reference.update("status", GroupInviteStatus.EXPIRED.name).await()
                    return GroupInviteResult.Error("Invitation has expired")
                }
            }
            
            // Get current user info
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val userName = userDoc.getString("displayName") ?: "Unknown"
            val userEmail = userDoc.getString("email") ?: currentUser.email ?: ""
            
            // Check if user is already a member
            val groupDoc = firestore.collection("groups").document(invite.groupId).get().await()
            val groupData = groupDoc.data ?: return GroupInviteResult.Error("Group not found")
            val members = groupData["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
            
            if (members.containsKey(currentUser.uid)) {
                return GroupInviteResult.Error("You are already a member of this group")
            }
            
            // Add user to group
            val newMember = mapOf(
                "name" to userName,
                "email" to userEmail,
                "isAdmin" to false,
                "joinedAt" to Timestamp.now(),
                "addedBy" to invite.inviterId
            )
            
            firestore.collection("groups")
                .document(invite.groupId)
                .update("members.${currentUser.uid}", newMember)
                .await()
            
            // Update invitation status
            inviteDoc.reference.update(
                mapOf(
                    "status" to GroupInviteStatus.ACCEPTED.name,
                    "acceptedAt" to Timestamp.now(),
                    "inviteeId" to currentUser.uid
                )
            ).await()
            
            return GroupInviteResult.Success("Successfully joined ${invite.groupName}")
            
        } catch (e: Exception) {
            return GroupInviteResult.Error("Failed to join group: ${e.message}")
        }
    }
    
    /**
     * Get pending invitations for current user
     */
    fun getPendingInvitations(): Flow<List<GroupInvite>> = flow {
        val currentUser = auth.currentUser ?: return@flow
        val userEmail = currentUser.email ?: return@flow
        
        try {
            val invitesSnapshot = firestore.collection("groupInvites")
                .whereEqualTo("inviteeEmail", userEmail)
                .whereEqualTo("status", GroupInviteStatus.PENDING.name)
                .get()
                .await()
            
            val invites = invitesSnapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(GroupInvite::class.java)?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            
            emit(invites)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    /**
     * Accept group invitation
     */
    suspend fun acceptInvitation(inviteId: String): GroupInviteResult {
        Log.d(TAG, "Starting acceptInvitation with inviteId: $inviteId")
        
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "User not authenticated")
                return GroupInviteResult.Error("User not authenticated")
            }

            Log.d(TAG, "User authenticated: ${currentUser.uid}")

            // Get the invite document
            val inviteDoc = firestore.collection("groupInvites").document(inviteId).get().await()
            if (!inviteDoc.exists()) {
                Log.e(TAG, "Invite document does not exist: $inviteId")
                return GroupInviteResult.Error("Invitation not found")
            }

            Log.d(TAG, "Invite document found: ${inviteDoc.data}")

            val inviteData = inviteDoc.data
            if (inviteData == null) {
                Log.e(TAG, "Invite data is null")
                return GroupInviteResult.Error("Invalid invitation data")
            }

            val groupId = inviteData["groupId"] as? String
            val invitedEmail = inviteData["inviteeEmail"] as? String
            val isAccepted = inviteData["status"] == GroupInviteStatus.ACCEPTED.name
            val expiresAt = inviteData["expiresAt"] as? Timestamp

            Log.d(TAG, "Invite data parsed - groupId: $groupId, invitedEmail: $invitedEmail, isAccepted: $isAccepted, expiresAt: $expiresAt")

            if (groupId == null) {
                Log.e(TAG, "Group ID is null in invite data")
                return GroupInviteResult.Error("Invalid invitation data")
            }

            if (invitedEmail == null) {
                Log.e(TAG, "Invited email is null in invite data")
                return GroupInviteResult.Error("Invalid invitation data")
            }

            // Check if already accepted
            if (isAccepted) {
                Log.w(TAG, "Invitation already accepted")
                return GroupInviteResult.Error("Invitation already accepted")
            }

            // Check expiration
            if (expiresAt != null && Timestamp.now().compareTo(expiresAt) > 0) {
                Log.w(TAG, "Invitation has expired")
                return GroupInviteResult.Error("Invitation has expired")
            }

            // Verify user's email matches the invited email
            val userEmail = currentUser.email
            if (userEmail != invitedEmail) {
                Log.e(TAG, "User email ($userEmail) does not match invited email ($invitedEmail)")
                return GroupInviteResult.Error("You can only accept invitations sent to your email address")
            }

            Log.d(TAG, "Email verification passed")

            // Get the group document
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            if (!groupDoc.exists()) {
                Log.e(TAG, "Group document does not exist: $groupId")
                return GroupInviteResult.Error("Group not found")
            }

            Log.d(TAG, "Group document found")

            val groupData = groupDoc.data
            if (groupData == null) {
                Log.e(TAG, "Group data is null")
                return GroupInviteResult.Error("Invalid group data")
            }

            // Check if user is already a member
            val memberIds = groupData["memberIds"] as? List<String> ?: emptyList()
            if (currentUser.uid in memberIds) {
                Log.w(TAG, "User is already a member of the group")
                
                // Even if user is already a member, we should update the invite status to ACCEPTED
                // and update the notification to show the accepted state
                Log.d(TAG, "Updating invite status to ACCEPTED (user already member)")
                firestore.collection("groupInvites")
                    .document(inviteId)
                    .update(
                        mapOf(
                            "status" to GroupInviteStatus.ACCEPTED.name,
                            "acceptedAt" to Timestamp.now(),
                            "inviteeId" to currentUser.uid
                        )
                    )
                    .await()

                // Update the notification to show as accepted
                Log.d(TAG, "Updating notification to show accepted state")
                try {
                    notificationService.updateNotificationStatus(
                        notificationId = null,
                        inviteId = inviteId,
                        status = GroupInviteStatus.ACCEPTED.name
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update notification status: ${e.message}")
                    // Don't fail the whole operation if notification update fails
                }

                Log.d(TAG, "Invitation marked as accepted (user already member)")
                return GroupInviteResult.Success("You are already a member of this group")
            }

            Log.d(TAG, "User is not already a member, proceeding with acceptance")

            // Update the group document to add the user as a member
            // Use arrayUnion to add the user to memberIds without reading the document first
            val userDisplayName = currentUser.displayName ?: "Unknown User"
            val memberInfo = mapOf(
                "name" to userDisplayName,
                "email" to userEmail,
                "joinedAt" to Timestamp.now(),
                "isAdmin" to false
            )
            
            Log.d(TAG, "Updating group with new member: ${currentUser.uid}")

            // Update group document using arrayUnion to avoid permission issues
            firestore.collection("groups")
                .document(groupId)
                .update(
                    mapOf(
                        "members.${currentUser.uid}" to memberInfo,
                        "memberIds" to com.google.firebase.firestore.FieldValue.arrayUnion(currentUser.uid)
                    )
                )
                .await()

            Log.d(TAG, "Group document updated successfully")

            // Update the invite document to mark it as accepted
            Log.d(TAG, "Updating invite status to ACCEPTED")
            firestore.collection("groupInvites")
                .document(inviteId)
                .update(
                    mapOf(
                        "status" to GroupInviteStatus.ACCEPTED.name,
                        "acceptedAt" to Timestamp.now(),
                        "inviteeId" to currentUser.uid
                    )
                )
                .await()

            // Update the notification to show as accepted
            Log.d(TAG, "Updating notification to show accepted state")
            try {
                notificationService.updateNotificationStatus(
                    notificationId = null, // We'll need to find the notification by inviteId
                    inviteId = inviteId,
                    status = GroupInviteStatus.ACCEPTED.name
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update notification status: ${e.message}")
                // Don't fail the whole operation if notification update fails
            }

            Log.d(TAG, "Invitation accepted successfully")
            return GroupInviteResult.Success("Successfully joined the group")
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting invitation", e)
            GroupInviteResult.Error("Failed to accept invitation: ${e.message}")
        }
    }
    
    /**
     * Reject group invitation
     */
    suspend fun rejectInvitation(inviteId: String): GroupInviteResult {
        Log.d(TAG, "rejectInvitation called with inviteId: $inviteId")
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            return GroupInviteResult.Error("User not authenticated")
        }
        
        try {
            Log.d(TAG, "Fetching invitation document...")
            val inviteDoc = firestore.collection("groupInvites").document(inviteId).get().await()
            if (!inviteDoc.exists()) {
                Log.e(TAG, "Invitation document not found: $inviteId")
                return GroupInviteResult.Error("Invitation not found")
            }
            
            Log.d(TAG, "Invitation document found, parsing...")
            val invite = inviteDoc.toObject(GroupInvite::class.java)
            if (invite == null) {
                Log.e(TAG, "Failed to parse invitation object")
                return GroupInviteResult.Error("Invalid invitation")
            }
            
            Log.d(TAG, "Invitation parsed - Status: ${invite.status}, InviteeEmail: ${invite.inviteeEmail}")
            
            if (invite.status != GroupInviteStatus.PENDING) {
                Log.e(TAG, "Invitation is not pending - current status: ${invite.status}")
                return GroupInviteResult.Error("Invitation is no longer pending")
            }
            
            // Validate that the current user's email matches the invitation
            val currentUserEmail = currentUser.email ?: ""
            Log.d(TAG, "Validating email match - Current user email: '$currentUserEmail', Invitation email: '${invite.inviteeEmail}'")
            if (invite.inviteeEmail != currentUserEmail) {
                Log.e(TAG, "Email mismatch - invitation not for current user")
                return GroupInviteResult.Error("This invitation was not sent to your email address")
            }
            
            Log.d(TAG, "Updating invitation status to rejected...")
            firestore.collection("groupInvites")
                .document(inviteId)
                .update(
                    mapOf(
                        "status" to GroupInviteStatus.REJECTED.name,
                        "rejectedAt" to Timestamp.now(),
                        "inviteeId" to currentUser.uid
                    )
                )
                .await()
            
            // Update the notification to show as rejected
            Log.d(TAG, "Updating notification to show rejected state")
            try {
                notificationService.updateNotificationStatus(
                    notificationId = null,
                    inviteId = inviteId,
                    status = GroupInviteStatus.REJECTED.name
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to update notification status: ${e.message}")
                // Don't fail the whole operation if notification update fails
            }
            
            Log.d(TAG, "Successfully rejected invitation")
            return GroupInviteResult.Success("Invitation rejected")
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in rejectInvitation", e)
            return GroupInviteResult.Error("Failed to reject invitation: ${e.message}")
        }
    }
    
    private fun generateInviteCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }
}
