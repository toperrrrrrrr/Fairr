package com.example.fairr.data.activity

import com.example.fairr.data.model.GroupActivity
import com.example.fairr.data.model.ActivityType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    fun getActivitiesForGroup(groupId: String): Flow<List<GroupActivity>> = flow {
        try {
            val activitiesRef = firestore.collection("groups")
                .document(groupId)
                .collection("activity_logs")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(50)
            
            val snapshot = activitiesRef.get().await()
            val activities = snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    GroupActivity(
                        id = doc.id,
                        type = ActivityType.valueOf(data["type"] as? String ?: "EXPENSE_ADDED"),
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        amount = (data["amount"] as? Number)?.toDouble(),
                        userName = data["userName"] as? String ?: "",
                        userInitials = data["userInitials"] as? String ?: "",
                        timestamp = (data["timestamp"] as? Timestamp)?.let { timestamp ->
                            timestamp.seconds * 1000L
                        } ?: System.currentTimeMillis(),
                        isPositive = data["isPositive"] as? Boolean ?: true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(activities)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
    
    suspend fun logActivity(
        groupId: String,
        type: ActivityType,
        title: String,
        description: String,
        amount: Double? = null,
        userName: String,
        userInitials: String,
        isPositive: Boolean = true
    ): Result<Unit> {
        return try {
            val activityData = HashMap<String, Any>()
            activityData["type"] = type.name
            activityData["title"] = title
            activityData["description"] = description
            activityData["userName"] = userName
            activityData["userInitials"] = userInitials
            activityData["timestamp"] = Timestamp.now()
            activityData["isPositive"] = isPositive
            if (amount != null) {
                activityData["amount"] = amount
            }
            
            firestore.collection("groups")
                .document(groupId)
                .collection("activity_logs")
                .add(activityData)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 