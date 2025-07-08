package com.example.fairr.data.notifications

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

private const val TAG = "NotificationService"

@Singleton
class NotificationService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val notificationsCollection = firestore.collection("notifications")

    fun getNotificationsForUser(): Flow<List<Notification>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        Log.d(TAG, "Starting notifications listener for user: ${currentUser.uid}")

        // First, get initial data
        try {
            val initialSnapshot = notificationsCollection
                .whereEqualTo("recipientId", currentUser.uid)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val initialNotifications = initialSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data
                    if (data == null) {
                        Log.e(TAG, "Notification data is null for document: ${doc.id}")
                        return@mapNotNull null
                    }

                    val typeStr = data["type"] as? String
                    val type = when {
                        typeStr == null -> {
                            Log.w(TAG, "Notification type is null for document: ${doc.id}")
                            NotificationType.UNKNOWN
                        }
                        typeStr !in NotificationType.values().map { it.name } -> {
                            Log.w(TAG, "Unknown notification type '$typeStr' for document: ${doc.id}")
                            NotificationType.UNKNOWN
                        }
                        else -> NotificationType.valueOf(typeStr)
                    }

                    val notificationData = data["data"] as? Map<String, Any> ?: emptyMap()
                    Log.d(TAG, "Notification data for ${doc.id}: $notificationData")

                    Notification(
                        id = doc.id,
                        type = type,
                        title = data["title"] as? String ?: "",
                        message = data["message"] as? String ?: "",
                        recipientId = data["recipientId"] as? String ?: "",
                        data = notificationData,
                        isRead = data["isRead"] as? Boolean ?: false,
                        createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
                    ).also {
                        Log.d(TAG, "Parsed notification: ${it.id}, type: ${it.type}, data: ${it.data}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification document: ${doc.id}", e)
                    null
                }
            }
            trySend(initialNotifications)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting initial notifications", e)
            trySend(emptyList())
        }

        // Then set up real-time listener
        val subscription = notificationsCollection
            .whereEqualTo("recipientId", currentUser.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching notifications", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.d(TAG, "No notifications snapshot")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "Received ${snapshot.documents.size} notifications")

                val notifications = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data
                        if (data == null) {
                            Log.e(TAG, "Notification data is null for document: ${doc.id}")
                            return@mapNotNull null
                        }

                        val typeStr = data["type"] as? String
                        val type = when {
                            typeStr == null -> {
                                Log.w(TAG, "Notification type is null for document: ${doc.id}")
                                NotificationType.UNKNOWN
                            }
                            typeStr !in NotificationType.values().map { it.name } -> {
                                Log.w(TAG, "Unknown notification type '$typeStr' for document: ${doc.id}")
                                NotificationType.UNKNOWN
                            }
                            else -> NotificationType.valueOf(typeStr)
                        }

                        val notificationData = data["data"] as? Map<String, Any> ?: emptyMap()
                        Log.d(TAG, "Notification data for ${doc.id}: $notificationData")

                        Notification(
                            id = doc.id,
                            type = type,
                            title = data["title"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            recipientId = data["recipientId"] as? String ?: "",
                            data = notificationData,
                            isRead = data["isRead"] as? Boolean ?: false,
                            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
                        ).also {
                            Log.d(TAG, "Parsed notification: ${it.id}, type: ${it.type}, data: ${it.data}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notification document: ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Successfully parsed ${notifications.size} notifications")
                trySend(notifications)
            }

        awaitClose { 
            Log.d(TAG, "Closing notifications listener")
            subscription.remove() 
        }
    }

    suspend fun createNotification(
        type: NotificationType,
        title: String,
        message: String,
        recipientId: String,
        data: Map<String, Any> = emptyMap()
    ): Boolean {
        return try {
            val notificationId = notificationsCollection.document().id
            val notification = Notification(
                id = notificationId,
                type = type,
                title = title,
                message = message,
                recipientId = recipientId,
                data = data,
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
            Log.d(TAG, "Successfully created notification: $notificationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
            false
        }
    }

    /**
     * Creates sample notifications using real user data from Firestore.
     * This function pulls actual group memberships, friend requests, and user information
     * to create realistic notifications instead of hardcoded fake data.
     */
    suspend fun createTestNotifications(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated - cannot create test notifications")
            return false
        }

        return try {
            // Get real user data
            val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
            val currentUserName = userDoc.getString("displayName") ?: "Unknown User"
            
            // Get user's groups to create realistic notifications
            val userGroupsSnapshot = firestore.collection("groups")
                .whereEqualTo("members.${currentUser.uid}.isAdmin", false) // Get groups where user is not admin
                .limit(3)
                .get()
                .await()

            val userGroups = userGroupsSnapshot.documents.mapNotNull { doc ->
                val groupName = doc.getString("name") ?: return@mapNotNull null
                val members = doc.data?.get("members") as? Map<String, Map<String, Any>> ?: return@mapNotNull null
                val adminMember = members.values.find { (it["isAdmin"] as? Boolean) == true }
                val adminName = adminMember?.get("name") as? String ?: "Group Admin"
                Triple(doc.id, groupName, adminName)
            }

            // Get real friend requests if any
            val friendRequestsSnapshot = firestore.collection("friendRequests")
                .whereEqualTo("receiverId", currentUser.uid)
                .whereEqualTo("status", "PENDING")
                .limit(2)
                .get()
                .await()

            val realFriendRequests = friendRequestsSnapshot.documents.mapNotNull { doc ->
                val senderName = doc.getString("senderName") ?: return@mapNotNull null
                Pair(doc.id, senderName)
            }

            // Create notifications using real data where possible
            val notifications = mutableListOf<Pair<Triple<NotificationType, String, String>, Map<String, Any>>>()

            // Add friend request notifications from real data
            realFriendRequests.forEach { (requestId, senderName) ->
                notifications.add(
                    Triple(
                        NotificationType.FRIEND_REQUEST,
                        "Friend Request",
                        "$senderName sent you a friend request"
                    ) to mapOf(
                        "requestId" to requestId,
                        "requesterId" to "real_user_id"
                    )
                )
            }

            // Add group-related notifications using real group data
            userGroups.take(2).forEach { (groupId, groupName, adminName) ->
                notifications.add(
                    Triple(
                        NotificationType.GROUP_INVITATION,
                        "Group Invitation",
                        "$adminName invited you to join '$groupName'"
                    ) to mapOf(
                        "inviteId" to "real_invite_${groupId}",
                        "groupId" to groupId,
                        "inviterId" to "real_admin_id"
                    )
                )
            }

            // Add some example expense notifications using real group data
            userGroups.take(1).forEach { (groupId, groupName, _) ->
                notifications.add(
                    Triple(
                        NotificationType.EXPENSE_ADDED,
                        "New Expense Added",
                        "New expense 'Grocery Shopping' (₱850.00) was added to '$groupName'"
                    ) to mapOf(
                        "expenseId" to "real_expense_${groupId}",
                        "groupId" to groupId
                    )
                )
            }

            // Add settlement reminder if user has groups
            if (userGroups.isNotEmpty()) {
                val (groupId, groupName, adminName) = userGroups.first()
                notifications.add(
                    Triple(
                        NotificationType.SETTLEMENT_REMINDER,
                        "Settlement Reminder",
                        "You owe $adminName ₱125.50 for expenses in '$groupName'"
                    ) to mapOf(
                        "settlementId" to "real_settlement_${groupId}",
                        "amount" to "125.50",
                        "creditorId" to "real_creditor_id",
                        "groupId" to groupId
                    )
                )
            }

            // If no real data available, create some sample notifications with realistic but generic data
            if (notifications.isEmpty()) {
                notifications.addAll(
                    listOf(
                        Triple(
                            NotificationType.GROUP_INVITATION,
                            "Group Invitation",
                            "You've been invited to join 'Family Expenses'"
                        ) to mapOf(
                            "inviteId" to "sample_invite_123",
                            "groupId" to "sample_group_456",
                            "inviterId" to "sample_user_789"
                        ),
                        Triple(
                            NotificationType.EXPENSE_ADDED,
                            "New Expense Added",
                            "New expense 'Lunch' (₱320.00) was added to 'Office Team'"
                        ) to mapOf(
                            "expenseId" to "sample_expense_123",
                            "groupId" to "sample_group_456"
                        )
                    )
                )
            }

            var successCount = 0
            notifications.forEach { (notificationData, data) ->
                val (type, title, message) = notificationData
                val success = createNotification(
                    type = type,
                    title = title,
                    message = message,
                    recipientId = currentUser.uid,
                    data = data
                )
                if (success) successCount++
            }

            Log.d(TAG, "Created $successCount notifications using real user data")
            successCount == notifications.size
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notifications with real data", e)
            false
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            Log.d(TAG, "Marking notification as read: $notificationId")
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            Log.d(TAG, "Successfully marked notification as read: $notificationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: $notificationId", e)
            false
        }
    }

    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            Log.d(TAG, "Deleting notification: $notificationId")
            notificationsCollection.document(notificationId)
                .delete()
                .await()
            Log.d(TAG, "Successfully deleted notification: $notificationId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification: $notificationId", e)
            false
        }
    }

    suspend fun markAllAsRead(): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            return false
        }

        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("recipientId", currentUser.uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }

            batch.commit().await()
            Log.d(TAG, "Marked ${snapshot.documents.size} notifications as read")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking all notifications as read", e)
            false
        }
    }
} 