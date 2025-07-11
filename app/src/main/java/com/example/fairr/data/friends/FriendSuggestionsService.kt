package com.example.fairr.data.friends

import com.example.fairr.ui.model.Friend
import com.example.fairr.ui.model.FriendStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class FriendSuggestion(
    val userId: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val mutualGroups: List<String> = emptyList(),
    val mutualFriends: List<String> = emptyList(),
    val suggestionReason: String,
    val score: Int // Higher score = better suggestion
)

@Singleton
class FriendSuggestionsService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Get friend suggestions based on mutual groups and mutual friends
     */
    fun getFriendSuggestions(): Flow<List<FriendSuggestion>> = flow {
        val currentUser = auth.currentUser ?: return@flow
        
        try {
            // Get current users groups
            val userGroups = getUserGroups(currentUser.uid)
            
            // Get current users friends
            val userFriends = getUserFriends(currentUser.uid)
            
            // Get existing friend requests to exclude them
            val existingRequests = getExistingFriendRequests(currentUser.uid)
            
            val suggestions = mutableListOf<FriendSuggestion>()
            
            // Find people in mutual groups
            userGroups.forEach { groupId ->
                val groupMembers = getGroupMembers(groupId)
                groupMembers.forEach { member ->
                    if (member.userId != currentUser.uid && 
                        !userFriends.contains(member.userId) && 
                        !existingRequests.contains(member.userId)) {
                        
                        val mutualGroups = getUserGroups(member.userId).intersect(userGroups).toList()
                        val mutualFriends = getUserFriends(member.userId).intersect(userFriends).toList()
                        
                        val score = calculateSuggestionScore(mutualGroups.size, mutualFriends.size)
                        val reason = buildSuggestionReason(mutualGroups.size, mutualFriends.size)
                        
                        if (score > 0) {
                            suggestions.add(
                                FriendSuggestion(
                                    userId = member.userId,
                                    name = member.name,
                                    email = member.email,
                                    photoUrl = member.photoUrl,
                                    mutualGroups = mutualGroups,
                                    mutualFriends = mutualFriends,
                                    suggestionReason = reason,
                                    score = score
                                )
                            )
                        }
                    }
                }
            }
            
            // Sort by score and remove duplicates
            val uniqueSuggestions = suggestions
                .distinctBy { it.userId }
                .sortedByDescending { it.score }
                .take(10)
            
            emit(uniqueSuggestions)
            
        } catch (e: Exception) {
            android.util.Log.e("FriendSuggestionsService", "Error getting suggestions", e)
            emit(emptyList())
        }
    }
    
    private suspend fun getUserGroups(userId: String): Set<String> {
        return try {
            val groupsSnapshot = firestore.collection("groups")
                .whereArrayContains("memberIds", userId)
                .get()
                .await()
            
            groupsSnapshot.documents.map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    private suspend fun getUserFriends(userId: String): Set<String> {
        return try {
            val friendsAsUser = firestore.collection("friends")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", FriendStatus.ACCEPTED.name)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("friendId") }
            
            val friendsAsFriend = firestore.collection("friends")
                .whereEqualTo("friendId", userId)
                .whereEqualTo("status", FriendStatus.ACCEPTED.name)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("userId") }
            
            (friendsAsUser + friendsAsFriend).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    private suspend fun getExistingFriendRequests(userId: String): Set<String> {
        return try {
            val sentRequests = firestore.collection("friendRequests")
                .whereEqualTo("senderId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("receiverId") }
            
            val receivedRequests = firestore.collection("friendRequests")
                .whereEqualTo("receiverId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("senderId") }
            
            (sentRequests + receivedRequests).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    private suspend fun getGroupMembers(groupId: String): List<GroupMember> {
        return try {
            val groupDoc = firestore.collection("groups")
                .document(groupId)
                .get()
                .await()
            
            val members = groupDoc.get("members") as? Map<String, Map<String, Any>> ?: emptyMap()
            
            members.map { (userId, memberData) ->
                GroupMember(
                    userId = userId,
                    name = memberData["name"] as? String ?: "Unknown",
                    email = memberData["email"] as? String ?: "",
                    photoUrl = memberData["photoUrl"] as? String
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun calculateSuggestionScore(mutualGroups: Int, mutualFriends: Int): Int {
        return (mutualGroups * 10) + (mutualFriends * 5)
    }
    
    private fun buildSuggestionReason(mutualGroups: Int, mutualFriends: Int): String {
        return when {
            mutualGroups > 0 && mutualFriends > 0 -> 
                "\$mutualGroups mutual groups, \$mutualFriends mutual friends"
            mutualGroups > 0 -> 
                "\$mutualGroups mutual group\${if (mutualGroups > 1) \"s\" else \"\"}"
            mutualFriends > 0 -> 
                "\$mutualFriends mutual friend\${if (mutualFriends > 1) \"s\" else \"\"}"
            else -> "Suggested for you"
        }
    }
    
    private data class GroupMember(
        val userId: String,
        val name: String,
        val email: String,
        val photoUrl: String? = null
    )
}
