package com.example.fairr.data.gdpr

import android.content.Context
import android.util.Log
import com.example.fairr.data.auth.AuthService
import com.example.fairr.data.export.ExportService
import com.example.fairr.data.export.ExportOptions
import com.example.fairr.data.export.ExportFormat
import com.example.fairr.data.preferences.UserPreferencesManager
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.utils.PhotoUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*

/**
 * GDPR Compliance Service for the Fairr app.
 *
 * Handles data deletion, export, and privacy controls to implement GDPR "Right to be Forgotten"
 * and "Data Portability" requirements. Coordinates user data removal across Firestore, Storage,
 * Auth, and local device, and manages export for data portability.
 */
@Singleton
class GDPRComplianceService @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val authService: AuthService,
    private val exportService: ExportService,
    private val userPreferencesManager: UserPreferencesManager,
    private val settingsDataStore: SettingsDataStore,
    private val photoUtils: PhotoUtils
) {
    
    companion object {
        private const val TAG = "GDPRComplianceService"
        private const val BATCH_SIZE = 500 // Firestore batch limit
    }

    /**
     * Complete user account deletion - implements GDPR "Right to be Forgotten"
     * Deletes all user data across all collections and Firebase Auth
     */
    suspend fun deleteUserAccount(provideFeedback: (String) -> Unit = {}): GDPRResult = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid
            ?: return@withContext GDPRResult.Error("User not authenticated")

        try {
            provideFeedback("Starting account deletion process...")
            
            // Step 1: Export user data before deletion (if requested)
            provideFeedback("Preparing final data export...")
            val exportResult = exportService.exportData(
                ExportOptions(
                    format = ExportFormat.CSV,
                    includeSettlements = true,
                    groupId = null
                )
            )
            
            // Step 2: Delete user data from all Firestore collections
            provideFeedback("Deleting user data from database...")
            deleteUserDataFromFirestore(userId, provideFeedback)
            
            // Step 3: Delete user files from Firebase Storage
            provideFeedback("Deleting user files from storage...")
            deleteUserStorageFiles(userId)
            
            // Step 4: Clear local data
            provideFeedback("Clearing local data...")
            clearLocalUserData()
            
            // Step 5: Delete Firebase Auth account (must be last)
            provideFeedback("Deleting authentication account...")
            auth.currentUser?.delete()?.await()
            
            // Step 6: Clean up temporary files
            provideFeedback("Cleaning up temporary files...")
            photoUtils.cleanupTempFiles(context)
            
            provideFeedback("Account deletion completed successfully")
            
            GDPRResult.Success(
                message = "Account and all associated data have been permanently deleted",
                exportData = exportResult.fileUri
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during account deletion", e)
            GDPRResult.Error("Account deletion failed: ${e.message}")
        }
    }

    /**
     * Delete user data from all Firestore collections
     */
    private suspend fun deleteUserDataFromFirestore(
        userId: String, 
        provideFeedback: (String) -> Unit
    ) {
        val collections = listOf(
            "users",
            "groups",
            "expenses",
            "settlements",
            "notifications",
            "activities",
            "groupJoinRequests",
            "groupInvites", 
            "friendRequests",
            "friends",
            "userReports",
            "friendGroups",
            "friendGroupMemberships"
        )
        
        for (collection in collections) {
            provideFeedback("Deleting data from $collection...")
            when (collection) {
                "users" -> deleteUserProfile(userId)
                "groups" -> handleGroupDeletion(userId, provideFeedback)
                "expenses" -> deleteUserExpenses(userId)
                "settlements" -> deleteUserSettlements(userId)
                "notifications" -> deleteUserNotifications(userId)
                "activities" -> deleteUserActivities(userId)
                "groupJoinRequests" -> deleteUserJoinRequests(userId)
                "groupInvites" -> deleteUserInvites(userId)
                "friendRequests" -> deleteUserFriendRequests(userId)
                "friends" -> deleteUserFriends(userId)
                "userReports" -> deleteUserReports(userId)
                "friendGroups" -> deleteUserFriendGroups(userId)
                "friendGroupMemberships" -> deleteUserFriendGroupMemberships(userId)
            }
        }
    }

    /**
     * Handle group deletion with special logic for admin users
     */
    private suspend fun handleGroupDeletion(userId: String, provideFeedback: (String) -> Unit) {
        // Get groups where user is a member
        val userGroups = firestore.collection("groups")
            .whereArrayContains("memberIds", userId)
            .get()
            .await()

        for (groupDoc in userGroups.documents) {
            val groupData = groupDoc.data ?: continue
            val members = groupData["members"] as? Map<String, Any> ?: continue
            val memberIds = groupData["memberIds"] as? List<String> ?: continue
            
            val isUserAdmin = (members[userId] as? Map<String, Any>)?.get("isAdmin") as? Boolean ?: false
            val remainingMembers = memberIds.filter { it != userId }
            
            if (isUserAdmin && remainingMembers.isNotEmpty()) {
                // Transfer admin to another member
                provideFeedback("Transferring admin role in group ${groupData["name"]}")
                val newAdmin = remainingMembers.first()
                val updatedMembers = members.toMutableMap()
                (updatedMembers[newAdmin] as? MutableMap<String, Any>)?.put("isAdmin", true)
                (updatedMembers[userId] as? MutableMap<String, Any>)?.put("isAdmin", false)
                
                groupDoc.reference.update(
                    mapOf(
                        "members" to updatedMembers,
                        "memberIds" to remainingMembers
                    )
                ).await()
            } else if (remainingMembers.isEmpty()) {
                // Delete the entire group if user was the last member
                provideFeedback("Deleting empty group ${groupData["name"]}")
                deleteEntireGroup(groupDoc.id)
            } else {
                // Just remove user from group
                val updatedMembers = members.toMutableMap()
                updatedMembers.remove(userId)
                
                groupDoc.reference.update(
                    mapOf(
                        "members" to updatedMembers,
                        "memberIds" to remainingMembers
                    )
                ).await()
            }
        }
    }

    /**
     * Delete an entire group and all associated data
     */
    private suspend fun deleteEntireGroup(groupId: String) {
        val batch = firestore.batch()
        
        // Delete all expenses for this group
        val expenses = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .get()
            .await()
        
        expenses.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete all settlements for this group
        val settlements = firestore.collection("settlements")
            .whereEqualTo("groupId", groupId)
            .get()
            .await()
            
        settlements.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete all activities for this group
        val activities = firestore.collection("activities")
            .whereEqualTo("groupId", groupId)
            .get()
            .await()
            
        activities.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete the group itself
        batch.delete(firestore.collection("groups").document(groupId))
        
        batch.commit().await()
    }

    /**
     * Delete user profile
     */
    private suspend fun deleteUserProfile(userId: String) {
        firestore.collection("users").document(userId).delete().await()
    }

    /**
     * Delete all expenses created by or involving the user
     */
    private suspend fun deleteUserExpenses(userId: String) {
        // Delete expenses where user was the payer
        val paidExpenses = firestore.collection("expenses")
            .whereEqualTo("paidBy", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        paidExpenses.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Note: Expenses where user was only in splitBetween are handled by group deletion
        batch.commit().await()
    }

    /**
     * Delete user settlements
     */
    private suspend fun deleteUserSettlements(userId: String) {
        val batch = firestore.batch()
        
        // Delete settlements where user was payer
        val payerSettlements = firestore.collection("settlements")
            .whereEqualTo("payerId", userId)
            .get()
            .await()
            
        payerSettlements.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete settlements where user was payee
        val payeeSettlements = firestore.collection("settlements")
            .whereEqualTo("payeeId", userId)
            .get()
            .await()
            
        payeeSettlements.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        batch.commit().await()
    }

    /**
     * Delete user notifications
     */
    private suspend fun deleteUserNotifications(userId: String) {
        val notifications = firestore.collection("notifications")
            .whereEqualTo("recipientId", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        notifications.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete user activities
     */
    private suspend fun deleteUserActivities(userId: String) {
        val activities = firestore.collection("activities")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        activities.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete user join requests
     */
    private suspend fun deleteUserJoinRequests(userId: String) {
        val requests = firestore.collection("groupJoinRequests")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        requests.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete user invites
     */
    private suspend fun deleteUserInvites(userId: String) {
        val batch = firestore.batch()
        
        // Delete invites sent by user
        val sentInvites = firestore.collection("groupInvites")
            .whereEqualTo("inviterId", userId)
            .get()
            .await()
            
        sentInvites.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete invites received by user
        val receivedInvites = firestore.collection("groupInvites")
            .whereEqualTo("inviteeId", userId)
            .get()
            .await()
            
        receivedInvites.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        batch.commit().await()
    }

    /**
     * Delete user friend requests
     */
    private suspend fun deleteUserFriendRequests(userId: String) {
        val batch = firestore.batch()
        
        // Delete requests sent by user
        val sentRequests = firestore.collection("friendRequests")
            .whereEqualTo("senderId", userId)
            .get()
            .await()
            
        sentRequests.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete requests received by user
        val receivedRequests = firestore.collection("friendRequests")
            .whereEqualTo("receiverId", userId)
            .get()
            .await()
            
        receivedRequests.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        batch.commit().await()
    }

    /**
     * Delete user friendships
     */
    private suspend fun deleteUserFriends(userId: String) {
        val batch = firestore.batch()
        
        // Delete friendships where user is userId
        val friends1 = firestore.collection("friends")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        friends1.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        // Delete friendships where user is friendId
        val friends2 = firestore.collection("friends")
            .whereEqualTo("friendId", userId)
            .get()
            .await()
            
        friends2.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        
        batch.commit().await()
    }

    /**
     * Delete user reports
     */
    private suspend fun deleteUserReports(userId: String) {
        val reports = firestore.collection("userReports")
            .whereEqualTo("reportedBy", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        reports.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete user friend groups
     */
    private suspend fun deleteUserFriendGroups(userId: String) {
        val friendGroups = firestore.collection("friendGroups")
            .whereEqualTo("createdBy", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        friendGroups.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete user friend group memberships
     */
    private suspend fun deleteUserFriendGroupMemberships(userId: String) {
        val memberships = firestore.collection("friendGroupMemberships")
            .whereEqualTo("addedBy", userId)
            .get()
            .await()
            
        val batch = firestore.batch()
        memberships.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    /**
     * Delete user files from Firebase Storage
     */
    private suspend fun deleteUserStorageFiles(userId: String) {
        try {
            // Delete profile pictures
            val profilePicRef = storage.reference.child("profile_pictures/$userId/")
            profilePicRef.listAll().await().items.forEach { item ->
                item.delete().await()
            }
            
            // Delete receipt images
            val receiptRef = storage.reference.child("receipts/$userId/")
            receiptRef.listAll().await().items.forEach { item ->
                item.delete().await()
            }
            
            // Delete any other user-specific files
            val userFilesRef = storage.reference.child("users/$userId/")
            userFilesRef.listAll().await().items.forEach { item ->
                item.delete().await()
            }
            
        } catch (e: Exception) {
            Log.w(TAG, "Some storage files could not be deleted", e)
            // Continue with deletion process even if some files fail
        }
    }

    /**
     * Clear all local user data
     */
    private suspend fun clearLocalUserData() {
        try {
            userPreferencesManager.clearAllData()
            settingsDataStore.clearAllSettings()
            
            // Clear cached files
            photoUtils.cleanupTempFiles(context)
            
        } catch (e: Exception) {
            Log.w(TAG, "Some local data could not be cleared", e)
            // Continue with deletion process
        }
    }

    /**
     * Request data portability - enhanced data export with metadata
     */
    suspend fun requestDataPortability(): GDPRResult = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid
                ?: return@withContext GDPRResult.Error("User not authenticated")

            val exportResult = exportService.exportData(
                ExportOptions(
                    format = ExportFormat.CSV,
                    includeSettlements = true,
                    groupId = null
                )
            )

            if (exportResult.success) {
                GDPRResult.Success(
                    message = "Data export completed successfully",
                    exportData = exportResult.fileUri
                )
            } else {
                GDPRResult.Error(exportResult.errorMessage ?: "Export failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during data export", e)
            GDPRResult.Error("Data export failed: ${e.message}")
        }
    }

    /**
     * Get data processing summary for transparency
     */
    suspend fun getDataProcessingSummary(): DataProcessingSummary = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext DataProcessingSummary()

        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userData = userDoc.data

            val groups = firestore.collection("groups")
                .whereArrayContains("memberIds", userId)
                .get()
                .await()

            val expenses = firestore.collection("expenses")
                .whereEqualTo("paidBy", userId)
                .get()
                .await()

            val notifications = firestore.collection("notifications")
                .whereEqualTo("recipientId", userId)
                .get()
                .await()

            DataProcessingSummary(
                accountCreated = userData?.get("createdAt")?.toString() ?: "Unknown",
                lastActive = userData?.get("lastActive")?.toString() ?: "Unknown",
                totalGroups = groups.size(),
                totalExpenses = expenses.size(),
                totalNotifications = notifications.size(),
                dataRetentionPeriod = "Active accounts: indefinite; Deleted accounts: 30 days",
                processingPurposes = listOf(
                    "Service delivery and expense tracking",
                    "Group communication and notifications", 
                    "Security and fraud prevention",
                    "App functionality improvement"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting data summary", e)
            DataProcessingSummary()
        }
    }
}

/**
 * Result wrapper for GDPR operations
 */
sealed class GDPRResult {
    data class Success(
        val message: String,
        val exportData: android.net.Uri? = null
    ) : GDPRResult()
    
    data class Error(val message: String) : GDPRResult()
}

/**
 * Data processing summary for GDPR transparency
 */
data class DataProcessingSummary(
    val accountCreated: String = "",
    val lastActive: String = "",
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val totalNotifications: Int = 0,
    val dataRetentionPeriod: String = "",
    val processingPurposes: List<String> = emptyList()
)