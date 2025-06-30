package com.example.fairr.data.friends

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

private const val TAG = "FriendGroupService"

sealed class FriendGroupResult {
    data class Success(val message: String, val groupId: String? = null) : FriendGroupResult()
    data class Error(val message: String) : FriendGroupResult()
}

@Singleton
class FriendGroupService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val friendGroupsCollection = firestore.collection("friendGroups")
    private val friendGroupMembershipsCollection = firestore.collection("friendGroupMemberships")

    /**
     * Create default friend groups for a new user
     */
    suspend fun createDefaultGroups(): FriendGroupResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return FriendGroupResult.Error("User not authenticated")

            // Check if default groups already exist
            val existingGroups = friendGroupsCollection
                .whereEqualTo("createdBy", currentUser.uid)
                .whereEqualTo("isDefault", true)
                .get()
                .await()

            if (!existingGroups.isEmpty) {
                return FriendGroupResult.Success("Default groups already exist")
            }

            // Create default groups
            DefaultFriendGroupType.values().forEach { defaultType ->
                val groupId = friendGroupsCollection.document().id
                val friendGroup = FriendGroup(
                    id = groupId,
                    name = defaultType.displayName,
                    description = "Default ${defaultType.displayName.lowercase()} group",
                    color = defaultType.color,
                    emoji = defaultType.emoji,
                    createdBy = currentUser.uid,
                    createdAt = Timestamp.now(),
                    memberIds = emptyList(),
                    isDefault = true
                )

                val groupData = mapOf(
                    "name" to friendGroup.name,
                    "description" to friendGroup.description,
                    "color" to friendGroup.color,
                    "emoji" to friendGroup.emoji,
                    "createdBy" to friendGroup.createdBy,
                    "createdAt" to friendGroup.createdAt,
                    "memberIds" to friendGroup.memberIds,
                    "isDefault" to friendGroup.isDefault
                )

                friendGroupsCollection.document(groupId).set(groupData).await()
            }

            Log.d(TAG, "Default friend groups created successfully")
            FriendGroupResult.Success("Default groups created successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error creating default groups", e)
            FriendGroupResult.Error(e.message ?: "Failed to create default groups")
        }
    }

    /**
     * Create a custom friend group
     */
    suspend fun createFriendGroup(
        name: String,
        description: String,
        color: String,
        emoji: String
    ): FriendGroupResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return FriendGroupResult.Error("User not authenticated")

            if (name.isBlank()) {
                return FriendGroupResult.Error("Group name cannot be empty")
            }

            val groupId = friendGroupsCollection.document().id
            val friendGroup = FriendGroup(
                id = groupId,
                name = name.trim(),
                description = description.trim(),
                color = color,
                emoji = emoji,
                createdBy = currentUser.uid,
                createdAt = Timestamp.now(),
                memberIds = emptyList(),
                isDefault = false
            )

            val groupData = mapOf(
                "name" to friendGroup.name,
                "description" to friendGroup.description,
                "color" to friendGroup.color,
                "emoji" to friendGroup.emoji,
                "createdBy" to friendGroup.createdBy,
                "createdAt" to friendGroup.createdAt,
                "memberIds" to friendGroup.memberIds,
                "isDefault" to friendGroup.isDefault
            )

            friendGroupsCollection.document(groupId).set(groupData).await()
            
            Log.d(TAG, "Friend group created: $name")
            FriendGroupResult.Success("Group created successfully", groupId)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating friend group", e)
            FriendGroupResult.Error(e.message ?: "Failed to create group")
        }
    }

    /**
     * Get all friend groups for current user
     */
    fun getFriendGroups(): Flow<List<FriendGroup>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = friendGroupsCollection
            .whereEqualTo("createdBy", currentUser.uid)
            .orderBy("createdAt")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        FriendGroup(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            color = data["color"] as? String ?: "#4A90E2",
                            emoji = data["emoji"] as? String ?: "👥",
                            createdBy = data["createdBy"] as? String ?: "",
                            createdAt = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                            memberIds = (data["memberIds"] as? List<String>) ?: emptyList(),
                            isDefault = data["isDefault"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing friend group", e)
                        null
                    }
                } ?: emptyList()

                trySend(groups)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Add friend to group
     */
    suspend fun addFriendToGroup(friendId: String, groupId: String): FriendGroupResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return FriendGroupResult.Error("User not authenticated")

            // Check if group exists and user owns it
            val groupDoc = friendGroupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return FriendGroupResult.Error("Group not found")
            }

            val groupData = groupDoc.data ?: return FriendGroupResult.Error("Group data not found")
            val groupCreatedBy = groupData["createdBy"] as? String

            if (groupCreatedBy != currentUser.uid) {
                return FriendGroupResult.Error("You don't have permission to modify this group")
            }

            // Check if friend is already in group
            val currentMemberIds = (groupData["memberIds"] as? List<String>) ?: emptyList()
            if (currentMemberIds.contains(friendId)) {
                return FriendGroupResult.Error("Friend is already in this group")
            }

            // Add membership record
            val membershipId = friendGroupMembershipsCollection.document().id
            val membership = FriendGroupMembership(
                id = membershipId,
                friendId = friendId,
                groupId = groupId,
                addedAt = Timestamp.now(),
                addedBy = currentUser.uid
            )

            val membershipData = mapOf(
                "friendId" to membership.friendId,
                "groupId" to membership.groupId,
                "addedAt" to membership.addedAt,
                "addedBy" to membership.addedBy
            )

            friendGroupMembershipsCollection.document(membershipId).set(membershipData).await()

            // Update group's member list
            val updatedMemberIds = currentMemberIds + friendId
            friendGroupsCollection.document(groupId)
                .update("memberIds", updatedMemberIds)
                .await()

            Log.d(TAG, "Friend added to group successfully")
            FriendGroupResult.Success("Friend added to group")

        } catch (e: Exception) {
            Log.e(TAG, "Error adding friend to group", e)
            FriendGroupResult.Error(e.message ?: "Failed to add friend to group")
        }
    }

    /**
     * Remove friend from group
     */
    suspend fun removeFriendFromGroup(friendId: String, groupId: String): FriendGroupResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return FriendGroupResult.Error("User not authenticated")

            // Check if group exists and user owns it
            val groupDoc = friendGroupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return FriendGroupResult.Error("Group not found")
            }

            val groupData = groupDoc.data ?: return FriendGroupResult.Error("Group data not found")
            val groupCreatedBy = groupData["createdBy"] as? String

            if (groupCreatedBy != currentUser.uid) {
                return FriendGroupResult.Error("You don't have permission to modify this group")
            }

            // Remove membership record
            val membershipQuery = friendGroupMembershipsCollection
                .whereEqualTo("friendId", friendId)
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            membershipQuery.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Update group's member list
            val currentMemberIds = (groupData["memberIds"] as? List<String>) ?: emptyList()
            val updatedMemberIds = currentMemberIds.filter { it != friendId }
            
            friendGroupsCollection.document(groupId)
                .update("memberIds", updatedMemberIds)
                .await()

            Log.d(TAG, "Friend removed from group successfully")
            FriendGroupResult.Success("Friend removed from group")

        } catch (e: Exception) {
            Log.e(TAG, "Error removing friend from group", e)
            FriendGroupResult.Error(e.message ?: "Failed to remove friend from group")
        }
    }

    /**
     * Update friend group
     */
    suspend fun updateFriendGroup(
        groupId: String,
        name: String,
        description: String,
        color: String,
        emoji: String
    ): FriendGroupResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return FriendGroupResult.Error("User not authenticated")

            if (name.isBlank()) {
                return FriendGroupResult.Error("Group name cannot be empty")
            }

            // Check if group exists and user owns it
            val groupDoc = friendGroupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return FriendGroupResult.Error("Group not found")
            }

            val groupData = groupDoc.data ?: return FriendGroupResult.Error("Group data not found")
            val groupCreatedBy = groupData["createdBy"] as? String

            if (groupCreatedBy != currentUser.uid) {
                return FriendGroupResult.Error("You don't have permission to modify this group")
            }

            val updates = mapOf(
                "name" to name.trim(),
                "description" to description.trim(),
                "color" to color,
                "emoji" to emoji
            )

            friendGroupsCollection.document(groupId).update(updates).await()
            
            Log.d(TAG, "Friend group updated: $name")
            FriendGroupResult.Success("Group updated successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error updating friend group", e)
            FriendGroupResult.Error(e.message ?: "Failed to update group")
        }
    }

    /**
     * Delete friend group
     */
    suspend fun deleteFriendGroup(groupId: String): FriendGroupResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return FriendGroupResult.Error("User not authenticated")

            // Check if group exists and user owns it
            val groupDoc = friendGroupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return FriendGroupResult.Error("Group not found")
            }

            val groupData = groupDoc.data ?: return FriendGroupResult.Error("Group data not found")
            val groupCreatedBy = groupData["createdBy"] as? String
            val isDefault = groupData["isDefault"] as? Boolean ?: false

            if (groupCreatedBy != currentUser.uid) {
                return FriendGroupResult.Error("You don't have permission to delete this group")
            }

            if (isDefault) {
                return FriendGroupResult.Error("Cannot delete default groups")
            }

            // Delete all memberships for this group
            val memberships = friendGroupMembershipsCollection
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            memberships.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            // Delete the group
            friendGroupsCollection.document(groupId).delete().await()
            
            Log.d(TAG, "Friend group deleted successfully")
            FriendGroupResult.Success("Group deleted successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting friend group", e)
            FriendGroupResult.Error(e.message ?: "Failed to delete group")
        }
    }

    /**
     * Get friends in a specific group
     */
    fun getFriendsInGroup(groupId: String): Flow<List<String>> = callbackFlow {
        val subscription = friendGroupMembershipsCollection
            .whereEqualTo("groupId", groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val friendIds = snapshot?.documents?.mapNotNull { doc ->
                    doc.getString("friendId")
                } ?: emptyList()

                trySend(friendIds)
            }

        awaitClose { subscription.remove() }
    }
} 