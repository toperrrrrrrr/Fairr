package com.example.fairr.data.friends

import com.example.fairr.data.model.FriendActivity
import com.example.fairr.data.model.FriendActivityType
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FriendActivityService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Log a friend activity
     */
    suspend fun logFriendActivity(
        friendId: String,
        friendName: String,
        type: FriendActivityType,
        title: String,
        description: String,
        amount: Double? = null,
        groupId: String? = null,
        groupName: String? = null
    ) {
        val currentUser = auth.currentUser ?: return
        
        try {
            // Get current users display name
            val userDoc = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()
            
            val userName = userDoc.getString("displayName") 
                ?: currentUser.email?.substringBefore("@") 
                ?: "Unknown User"
            
            val userInitials = userName.split(" ")
                .take(2)
                .joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
            
            val activity = FriendActivity(
                userId = currentUser.uid,
                userName = userName,
                userInitials = userInitials,
                friendId = friendId,
                friendName = friendName,
                type = type,
                title = title,
                description = description,
                timestamp = Timestamp.now(),
                amount = amount,
                groupId = groupId,
                groupName = groupName
            )
            
            // Add to users friend activities
            firestore.collection("users")
                .document(currentUser.uid)
                .collection("friendActivities")
                .add(activity)
                .await()
                
        } catch (e: Exception) {
            // Log error but dont throw - friend activities are not critical
            android.util.Log.e("FriendActivityService", "Failed to log friend activity", e)
        }
    }
    
    /**
     * Get friend activities for the current user
     */
    fun getFriendActivities(): Flow<List<FriendActivity>> = flow {
        val currentUser = auth.currentUser ?: return@flow
        
        try {
            val activitiesSnapshot = firestore.collection("users")
                .document(currentUser.uid)
                .collection("friendActivities")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()
            
            val activities = activitiesSnapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    FriendActivity(
                        id = doc.id,
                        userId = data["userId"] as? String ?: "",
                        userName = data["userName"] as? String ?: "",
                        userInitials = data["userInitials"] as? String ?: "",
                        friendId = data["friendId"] as? String ?: "",
                        friendName = data["friendName"] as? String ?: "",
                        type = try {
                            FriendActivityType.valueOf(data["type"] as? String ?: "")
                        } catch (e: Exception) {
                            FriendActivityType.FRIEND_ADDED
                        },
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        timestamp = data["timestamp"] as? Timestamp ?: Timestamp.now(),
                        amount = (data["amount"] as? Number)?.toDouble(),
                        groupId = data["groupId"] as? String,
                        groupName = data["groupName"] as? String
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FriendActivityService", "Error parsing activity", e)
                    null
                }
            }
            
            emit(activities)
        } catch (e: Exception) {
            android.util.Log.e("FriendActivityService", "Error loading friend activities", e)
            emit(emptyList())
        }
    }
}
