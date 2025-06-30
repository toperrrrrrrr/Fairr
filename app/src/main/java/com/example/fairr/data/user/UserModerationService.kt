package com.example.fairr.data.user

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

private const val TAG = "UserModerationService"

sealed class ModerationResult {
    data class Success(val message: String) : ModerationResult()
    data class Error(val message: String) : ModerationResult()
}

@Singleton
class UserModerationService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val blockedUsersCollection = firestore.collection("blockedUsers")
    private val userReportsCollection = firestore.collection("userReports")

    /**
     * Block a user
     */
    suspend fun blockUser(
        userId: String, 
        userName: String, 
        userEmail: String, 
        reason: String = ""
    ): ModerationResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return ModerationResult.Error("User not authenticated")

            if (userId == currentUser.uid) {
                return ModerationResult.Error("Cannot block yourself")
            }

            // Check if user is already blocked
            val existingBlock = blockedUsersCollection
                .whereEqualTo("blockedBy", currentUser.uid)
                .whereEqualTo("blockedUserId", userId)
                .get()
                .await()

            if (!existingBlock.isEmpty) {
                return ModerationResult.Error("User is already blocked")
            }

            val blockId = blockedUsersCollection.document().id
            val blockedUser = BlockedUser(
                id = blockId,
                blockedUserId = userId,
                blockedUserName = userName,
                blockedUserEmail = userEmail,
                blockedBy = currentUser.uid,
                blockedAt = Timestamp.now(),
                reason = reason
            )

            val blockData = mapOf(
                "blockedUserId" to blockedUser.blockedUserId,
                "blockedUserName" to blockedUser.blockedUserName,
                "blockedUserEmail" to blockedUser.blockedUserEmail,
                "blockedBy" to blockedUser.blockedBy,
                "blockedAt" to blockedUser.blockedAt,
                "reason" to blockedUser.reason
            )

            blockedUsersCollection.document(blockId).set(blockData).await()
            
            Log.d(TAG, "Successfully blocked user: $userName")
            ModerationResult.Success("User blocked successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error blocking user", e)
            ModerationResult.Error(e.message ?: "Failed to block user")
        }
    }

    /**
     * Unblock a user
     */
    suspend fun unblockUser(userId: String): ModerationResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return ModerationResult.Error("User not authenticated")

            val blockQuery = blockedUsersCollection
                .whereEqualTo("blockedBy", currentUser.uid)
                .whereEqualTo("blockedUserId", userId)
                .get()
                .await()

            if (blockQuery.isEmpty) {
                return ModerationResult.Error("User is not blocked")
            }

            // Delete the block record
            blockQuery.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            Log.d(TAG, "Successfully unblocked user: $userId")
            ModerationResult.Success("User unblocked successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error unblocking user", e)
            ModerationResult.Error(e.message ?: "Failed to unblock user")
        }
    }

    /**
     * Check if a user is blocked by current user
     */
    suspend fun isUserBlocked(userId: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false

            val blockQuery = blockedUsersCollection
                .whereEqualTo("blockedBy", currentUser.uid)
                .whereEqualTo("blockedUserId", userId)
                .get()
                .await()

            !blockQuery.isEmpty

        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user is blocked", e)
            false
        }
    }

    /**
     * Get all blocked users for current user
     */
    fun getBlockedUsers(): Flow<List<BlockedUser>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = blockedUsersCollection
            .whereEqualTo("blockedBy", currentUser.uid)
            .orderBy("blockedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val blockedUsers = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        BlockedUser(
                            id = doc.id,
                            blockedUserId = data["blockedUserId"] as? String ?: "",
                            blockedUserName = data["blockedUserName"] as? String ?: "",
                            blockedUserEmail = data["blockedUserEmail"] as? String ?: "",
                            blockedBy = data["blockedBy"] as? String ?: "",
                            blockedAt = data["blockedAt"] as? Timestamp ?: Timestamp.now(),
                            reason = data["reason"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing blocked user", e)
                        null
                    }
                } ?: emptyList()

                trySend(blockedUsers)
            }

        awaitClose { subscription.remove() }
    }

    /**
     * Report a user
     */
    suspend fun reportUser(
        userId: String,
        userName: String,
        userEmail: String,
        reportType: UserReportType,
        reason: String,
        description: String
    ): ModerationResult {
        return try {
            val currentUser = auth.currentUser 
                ?: return ModerationResult.Error("User not authenticated")

            if (userId == currentUser.uid) {
                return ModerationResult.Error("Cannot report yourself")
            }

            val reportId = userReportsCollection.document().id
            val userReport = UserReport(
                id = reportId,
                reportedUserId = userId,
                reportedUserName = userName,
                reportedUserEmail = userEmail,
                reportedBy = currentUser.uid,
                reportType = reportType,
                reason = reason,
                description = description,
                reportedAt = Timestamp.now(),
                status = ReportStatus.PENDING
            )

            val reportData = mapOf(
                "reportedUserId" to userReport.reportedUserId,
                "reportedUserName" to userReport.reportedUserName,
                "reportedUserEmail" to userReport.reportedUserEmail,
                "reportedBy" to userReport.reportedBy,
                "reportType" to userReport.reportType.name,
                "reason" to userReport.reason,
                "description" to userReport.description,
                "reportedAt" to userReport.reportedAt,
                "status" to userReport.status.name
            )

            userReportsCollection.document(reportId).set(reportData).await()
            
            Log.d(TAG, "Successfully reported user: $userName")
            ModerationResult.Success("User reported successfully. Thank you for helping keep our community safe.")

        } catch (e: Exception) {
            Log.e(TAG, "Error reporting user", e)
            ModerationResult.Error(e.message ?: "Failed to report user")
        }
    }

    /**
     * Get reports made by current user
     */
    fun getUserReports(): Flow<List<UserReport>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val subscription = userReportsCollection
            .whereEqualTo("reportedBy", currentUser.uid)
            .orderBy("reportedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reports = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        UserReport(
                            id = doc.id,
                            reportedUserId = data["reportedUserId"] as? String ?: "",
                            reportedUserName = data["reportedUserName"] as? String ?: "",
                            reportedUserEmail = data["reportedUserEmail"] as? String ?: "",
                            reportedBy = data["reportedBy"] as? String ?: "",
                            reportType = UserReportType.valueOf(data["reportType"] as? String ?: "OTHER"),
                            reason = data["reason"] as? String ?: "",
                            description = data["description"] as? String ?: "",
                            reportedAt = data["reportedAt"] as? Timestamp ?: Timestamp.now(),
                            status = ReportStatus.valueOf(data["status"] as? String ?: "PENDING")
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing user report", e)
                        null
                    }
                } ?: emptyList()

                trySend(reports)
            }

        awaitClose { subscription.remove() }
    }
} 