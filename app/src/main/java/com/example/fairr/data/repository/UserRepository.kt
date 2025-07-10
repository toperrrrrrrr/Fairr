package com.example.fairr.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository interface for managing user data operations.
 * 
 * This repository handles all user-related data operations including:
 * - User profile creation and updates
 * - User authentication state management
 * - User preferences and settings
 * - User statistics and analytics data
 * 
 * Threading: All suspend functions are safe to call from any coroutine context.
 * They will automatically switch to appropriate dispatchers as needed.
 * 
 * Error Handling: All methods may throw [FirebaseException] for network issues,
 * [AuthenticationException] for auth failures, or [ValidationException] for
 * invalid input data.
 * 
 * @since 1.0.0
 * @author Fairr Development Team
 */
interface UserRepository {
    
    /**
     * Create or update user profile in Firestore
     * @param user The Firebase user object
     */
    suspend fun createOrUpdateUser(user: FirebaseUser)
    
    /**
     * Get user profile by ID
     * @param userId The user ID to retrieve
     * @return User profile data or null if not found
     */
    suspend fun getUserById(userId: String): UserProfile?
    
    /**
     * Update user profile information
     * @param userId The user ID to update
     * @param updates Map of field updates
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>)
    
    /**
     * Delete user profile and all associated data
     * @param userId The user ID to delete
     */
    suspend fun deleteUser(userId: String)
    
    /**
     * Get user statistics (groups, expenses, etc.)
     * @param userId The user ID to get statistics for
     * @return User statistics
     */
    suspend fun getUserStatistics(userId: String): UserStatistics
}

/**
 * User profile data model
 */
data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val createdAt: Long,
    val lastLoginAt: Long,
    val isEmailVerified: Boolean
)

/**
 * User statistics data model
 */
data class UserStatistics(
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val primaryCurrency: String = "USD",
    val userType: String = "new_user"
)

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository {
    private val usersCollection = firestore.collection("users")

    override suspend fun createOrUpdateUser(user: FirebaseUser) {
        val userData = hashMapOf(
            "id" to user.uid,
            "email" to user.email,
            "displayName" to (user.displayName ?: user.email?.substringBefore("@")),
            "photoUrl" to (user.photoUrl?.toString()),
            "createdAt" to System.currentTimeMillis(),
            "lastLoginAt" to System.currentTimeMillis(),
            "isEmailVerified" to user.isEmailVerified
        )

        try {
            // First try to get the user document
            val userDoc = usersCollection.document(user.uid).get().await()
            
            if (!userDoc.exists()) {
                // If user doesn't exist, create new document
                usersCollection.document(user.uid).set(userData).await()
            } else {
                // If user exists, update only lastLoginAt
                usersCollection.document(user.uid)
                    .update("lastLoginAt", System.currentTimeMillis())
                    .await()
            }
        } catch (e: Exception) {
            throw e
        }
    }
    
    override suspend fun getUserById(userId: String): UserProfile? {
        return try {
            val userDoc = usersCollection.document(userId).get().await()
            if (!userDoc.exists()) return null
            
            val data = userDoc.data ?: return null
            UserProfile(
                id = userDoc.id,
                email = data["email"] as? String ?: "",
                displayName = data["displayName"] as? String,
                photoUrl = data["photoUrl"] as? String,
                createdAt = data["createdAt"] as? Long ?: 0L,
                lastLoginAt = data["lastLoginAt"] as? Long ?: 0L,
                isEmailVerified = data["isEmailVerified"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        usersCollection.document(userId).update(updates).await()
    }
    
    override suspend fun deleteUser(userId: String) {
        // Note: This should be implemented with proper GDPR compliance
        // and cascading deletion of all user data
        usersCollection.document(userId).delete().await()
    }
    
    override suspend fun getUserStatistics(userId: String): UserStatistics {
        // Implementation would query groups and expenses collections
        // For now, return default statistics
        return UserStatistics()
    }
} 