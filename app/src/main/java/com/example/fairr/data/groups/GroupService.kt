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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
        return UUID.randomUUID().toString().substring(0, 6).uppercase()
    }

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
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

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
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

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
                    val currentUserId = currentUser.uid
                    
                    val group = Group(
                        id = snapshot.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        currency = data["currency"] as? String ?: "USD",
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

    suspend fun createGroup(groupData: CreateGroupData): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            Log.d(TAG, "Creating group with name: ${groupData.name}")
            
            val groupId = UUID.randomUUID().toString()
            
            // Create a map of members with user IDs as keys
            val membersMap = mutableMapOf<String, Map<String, Any>>()
            val memberIds = mutableListOf<String>()
            
            // Add current user as admin
            val currentUserData = createGroupMember(
                currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown",
                currentUser.email ?: "",
                true
            )
            membersMap[currentUser.uid] = currentUserData
            memberIds.add(currentUser.uid)
            
            Log.d(TAG, "Current user data: $currentUserData")
            
            // Add other members
            groupData.members.forEach { member ->
                val memberData = createGroupMember(member.name, member.email, member.isAdmin)
                membersMap[member.id] = memberData
                memberIds.add(member.id)
            }

            val groupDocument = hashMapOf(
                "name" to groupData.name,
                "description" to groupData.description,
                "currency" to groupData.currency,
                "members" to membersMap,
                "memberIds" to memberIds,  // Add memberIds array for querying
                "createdBy" to currentUser.uid,
                "createdAt" to Timestamp.now(),
                "inviteCode" to generateInviteCode()
            )

            Log.d(TAG, "Creating group document: $groupDocument")

            groupsCollection.document(groupId)
                .set(groupDocument)
                .await()

            Log.d(TAG, "Successfully created group with ID: $groupId")
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

            val membersMap = parseGroupData(groupDoc.data ?: emptyMap())
            if (membersMap.containsKey(currentUser.uid)) {
                return GroupResult.Error("You are already a member of this group")
            }

            val newMember = createGroupMember(
                currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown",
                currentUser.email ?: "",
                false
            )

            groupsCollection.document(groupId)
                .update("members", membersMap + newMember)
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
} 