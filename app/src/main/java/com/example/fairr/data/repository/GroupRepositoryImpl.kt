package com.example.fairr.data.repository

import android.util.Log
import com.example.fairr.ui.model.CreateGroupData
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember
import com.example.fairr.data.model.GroupRole
import com.example.fairr.data.model.AvatarType
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

private const val TAG = "GroupRepositoryImpl"

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : GroupRepository {
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

    override fun getUserGroups(): Flow<List<Group>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "Fetching groups for user: ${currentUser.uid}")

        // Query groups where the current user is a member (including archived)
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
                            avatar = data["avatar"] as? String ?: "",
                            avatarType = if ((data["avatar"] as? String)?.isNotEmpty() == true) {
                                AvatarType.valueOf(data["avatarType"] as? String ?: AvatarType.EMOJI.name)
                            } else {
                                AvatarType.EMOJI
                            },
                            isArchived = data["isArchived"] as? Boolean ?: false,
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
                        Log.e(TAG, "Error parsing group document ${doc.id}", e)
                        null
                    }
                }

                Log.d(TAG, "Successfully parsed ${groups.size} groups")
                trySend(groups)
            }

        awaitClose { subscription.remove() }
    }

    override fun getActiveGroups(): Flow<List<Group>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        val subscription = groupsCollection
            .whereArrayContains("memberIds", currentUser.uid)
            .whereEqualTo("isArchived", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Group(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            currency = data["currency"] as? String ?: "PHP",
                            createdAt = (data["createdAt"] as? Timestamp) ?: Timestamp.now(),
                            createdBy = data["createdBy"] as? String ?: "",
                            inviteCode = data["inviteCode"] as? String ?: "",
                            avatar = data["avatar"] as? String ?: "",
                            avatarType = if ((data["avatar"] as? String)?.isNotEmpty() == true) {
                                AvatarType.valueOf(data["avatarType"] as? String ?: AvatarType.EMOJI.name)
                            } else {
                                AvatarType.EMOJI
                            },
                            isArchived = data["isArchived"] as? Boolean ?: false,
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
                } ?: emptyList()

                trySend(groups)
            }

        awaitClose { subscription.remove() }
    }

    override fun getArchivedGroups(): Flow<List<Group>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        val subscription = groupsCollection
            .whereArrayContains("memberIds", currentUser.uid)
            .whereEqualTo("isArchived", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val groups = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        Group(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            currency = data["currency"] as? String ?: "PHP",
                            createdAt = (data["createdAt"] as? Timestamp) ?: Timestamp.now(),
                            createdBy = data["createdBy"] as? String ?: "",
                            inviteCode = data["inviteCode"] as? String ?: "",
                            avatar = data["avatar"] as? String ?: "",
                            avatarType = if ((data["avatar"] as? String)?.isNotEmpty() == true) {
                                AvatarType.valueOf(data["avatarType"] as? String ?: AvatarType.EMOJI.name)
                            } else {
                                AvatarType.EMOJI
                            },
                            isArchived = data["isArchived"] as? Boolean ?: false,
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
                } ?: emptyList()

                trySend(groups)
            }

        awaitClose { subscription.remove() }
    }

    override fun getGroup(groupId: String): Flow<Group> = callbackFlow {
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
                        avatar = data["avatar"] as? String ?: "",
                        avatarType = if ((data["avatar"] as? String)?.isNotEmpty() == true) {
                            AvatarType.valueOf(data["avatarType"] as? String ?: AvatarType.EMOJI.name)
                        } else {
                            AvatarType.EMOJI
                        },
                        isArchived = data["isArchived"] as? Boolean ?: false,
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

    override suspend fun createGroup(data: CreateGroupData): GroupResult {
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

    override suspend fun joinGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data ?: return GroupResult.Error("Group data missing")
            val membersMap = parseGroupData(data)

            // Check if user is already a member
            if (membersMap.containsKey(currentUser.uid)) {
                return GroupResult.Error("You are already a member of this group")
            }

            // Add user to group
            val memberData = createGroupMember(
                name = currentUser.displayName ?: "Unknown",
                email = currentUser.email ?: "",
                isAdmin = false
            )

            val updates = hashMapOf<String, Any>(
                "members.${currentUser.uid}" to memberData,
                "memberIds" to FieldValue.arrayUnion(currentUser.uid)
            )

            groupsCollection.document(groupId).update(updates).await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error joining group", e)
            GroupResult.Error(e.message ?: "Failed to join group")
        }
    }

    override suspend fun leaveGroup(groupId: String): GroupResult {
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

            // Check if user is the last admin
            val adminCount = membersMap.values.count { it["isAdmin"] as? Boolean == true }
            if (isAdmin && adminCount == 1 && membersMap.size > 1) {
                return GroupResult.Error("Cannot leave group: You are the last admin and other members remain")
            }

            // Remove user from group
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

    override suspend fun deleteGroup(groupId: String): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            // Check if current user is admin
            val groupDoc = groupsCollection.document(groupId).get().await()
            if (!groupDoc.exists()) {
                return GroupResult.Error("Group not found")
            }

            val data = groupDoc.data ?: return GroupResult.Error("Group data missing")
            val membersMap = parseGroupData(data)
            val currentUserData = membersMap[currentUser.uid] ?: return GroupResult.Error("You are not a member of this group")

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

    override suspend fun updateGroup(
        groupId: String,
        name: String,
        description: String,
        currency: String
    ): GroupResult {
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

    override suspend fun removeMember(groupId: String, userId: String): GroupResult {
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
            if (!isAdmin) return GroupResult.Error("Only admins can remove members")

            // Remove member from group
            val updates = hashMapOf<String, Any>(
                "members.$userId" to FieldValue.delete(),
                "memberIds" to FieldValue.arrayRemove(userId)
            )

            groupsCollection.document(groupId).update(updates).await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            Log.e(TAG, "Error removing member", e)
            GroupResult.Error(e.message ?: "Failed to remove member")
        }
    }

    override suspend fun promoteToAdmin(groupId: String, userId: String): GroupResult {
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

    override suspend fun demoteFromAdmin(groupId: String, userId: String): GroupResult {
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

    override suspend fun archiveGroup(groupId: String): GroupResult {
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

    override suspend fun unarchiveGroup(groupId: String): GroupResult {
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