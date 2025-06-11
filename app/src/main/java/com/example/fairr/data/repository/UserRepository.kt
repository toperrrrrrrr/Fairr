package com.example.fairr.data.repository

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun createOrUpdateUser(user: FirebaseUser) {
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
} 