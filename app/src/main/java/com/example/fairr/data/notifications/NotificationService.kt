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
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = notificationsCollection
            .whereEqualTo("recipientId", currentUser.uid)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val notifications = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Notification(
                            id = doc.id,
                            type = NotificationType.valueOf(data["type"] as? String ?: "GROUP_JOIN_REQUEST"),
                            title = data["title"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            recipientId = data["recipientId"] as? String ?: "",
                            data = data["data"] as? Map<String, Any> ?: emptyMap(),
                            isRead = data["isRead"] as? Boolean ?: false,
                            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing notification", e)
                        null
                    }
                } ?: emptyList()

                trySend(notifications)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId)
                .update("isRead", true)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
            false
        }
    }

    suspend fun deleteNotification(notificationId: String): Boolean {
        return try {
            notificationsCollection.document(notificationId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notification", e)
            false
        }
    }
} 