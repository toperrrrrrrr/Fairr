package com.example.fairr.data.groups

import com.example.fairr.ui.model.CreateGroupData
import com.example.fairr.ui.model.GroupMember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

sealed class GroupResult {
    data class Success(val groupId: String) : GroupResult()
    data class Error(val message: String) : GroupResult()
}

class GroupService {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val groupsCollection = firestore.collection("groups")

    suspend fun createGroup(groupData: CreateGroupData): GroupResult {
        return try {
            val currentUser = auth.currentUser
                ?: return GroupResult.Error("User not authenticated")

            // Create group document data
            val groupId = UUID.randomUUID().toString()
            
            // Create a map of members with user IDs as keys
            val membersMap = mutableMapOf<String, Map<String, Any>>()
            
            // Add current user as admin
            membersMap[currentUser.uid] = mapOf(
                "id" to currentUser.uid,
                "name" to (currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Unknown"),
                "email" to (currentUser.email ?: ""),
                "isAdmin" to true
            )
            
            // Add other members
            groupData.members.forEach { member ->
                membersMap[member.id] = mapOf(
                    "id" to member.id,
                    "name" to member.name,
                    "email" to member.email,
                    "isAdmin" to member.isAdmin
                )
            }

            val groupDocument = hashMapOf(
                "id" to groupId,
                "name" to groupData.name,
                "description" to groupData.description,
                "currency" to groupData.currency,
                "createdBy" to currentUser.uid,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "members" to membersMap,
                "inviteCode" to generateInviteCode()
            )

            // Create the group document
            groupsCollection.document(groupId)
                .set(groupDocument)
                .await()

            // Add group reference to each member's user document
            val batch = firestore.batch()
            membersMap.keys.forEach { userId ->
                val userGroupRef = firestore.collection("users")
                    .document(userId)
                    .collection("groups")
                    .document(groupId)
                
                batch.set(userGroupRef, hashMapOf(
                    "groupId" to groupId,
                    "joinedAt" to com.google.firebase.Timestamp.now(),
                    "isAdmin" to (membersMap[userId]?.get("isAdmin") ?: false)
                ))
            }
            batch.commit().await()

            GroupResult.Success(groupId)
        } catch (e: Exception) {
            GroupResult.Error(e.message ?: "Failed to create group")
        }
    }

    private fun generateInviteCode(): String {
        return UUID.randomUUID().toString()
            .replace("-", "")
            .take(6)
            .uppercase()
    }
} 