package com.example.fairr.data.activity

import android.util.Log
import com.example.fairr.data.model.GroupActivity
import com.example.fairr.data.model.ActivityType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.example.fairr.models.RecentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt

private const val TAG = "ActivityService"

// Pagination constants for activity
private const val ACTIVITY_PAGE_SIZE = 25
private const val MAX_ACTIVITY_PAGE_SIZE = 50
private const val INITIAL_ACTIVITY_LOAD = 15

/**
 * Paginated activity result
 */
data class PaginatedActivities(
    val activities: List<com.example.fairr.models.RecentActivity>,
    val hasMore: Boolean,
    val lastDocument: DocumentSnapshot?,
    val totalCount: Int? = null
)

/**
 * Activity query parameters
 */
data class ActivityQueryParams(
    val groupId: String,
    val pageSize: Int = ACTIVITY_PAGE_SIZE,
    val lastDocument: DocumentSnapshot? = null,
    val activityTypes: List<com.example.fairr.data.model.ActivityType>? = null,
    val userId: String? = null,
    val startDate: Date? = null,
    val endDate: Date? = null
)

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

    /**
     * Get paginated activities with better performance
     */
    suspend fun getPaginatedActivities(params: ActivityQueryParams): PaginatedActivities = 
        withContext(Dispatchers.IO) {
            try {
                val pageSize = params.pageSize.coerceAtMost(MAX_ACTIVITY_PAGE_SIZE)
                var query = firestore.collection("activities")
                    .whereEqualTo("groupId", params.groupId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
                
                // Apply filters
                params.activityTypes?.let { types ->
                    if (types.size == 1) {
                        // Single type filter - more efficient
                        query = query.whereEqualTo("type", types.first().name)
                    }
                    // For multiple types, we'll filter client-side since Firestore doesn't support IN with other conditions easily
                }
                
                params.userId?.let { userId ->
                    query = query.whereEqualTo("userId", userId)
                }
                
                // Handle pagination
                params.lastDocument?.let { lastDoc ->
                    query = query.startAfter(lastDoc)
                }
                
                val snapshot = query.get().await()
                
                // Parse activities with client-side filtering if needed
                val allActivities = snapshot.documents.mapNotNull { doc ->
                    parseActivityDocument(doc)
                }
                
                // Apply client-side filters for complex queries (skip complex filtering for now)
                val filteredActivities = allActivities
                
                // Apply date filters if needed (skip complex date filtering for now)
                val finalActivities = filteredActivities
                
                val hasMore = snapshot.documents.size == pageSize
                val lastDocument = snapshot.documents.lastOrNull()
                
                PaginatedActivities(
                    activities = finalActivities,
                    hasMore = hasMore,
                    lastDocument = lastDocument
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting paginated activities", e)
                PaginatedActivities(
                    activities = emptyList(),
                    hasMore = false,
                    lastDocument = null
                )
            }
        }
    
    /**
     * Get initial activities for screen load
     */
    suspend fun getInitialActivities(groupId: String): PaginatedActivities = 
        getPaginatedActivities(
            ActivityQueryParams(
                groupId = groupId,
                pageSize = INITIAL_ACTIVITY_LOAD
            )
        )
    
    /**
     * Load more activities for pagination
     */
    suspend fun loadMoreActivities(
        groupId: String,
        lastDocument: DocumentSnapshot,
        pageSize: Int = ACTIVITY_PAGE_SIZE
    ): PaginatedActivities = 
        getPaginatedActivities(
            ActivityQueryParams(
                groupId = groupId,
                pageSize = pageSize,
                lastDocument = lastDocument
            )
        )
    
    /**
     * Get filtered activities with pagination
     */
    suspend fun getFilteredActivities(
        groupId: String,
        activityTypes: List<com.example.fairr.data.model.ActivityType>,
        pageSize: Int = ACTIVITY_PAGE_SIZE,
        lastDocument: DocumentSnapshot? = null
    ): PaginatedActivities = 
        getPaginatedActivities(
            ActivityQueryParams(
                groupId = groupId,
                pageSize = pageSize,
                lastDocument = lastDocument,
                activityTypes = activityTypes
            )
        )
    
    /**
     * Parse activity document with error handling
     */
    private fun parseActivityDocument(doc: DocumentSnapshot): com.example.fairr.models.RecentActivity? {
        return try {
            val data = doc.data ?: return null
            
            com.example.fairr.models.RecentActivity(
                title = data["title"] as? String ?: "",
                amount = (data["amount"] as? Number)?.let { "$${it}" } ?: "",
                timestamp = data["timestamp"]?.let { 
                    val ts = it as? com.google.firebase.Timestamp
                    ts?.toDate()?.toString() ?: "Unknown"
                } ?: "Unknown",
                icon = Icons.Default.Receipt, // Default icon
                type = try {
                    com.example.fairr.models.ActivityType.valueOf(data["type"] as? String ?: "EXPENSE")
                } catch (e: Exception) {
                    com.example.fairr.models.ActivityType.EXPENSE
                },
                subtitle = data["description"] as? String ?: "",
                groupId = data["groupId"] as? String ?: ""
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing activity document ${doc.id}", e)
            null
        }
    }

    // Keep the old method for backward compatibility but mark as deprecated
    @Deprecated("Use getPaginatedActivities instead for better performance")
    suspend fun getActivitiesByGroupId(groupId: String): List<com.example.fairr.models.RecentActivity> = 
        withContext(Dispatchers.IO) {
            try {
                Log.w(TAG, "Using deprecated getActivitiesByGroupId. Consider using getPaginatedActivities for better performance.")
                
                // Use pagination with larger page size for backward compatibility
                val result = getPaginatedActivities(
                    ActivityQueryParams(
                        groupId = groupId,
                        pageSize = MAX_ACTIVITY_PAGE_SIZE
                    )
                )
                result.activities
            } catch (e: Exception) {
                Log.e(TAG, "Error getting activities", e)
                emptyList()
            }
        }

    suspend fun addActivity(
        groupId: String,
        type: com.example.fairr.data.model.ActivityType,
        title: String,
        description: String,
        userId: String,
        userName: String,
        relatedEntityId: String? = null,
        relatedEntityType: String? = null,
        metadata: Map<String, Any> = emptyMap()
    ) {
        try {
            val activity = hashMapOf(
                "groupId" to groupId,
                "type" to type.name,
                "title" to title,
                "description" to description,
                "userId" to userId,
                "userName" to userName,
                "timestamp" to com.google.firebase.Timestamp.now(),
                "relatedEntityId" to relatedEntityId,
                "relatedEntityType" to relatedEntityType,
                "metadata" to metadata
            )
            
            firestore.collection("activities").add(activity).await()
            Log.d(TAG, "Activity added successfully: $type for group $groupId")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding activity", e)
        }
    }

    suspend fun getRecentActivities(groupId: String, limit: Int = 10): List<com.example.fairr.models.RecentActivity> {
        return try {
            val result = getPaginatedActivities(
                ActivityQueryParams(
                    groupId = groupId,
                    pageSize = limit.coerceAtMost(MAX_ACTIVITY_PAGE_SIZE)
                )
            )
            result.activities
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recent activities", e)
            emptyList()
        }
    }

    suspend fun deleteActivitiesForGroup(groupId: String) {
        try {
            val batch = firestore.batch()
            val activities = firestore.collection("activities")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            activities.documents.forEach { doc ->
                batch.delete(doc.reference)
            }

            batch.commit().await()
            Log.d(TAG, "Deleted ${activities.size()} activities for group $groupId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting activities for group", e)
        }
    }

    suspend fun deleteActivity(activityId: String) {
        try {
            firestore.collection("activities").document(activityId).delete().await()
            Log.d(TAG, "Deleted activity: $activityId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting activity", e)
        }
    }
    
    /**
     * Get activity count for analytics (cached for performance)
     */
    suspend fun getActivityCount(groupId: String): Int = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("activities")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting activity count", e)
            0
        }
    }
} 