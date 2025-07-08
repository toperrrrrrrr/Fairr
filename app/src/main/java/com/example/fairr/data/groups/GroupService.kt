package com.example.fairr.data.groups

import android.util.Log
import com.example.fairr.ui.model.CreateGroupData
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember
import com.example.fairr.data.model.GroupRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.firestore.FieldValue
import kotlin.random.Random

private const val TAG = "GroupService"

sealed class GroupResult {
    data class Success(val groupId: String) : GroupResult()
    data class Error(val message: String) : GroupResult()
}

@Singleton
class GroupService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val groupsCollection = firestore.collection("groups")

    private fun generateInviteCode(): String {
        return Random.nextInt(100000, 999999).toString()
    }

    // Firestore returns generic Map<String, Any>; safe cast with try/catch for member parsing
    @Suppress("UNCHECKED_CAST")
    private fun parseGroupData(data: Map<String, Any>): Map<String, Map<String, Any>> {
        return try {
            data["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing group data", e)
            emptyMap()
        }
    }

    private fun createGroupMember(
        name: String,
        email: String,
        isAdmin: Boolean
    ): Map<String, Any> {
        return mapOf(
            "name" to name,
            "email" to email,
            "isAdmin" to isAdmin,
            "joinedAt" to Timestamp.now()
        )
    }

    fun getUserGroups(): Flow<List<Group>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "Fetching groups for user: ${currentUser.uid}")

        // Query groups where the current user is a member
        val subscription = groupsCollection
            .whereArrayContains("memberIds", currentUser.uid)  // Add memberIds array for efficient querying
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching groups", error)
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.d(TAG, "No groups found (snapshot is null)")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                Log.d(TAG, "Number of documents: ${snapshot.documents.size}")
                
                val groups = snapshot.documents.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Log.d(TAG, "Processing group document: ${doc.id}")
                        Log.d(TAG, "Group data: $data")

                        Group(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            currency = data["currency"] as? String ?: "PHP",
                            createdAt = (data["createdAt"] as? Timestamp) ?: Timestamp.now(),
                            createdBy = data["createdBy"] as? String ?: "",
                            inviteCode = data["inviteCode"] as? String ?: "",
                            members = parseGroupData(data).map { (userId, memberData) ->
                                GroupMember(
                                    userId = userId,
                                    name = memberData["name"] as? String ?: "Unknown",
                                    email = memberData["email"] as? String ?: "",
                                    role = if (memberData["isAdmin"] as? Boolean == true) GroupRole.ADMIN else GroupRole.MEMBER,
                                    joinedAt = memberData["joinedAt"] as? Timestamp ?: Timestamp.now()
                                )
                            }
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing group document", e)
                        null
                    }
                }
                Log.d(TAG, "Processed ${groups.size} groups")
                trySend(groups)
            }

        awaitClose { subscription.remove() }
    }

    fun getGroupById(groupId: String): Flow<Group> = callbackFlow {
        if (auth.currentUser == null) {
            throw IllegalStateException("User not authenticated")
        }

        val subscription = groupsCollection.document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    close(IllegalStateException("Group not found"))
                    return@addSnapshotListener
                }

                try {
                    val data = snapshot.data ?: throw IllegalStateException("Group data is null")
                    
                    val group = Group(
                        id = snapshot.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        currency = data["currency"] as? String ?: "PHP",
                        createdAt = (data["createdAt"] as? Timestamp) ?: Timestamp.now(),
                        createdBy = data["createdBy"] as? String ?: "",
                        inviteCode = data["inviteCode"] as? String ?: "",
                        members = parseGroupData(data).map { (userId, memberData) ->
                            GroupMember(
                                userId = userId,
                                name = memberData["name"] as? String ?: "Unknown",
                                email = memberData["email"] as? String ?: "",
                                role = if (memberData["isAdmin"] as? Boolean == true) GroupRole.ADMIN else GroupRole.MEMBER,
                                joinedAt = memberData["joinedAt"] as? Timestamp ?: Timestamp.now()
                            )
                        }
                    )

                    trySend(group)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing group document", e)
                    close(e)
                }
            }

        awaitClose { subscription.remove() }
    }

    fun getGroup(groupId: String): Flow<Group> = callbackFlow {
        if (auth.currentUser == null) {
            throw IllegalStateException("User not authenticated")
        }

        val subscription = groupsCollection.document(groupId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    close(IllegalStateException("Group not found"))
                    return@addSnapshotListener
                }

                try {
                    val data = snapshot.data ?: throw IllegalStateException("Group data is null")
                    
                    val group = Group(
                        id = snapshot.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        currency = data["currency"] as? String ?: "PHP",
                        createdAt = (data["createdAt"] as? Timestamp) ?: Timestamp.now(),
                        createdBy = data["createdBy"] as? String ?: "",
                        inviteCode = data["inviteCode"] as? String ?: "",
                        members = parseGroupData(data).map { (userId, memberData) ->
                            GroupMember(
                                userId = userId,
                                name = memberData["name"] as? String ?: "Unknown",
                                email = memberData["email"] as? String ?: "",
                                role = if (memberData["isAdmin"] as? Boolean == true) GroupRole.ADMIN else GroupRole.MEMBER,
                                joinedAt = memberData["joinedAt"] as? Timestamp ?: Timestamp.now()
                            )
                        }
                    )

                    trySend(group)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing group document", e)
                    close(e)
                }
            }

        awaitClose { subscription.remove() }
    }

    suspend fun createGroup(data: CreateGroupData): GroupResult {
        val currentUser = auth.currentUser
            ?: return GroupResult.Error("User not authenticated")

        val groupId = groupsCollection.document().id
        val inviteCode = generateInviteCode()
        val timestamp = Timestamp.now()

        val groupData = mapOf(
            "name" to data.name,
            "description" to data.description,
            "currency" to data.currency,
            "avatar" to data.avatar,
            "avatarType" to data.avatarType,
            "createdAt" to timestamp,
            "createdBy" to currentUser.uid,
            "inviteCode" to inviteCode,
            "memberIds" to listOf(currentUser.uid),  // Initialize with creator as member
            "members" to mapOf(
                currentUser.uid to createGroupMember(
                    name = currentUser.displayName ?: "Unknown",
                    email = currentUser.email ?: "",
                    isAdmin = true
                )
            )
        )

        return try {
            groupsCollection.document(groupId).set(groupData).await()
            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating group", e)
            GroupResult.Error(e.message ?: "Failed to create group")
        }
    }

    suspend fun joinGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data ?: emptyMap()
            val membersMap = parseGroupData(data)
            if (membersMap.containsKey(currentUser.uid)) {
                return GroupResult.Error("You are already a member of this group")
            }

            val newMember = createGroupMember(
                currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown",
                currentUser.email ?: "",
                false
            )

            // Get existing memberIds array or create new one
            val memberIds = (data["memberIds"] as? List<String> ?: emptyList()) + currentUser.uid

            // Update both members map and memberIds array
            groupsCollection.document(groupId)
                .update(
                    mapOf(
                        "members.${currentUser.uid}" to newMember,
                        "memberIds" to memberIds
                    )
                )
                .await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            GroupResult.Error(e.message ?: "Failed to join group")
        }
    }

    suspend fun getGroupMembers(groupId: String): Flow<List<GroupMember>> = callbackFlow {
        try {
            val membersRef = firestore.collection("groups").document(groupId)
                .collection("members")
            
            val subscription = membersRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val members = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        GroupMember(
                            userId = doc.id,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: "",
                            role = if (doc.getBoolean("isAdmin") == true) GroupRole.ADMIN else GroupRole.MEMBER,
                            joinedAt = doc.getTimestamp("joinedAt") ?: Timestamp.now()
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing member document", e)
                        null
                    }
                } ?: emptyList()

                trySend(members)
            }

            awaitClose { subscription.remove() }
        } catch (e: Exception) {
            close(e)
        }
    }

    suspend fun deleteGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            // Get the group to check permissions
            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data
                ?: return GroupResult.Error("Group data not found")

            // Check if current user is admin
            val membersMap = parseGroupData(data)
            val currentUserData = membersMap[currentUser.uid]
                ?: return GroupResult.Error("You are not a member of this group")

            if (!(currentUserData["isAdmin"] as? Boolean ?: false)) {
                return GroupResult.Error("Only admins can delete the group")
            }

            // Delete all expenses for this group
            val expensesSnapshot = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()

            // Delete expenses in batches
            val batch = firestore.batch()
            expensesSnapshot.documents.forEach { doc ->
                batch.delete(firestore.collection("expenses").document(doc.id))
            }

            // Delete the group
            batch.delete(groupsCollection.document(groupId))

            // Commit the batch
            batch.commit().await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting group", e)
            GroupResult.Error(e.message ?: "Failed to delete group")
        }
    }

    /**
     * Current user leaves the specified group. Fails if they are the last admin and other members remain.
     */
    suspend fun leaveGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser ?: return GroupResult.Error("User not authenticated")

            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data ?: return GroupResult.Error("Group data missing")
            val membersMap = parseGroupData(data)

            val currentMember = membersMap[currentUser.uid] ?: return GroupResult.Error("You are not a member of this group")

            val isAdmin = currentMember["isAdmin"] as? Boolean ?: false
            if (isAdmin) {
                val adminCount = membersMap.values.count { (it["isAdmin"] as? Boolean) == true }
                if (adminCount <= 1 && membersMap.size > 1) {
                    return GroupResult.Error("Cannot leave group as the last admin. Transfer admin rights or delete the group.")
                }
            }

            // Build updates to remove member from map and array
            val updates = hashMapOf<String, Any>(
                "members.${currentUser.uid}" to FieldValue.delete(),
                "memberIds" to FieldValue.arrayRemove(currentUser.uid)
            )

            groupsCollection.document(groupId).update(updates).await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error leaving group", e)
            GroupResult.Error(e.message ?: "Failed to leave group")
        }
    }

    /**
     * Admin removes another member from the group.
     */
    suspend fun removeMember(groupId: String, memberId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser ?: return GroupResult.Error("User not authenticated")

            // Fetch group
            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) return GroupResult.Error("Group not found")

            val data = groupDoc.data ?: return GroupResult.Error("Group data missing")
            val membersMap = parseGroupData(data)

            val currentMember = membersMap[currentUser.uid] ?: return GroupResult.Error("You are not a member of this group")
            val isAdmin = currentMember["isAdmin"] as? Boolean ?: false
            if (!isAdmin) return GroupResult.Error("Only admins can remove members")

            if (!membersMap.containsKey(memberId)) return GroupResult.Error("Member not found")

            // Prevent removing self via this method
            if (memberId == currentUser.uid) return GroupResult.Error("Use leave group to remove yourself")

            // If removing an admin, ensure at least one admin remains
            val targetIsAdmin = membersMap[memberId]?.get("isAdmin") as? Boolean ?: false
            if (targetIsAdmin) {
                val adminCount = membersMap.values.count { (it["isAdmin"] as? Boolean) == true }
                if (adminCount <= 1) {
                    return GroupResult.Error("Cannot remove the last admin of the group")
                }
            }

            val updates = hashMapOf<String, Any>(
                "members.$memberId" to FieldValue.delete(),
                "memberIds" to FieldValue.arrayRemove(memberId)
            )
            groupsCollection.document(groupId).update(updates).await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing member", e)
            GroupResult.Error(e.message ?: "Failed to remove member")
        }
    }

    /**
     * Admin updates group information (name, description, currency).
     */
    suspend fun updateGroup(groupId: String, name: String, description: String, currency: String): GroupResult {
        return try {
            val currentUser = auth.currentUser ?: return GroupResult.Error("User not authenticated")

            // Fetch group to check permissions
            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) return GroupResult.Error("Group not found")

            val data = groupDoc.data ?: return GroupResult.Error("Group data missing")
            val membersMap = parseGroupData(data)

            val currentMember = membersMap[currentUser.uid] ?: return GroupResult.Error("You are not a member of this group")
            val isAdmin = currentMember["isAdmin"] as? Boolean ?: false
            if (!isAdmin) return GroupResult.Error("Only admins can update group information")

            // Validate input
            if (name.isBlank()) return GroupResult.Error("Group name cannot be empty")
            if (currency.isBlank()) return GroupResult.Error("Currency cannot be empty")

            // Update group information
            val updates = hashMapOf<String, Any>(
                "name" to name.trim(),
                "description" to description.trim(),
                "currency" to currency.trim()
            )

            groupsCollection.document(groupId).update(updates).await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating group", e)
            GroupResult.Error(e.message ?: "Failed to update group")
        }
    }

    suspend fun promoteToAdmin(groupId: String, userId: String): GroupResult {
        return try {
            val groupDoc = groupsCollection.document(groupId)
            val memberField = "members.$userId.isAdmin"
            groupDoc.update(memberField, true).await()
            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error promoting member to admin", e)
            GroupResult.Error(e.message ?: "Failed to promote member to admin")
        }
    }

    suspend fun demoteFromAdmin(groupId: String, userId: String): GroupResult {
        return try {
            val groupDoc = groupsCollection.document(groupId)
            val memberField = "members.$userId.isAdmin"
            groupDoc.update(memberField, false).await()
            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error demoting member from admin", e)
            GroupResult.Error(e.message ?: "Failed to demote member from admin")
        }
    }

    /**
     * Archive a group (admin only)
     */
    suspend fun archiveGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            // Check if current user is admin
            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data
                ?: return GroupResult.Error("Group data not found")

            val membersMap = parseGroupData(data)
            val currentUserData = membersMap[currentUser.uid]
                ?: return GroupResult.Error("You are not a member of this group")

            if (!(currentUserData["isAdmin"] as? Boolean ?: false)) {
                return GroupResult.Error("Only admins can archive the group")
            }

            // Update the group to be archived
            groupsCollection.document(groupId)
                .update("isArchived", true)
                .await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error archiving group", e)
            GroupResult.Error(e.message ?: "Failed to archive group")
        }
    }

    /**
     * Unarchive a group (admin only)
     */
    suspend fun unarchiveGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            // Check if current user is admin
            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data
                ?: return GroupResult.Error("Group data not found")

            val membersMap = parseGroupData(data)
            val currentUserData = membersMap[currentUser.uid]
                ?: return GroupResult.Error("You are not a member of this group")

            if (!(currentUserData["isAdmin"] as? Boolean ?: false)) {
                return GroupResult.Error("Only admins can unarchive the group")
            }

            // Update the group to be unarchived
            groupsCollection.document(groupId)
                .update("isArchived", false)
                .await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error unarchiving group", e)
            GroupResult.Error(e.message ?: "Failed to unarchive group")
        }
    }
} 