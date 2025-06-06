package com.example.fairr.data.groups

import android.util.Log
import com.example.fairr.ui.model.CreateGroupData
import com.example.fairr.ui.model.Group
import com.example.fairr.ui.model.GroupMember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
class GroupService @Inject constructor() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    fun getUserGroups(): Flow<List<Group>> = callbackFlow {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "Fetching groups for user: ${currentUser.uid}")

        val subscription = groupsCollection
            .whereNotEqualTo("members." + currentUser.uid, null)
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
                        
                        val membersMap = data["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
                        val members = membersMap.map { (userId, memberData) ->
                            GroupMember(
                                id = userId,
                                name = memberData["name"] as? String ?: "Unknown",
                                email = memberData["email"] as? String ?: "",
                                isAdmin = memberData["isAdmin"] as? Boolean ?: false,
                                isCurrentUser = userId == currentUser.uid
                            )
                        }

                        Group(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            currency = data["currency"] as? String ?: "PHP",
                            createdBy = data["createdBy"] as? String ?: "",
                            members = members,
                            inviteCode = data["inviteCode"] as? String ?: "",
                            createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
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
                    val membersMap = data["members"] as? Map<String, Map<String, Any>> ?: emptyMap()
                    val members = membersMap.map { (userId, memberData) ->
                        GroupMember(
                            id = userId,
                            name = memberData["name"] as? String ?: "Unknown",
                            email = memberData["email"] as? String ?: "",
                            isAdmin = memberData["isAdmin"] as? Boolean ?: false,
                            isCurrentUser = userId == currentUser.uid
                        )
                    }

                    val group = Group(
                        id = snapshot.id,
                        name = data["name"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        currency = data["currency"] as? String ?: "PHP",
                        createdBy = data["createdBy"] as? String ?: "",
                        members = members,
                        inviteCode = data["inviteCode"] as? String ?: "",
                        createdAt = data["createdAt"] as? Long ?: System.currentTimeMillis()
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
            
            // Add current user as admin
            val currentUserData = mapOf(
                "id" to currentUser.uid,
                "name" to (currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown"),
                "email" to (currentUser.email ?: ""),
                "isAdmin" to true
            )
            membersMap[currentUser.uid] = currentUserData
            
            Log.d(TAG, "Current user data: $currentUserData")
            
            // Add other members
            groupData.members.forEach { member ->
                val memberData = mapOf(
                    "id" to member.id,
                    "name" to member.name,
                    "email" to member.email,
                    "isAdmin" to member.isAdmin
                )
                membersMap[member.id] = memberData
            }

            val groupDocument = hashMapOf(
                "name" to groupData.name,
                "description" to groupData.description,
                "currency" to groupData.currency,
                "members" to membersMap,
                "createdBy" to currentUser.uid,
                "createdAt" to com.google.firebase.Timestamp.now(),
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

            val membersMap = groupDoc.get("members") as? Map<String, Map<String, Any>> ?: emptyMap()
            if (membersMap.containsKey(currentUser.uid)) {
                return GroupResult.Error("You are already a member of this group")
            }

            val newMember = mapOf(
                currentUser.uid to mapOf(
                    "id" to currentUser.uid,
                    "name" to (currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown"),
                    "email" to (currentUser.email ?: ""),
                    "isAdmin" to false
                )
            )

            groupsCollection.document(groupId)
                .update("members", membersMap + newMember)
                .await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            GroupResult.Error(e.message ?: "Failed to join group")
        }
    }

    private fun generateInviteCode(): String {
        return UUID.randomUUID().toString()
            .replace("-", "")
            .take(6)
            .uppercase()
    }
} 