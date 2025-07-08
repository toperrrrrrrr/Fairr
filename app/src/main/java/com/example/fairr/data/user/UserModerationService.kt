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
    private val userReportsCollection = firestore.collection("userReports")

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