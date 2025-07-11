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
import com.google.firebase.auth.FirebaseUser

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

            Log.d(TAG, "Initial snapshot received with ${initialSnapshot.documents.size} documents")

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
            
            Log.d(TAG, "Successfully parsed ${initialNotifications.size} initial notifications")
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

                Log.d(TAG, "Received ${snapshot.documents.size} notifications in real-time update")

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

                Log.d(TAG, "Successfully parsed ${notifications.size} notifications from real-time update")
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

    /**
     * Get the current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    suspend fun updateNotificationStatus(
        notificationId: String? = null,
        inviteId: String? = null,
        status: String
    ): Boolean {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e(TAG, "User not authenticated")
            return false
        }

        return try {
            val query = notificationsCollection
                .whereEqualTo("recipientId", currentUser.uid)
                .whereEqualTo("type", "GROUP_INVITATION")

            // If we have a specific notificationId, use it
            val notificationRef = if (notificationId != null) {
                notificationsCollection.document(notificationId)
            } else {
                // Otherwise, find the notification by inviteId
                val snapshot = query
                    .whereEqualTo("data.inviteId", inviteId)
                    .limit(1)
                    .get()
                    .await()

                if (snapshot.documents.isEmpty()) {
                    Log.w(TAG, "No notification found for inviteId: $inviteId")
                    return false
                }
                snapshot.documents.first().reference
            }

            // Update the notification with the new status
            notificationRef.update(
                mapOf(
                    "data.status" to status,
                    "updatedAt" to Timestamp.now()
                )
            ).await()

            Log.d(TAG, "Notification status updated to: $status")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating notification status", e)
            false
        }
    }
} 