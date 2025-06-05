package com.example.fairr.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign in failed: Unknown error")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign up failed: Unknown error")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Sign up failed")
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(auth.currentUser!!)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Password reset failed")
        }
    }
} 