package com.example.fairr.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Patterns

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    private fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    private fun getFirebaseAuthErrorMessage(e: Exception): String {
        return when (e) {
            is FirebaseAuthInvalidUserException -> {
                when (e.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                    "ERROR_USER_DISABLED" -> "This account has been disabled"
                    else -> "Invalid email or password"
                }
            }
            is FirebaseAuthInvalidCredentialsException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Invalid email format"
                    "ERROR_WRONG_PASSWORD" -> "Incorrect password"
                    "ERROR_INVALID_CREDENTIAL" -> "The credentials are malformed or have expired"
                    else -> "Invalid email or password"
                }
            }
            else -> e.message ?: "Authentication failed"
        }
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (!isValidPassword(password)) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }

        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign in failed: Unknown error")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }

    suspend fun signUp(email: String, password: String): AuthResult {
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }
        if (!isValidPassword(password)) {
            return AuthResult.Error("Password must be at least 6 characters long")
        }

        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign up failed: Unknown error")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun resetPassword(email: String): AuthResult {
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }

        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(auth.currentUser!!)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }
} 