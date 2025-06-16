package com.example.fairr.data.groups

import android.util.Log
import com.example.fairr.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GroupJoinService"

sealed class JoinRequestResult {
    data class Success(val requestId: String) : JoinRequestResult()
    data class Error(val message: String) : JoinRequestResult()
}

@Singleton
class GroupJoinService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val joinRequestsCollection = firestore.collection("groupJoinRequests")
    private val notificationsCollection = firestore.collection("notifications")
    private val groupsCollection = firestore.collection("groups")
    private val usersCollection = firestore.collection("users")

    suspend fun requestToJoinGroup(inviteCode: String): JoinRequestResult {
        return try {
            Log.d(TAG, "Starting join request for invite code: $inviteCode")
            val currentUser = auth.currentUser
                ?: return JoinRequestResult.Error("User not authenticated")

            Log.d(TAG, "User authenticated: ${currentUser.uid}")

            // Find group by invite code
            Log.d(TAG, "Querying group by invite code...")
            val groupQuery = groupsCollection
                .whereEqualTo("inviteCode", inviteCode)
                .limit(1)
                .get()
                .await()

            Log.d(TAG, "Group query completed. Found ${groupQuery.size()} groups")

            if (groupQuery.isEmpty) {
                return JoinRequestResult.Error("Invalid invite code")
            }

            val groupDoc = groupQuery.documents.first()
            val groupData = groupDoc.data ?: return JoinRequestResult.Error("Group data not found")
            
            // Check if user is already a member
            val membersMap = groupData["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
            if (membersMap.containsKey(currentUser.uid)) {
                return JoinRequestResult.Error("You are already a member of this group")
            }

            // Check if there's already a pending request
            Log.d(TAG, "Checking for existing pending requests...")
            val existingRequest = joinRequestsCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("groupId", groupDoc.id)
                .whereEqualTo("status", JoinRequestStatus.PENDING.name)
                .get()
                .await()

            Log.d(TAG, "Existing request check completed. Found ${existingRequest.size()} pending requests")

            if (!existingRequest.isEmpty) {
                return JoinRequestResult.Error("You already have a pending request for this group")
            }

            val groupCreatorId = groupData["createdBy"] as? String
                ?: return JoinRequestResult.Error("Group creator not found")

            // Create join request
            val requestId = joinRequestsCollection.document().id
            val joinRequest = GroupJoinRequest(
                id = requestId,
                userId = currentUser.uid,
                userName = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown",
                userEmail = currentUser.email ?: "",
                groupId = groupDoc.id,
                groupName = groupData["name"] as? String ?: "Unknown Group",
                groupCreatorId = groupCreatorId,
                status = JoinRequestStatus.PENDING,
                requestedAt = Timestamp.now(),
                inviteCode = inviteCode
            )

            val requestData = mapOf(
                "userId" to joinRequest.userId,
                "userName" to joinRequest.userName,
                "userEmail" to joinRequest.userEmail,
                "groupId" to joinRequest.groupId,
                "groupName" to joinRequest.groupName,
                "groupCreatorId" to joinRequest.groupCreatorId,
                "status" to joinRequest.status.name,
                "requestedAt" to joinRequest.requestedAt,
                "inviteCode" to joinRequest.inviteCode
            )

            Log.d(TAG, "Creating join request document...")
            joinRequestsCollection.document(requestId).set(requestData).await()
            Log.d(TAG, "Join request document created successfully")

            // Create notification for group creator
            Log.d(TAG, "Creating notification for group creator...")
            createJoinRequestNotification(joinRequest)
            Log.d(TAG, "Notification created successfully")

            Log.d(TAG, "Join request completed successfully")
            JoinRequestResult.Success(requestId)
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting to join group", e)
            JoinRequestResult.Error(e.message ?: "Failed to request group join")
        }
    }

    suspend fun respondToJoinRequest(requestId: String, approve: Boolean): JoinRequestResult {
        return try {
            val currentUser = auth.currentUser
                ?: return JoinRequestResult.Error("User not authenticated")

            val requestDoc = joinRequestsCollection.document(requestId).get().await()
            if (!requestDoc.exists()) {
                return JoinRequestResult.Error("Join request not found")
            }

            val requestData = requestDoc.data ?: return JoinRequestResult.Error("Request data not found")
            val groupCreatorId = requestData["groupCreatorId"] as? String
            
            if (groupCreatorId != currentUser.uid) {
                return JoinRequestResult.Error("You are not authorized to respond to this request")
            }

            val status = if (approve) JoinRequestStatus.APPROVED else JoinRequestStatus.REJECTED
            
            // Update request status
            joinRequestsCollection.document(requestId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "respondedAt" to Timestamp.now()
                    )
                )
                .await()

            if (approve) {
                // Add user to group
                val groupId = requestData["groupId"] as? String ?: return JoinRequestResult.Error("Group ID not found")
                val userId = requestData["userId"] as? String ?: return JoinRequestResult.Error("User ID not found")
                val userName = requestData["userName"] as? String ?: "Unknown"
                val userEmail = requestData["userEmail"] as? String ?: ""

                val newMember = mapOf(
                    "name" to userName,
                    "email" to userEmail,
                    "isAdmin" to false,
                    "joinedAt" to Timestamp.now()
                )

                // Get existing memberIds array or create new one
                val groupDoc = groupsCollection.document(groupId).get().await()
                val groupData = groupDoc.data ?: emptyMap()
                val memberIds = (groupData["memberIds"] as? List<String> ?: emptyList()) + userId

                // Update both members map and memberIds array
                groupsCollection.document(groupId)
                    .update(
                        mapOf(
                            "members.$userId" to newMember,
                            "memberIds" to memberIds
                        )
                    )
                    .await()
            }

            JoinRequestResult.Success(requestId)
        } catch (e: Exception) {
            Log.e(TAG, "Error responding to join request", e)
            JoinRequestResult.Error(e.message ?: "Failed to respond to join request")
        }
    }

    fun getJoinRequestsForGroup(groupId: String): Flow<List<GroupJoinRequest>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = joinRequestsCollection
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("groupCreatorId", currentUser.uid)
            .whereEqualTo("status", JoinRequestStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        GroupJoinRequest(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            userEmail = data["userEmail"] as? String ?: "",
                            groupId = data["groupId"] as? String ?: "",
                            groupName = data["groupName"] as? String ?: "",
                            groupCreatorId = data["groupCreatorId"] as? String ?: "",
                            status = JoinRequestStatus.valueOf(data["status"] as? String ?: "PENDING"),
                            requestedAt = data["requestedAt"] as? Timestamp ?: Timestamp.now(),
                            respondedAt = data["respondedAt"] as? Timestamp,
                            inviteCode = data["inviteCode"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing join request", e)
                        null
                    }
                } ?: emptyList()

                trySend(requests)
            }

        awaitClose { subscription.remove() }
    }

    fun getAllJoinRequestsForUser(): Flow<List<GroupJoinRequest>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = joinRequestsCollection
            .whereEqualTo("groupCreatorId", currentUser.uid)
            .whereEqualTo("status", JoinRequestStatus.PENDING.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val requests = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        GroupJoinRequest(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            userName = data["userName"] as? String ?: "",
                            userEmail = data["userEmail"] as? String ?: "",
                            groupId = data["groupId"] as? String ?: "",
                            groupName = data["groupName"] as? String ?: "",
                            groupCreatorId = data["groupCreatorId"] as? String ?: "",
                            status = JoinRequestStatus.valueOf(data["status"] as? String ?: "PENDING"),
                            requestedAt = data["requestedAt"] as? Timestamp ?: Timestamp.now(),
                            respondedAt = data["respondedAt"] as? Timestamp,
                            inviteCode = data["inviteCode"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing join request", e)
                        null
                    }
                } ?: emptyList()

                trySend(requests)
            }

        awaitClose { subscription.remove() }
    }

    private suspend fun createJoinRequestNotification(request: GroupJoinRequest) {
        try {
            val notificationId = notificationsCollection.document().id
            val notification = Notification(
                id = notificationId,
                type = NotificationType.GROUP_JOIN_REQUEST,
                title = "New Group Join Request",
                message = "${request.userName} wants to join ${request.groupName}",
                recipientId = request.groupCreatorId,
                data = mapOf(
                    "requestId" to request.id,
                    "groupId" to request.groupId,
                    "userId" to request.userId
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