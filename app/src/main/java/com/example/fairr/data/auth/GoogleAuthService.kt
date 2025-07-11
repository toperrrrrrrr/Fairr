package com.example.fairr.data.auth

import android.content.Context
import android.content.Intent
import com.example.fairr.data.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val context: Context,
    private val userRepository: UserRepository
) {
    private lateinit var googleSignInClient: GoogleSignInClient

    fun initialize(webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<Unit> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = auth.signInWithCredential(credential).await()
        
        // Create or update user profile in Firestore
        authResult.user?.let { user ->
            userRepository.createOrUpdateUser(user)
        } ?: throw Exception("Authentication successful but user is null")
    }

    suspend fun signOut() {
        try {
            // Sign out from Firebase
            auth.signOut()
            // Revoke access and sign out from Google
            googleSignInClient.revokeAccess().await()
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            // Handle any errors silently, as this is a sign-out operation
        }
    }
} 