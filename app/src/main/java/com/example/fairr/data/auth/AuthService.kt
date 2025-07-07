package com.example.fairr.data.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Patterns

private const val TAG = "AuthService"

sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Authentication Service for the Fairr app.
 *
 * Handles user authentication, session management, token refresh, and account actions.
 * Integrates with Firebase Auth and provides helpers for sign-in, sign-up, password reset,
 * email verification, and session validation.
 */
@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _authState.value = if (user != null) {
            Log.d(TAG, "User authenticated: ${user.email}")
            AuthState.Authenticated(user)
        } else {
            Log.d(TAG, "User unauthenticated")
            AuthState.Unauthenticated
        }
    }

    init {
        // Add auth state listener on initialization
        auth.addAuthStateListener(authStateListener)
        
        // Set initial state safely
        val currentUser = auth.currentUser
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser)
        } else {
            AuthState.Unauthenticated
        }
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Check if user is currently authenticated with a valid session
     */
    suspend fun isUserAuthenticated(): Boolean {
        return try {
            val user = auth.currentUser
            if (user != null) {
                // Force token refresh to validate session
                user.getIdToken(true).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Session validation failed", e)
            false
        }
    }

    /**
     * Validate current session and refresh token if needed
     */
    suspend fun validateCurrentSession(): Boolean {
        return try {
            val user = auth.currentUser
            if (user != null) {
                // Force token refresh to validate session
                user.getIdToken(true).await()
                Log.d(TAG, "Session validated successfully")
                true
            } else {
                Log.d(TAG, "No current user found")
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Session validation failed", e)
            false
        }
    }

    /**
     * Refresh user token and return the new token
     */
    suspend fun refreshUserToken(): Result<String> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                val token = user.getIdToken(true).await()
                Log.d(TAG, "Token refreshed successfully")
                Result.success(token.token ?: "")
            } else {
                Result.failure(Exception("No authenticated user found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token refresh failed", e)
            Result.failure(e)
        }
    }

    /**
     * Get current user's ID token
     */
    suspend fun getCurrentUserToken(): String? {
        return try {
            val user = auth.currentUser
            if (user != null) {
                val token = user.getIdToken(false).await()
                token.token
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user token", e)
            null
        }
    }

    /**
     * Handle session expiration by signing out user
     */
    fun handleSessionExpiration() {
        Log.w(TAG, "Handling session expiration")
        signOut()
    }

    /**
     * Add custom auth state listener
     */
    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    /**
     * Remove custom auth state listener
     */
    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

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
                Log.d(TAG, "User signed in successfully: ${it.email}")
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign in failed: Unknown error")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sign in failed", e)
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
                Log.d(TAG, "User signed up successfully: ${it.email}")
                AuthResult.Success(it)
            } ?: AuthResult.Error("Sign up failed: Unknown error")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Sign up failed", e)
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }

    fun signOut() {
        Log.d(TAG, "User signing out")
        auth.signOut()
    }

    /**
     * Sign out and clear all user data and settings
     * This forces a complete fresh start on next login
     */
    suspend fun signOutWithDataClearing(
        userPreferencesManager: com.example.fairr.data.preferences.UserPreferencesManager,
        settingsDataStore: com.example.fairr.data.settings.SettingsDataStore
    ) {
        Log.d(TAG, "User signing out with complete data clearing")
        
        try {
            // Clear all user preferences and settings
            userPreferencesManager.clearAllData()
            settingsDataStore.clearAllSettings()
            
            // Sign out from Firebase
            auth.signOut()
            
            Log.d(TAG, "Sign out with data clearing completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out with data clearing", e)
            // Still sign out from Firebase even if data clearing fails
            auth.signOut()
            throw e
        }
    }

    suspend fun resetPassword(email: String): AuthResult {
        if (!isValidEmail(email)) {
            return AuthResult.Error("Please enter a valid email address")
        }

        return try {
            auth.sendPasswordResetEmail(email).await()
            Log.d(TAG, "Password reset email sent successfully to: $email")
            AuthResult.Success(auth.currentUser ?: throw Exception("No current user"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Password reset failed", e)
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }

    suspend fun sendEmailVerification(): AuthResult {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return AuthResult.Error("No user is currently signed in")
        }

        return try {
            currentUser.sendEmailVerification().await()
            Log.d(TAG, "Email verification sent successfully to: ${currentUser.email}")
            AuthResult.Success(currentUser)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Email verification failed", e)
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }

    /**
     * Check if user's email is verified and handle verification process
     * 
     * @param code Verification code (currently unused but kept for future custom verification)
     * @return AuthResult indicating success or failure of verification check
     */
    suspend fun verifyEmailWithCode(code: String): AuthResult {
        // Validate verification code format for future use
        if (code.isBlank()) {
            Log.w(TAG, "Empty verification code provided")
        } else if (code.length != 6 || !code.all { it.isDigit() }) {
            Log.w(TAG, "Invalid verification code format: $code")
        }
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return AuthResult.Error("No user is currently signed in")
        }

        return try {
            // Reload user to get latest verification status from Firebase
            currentUser.reload().await()
            
            if (currentUser.isEmailVerified) {
                Log.d(TAG, "Email verified successfully for user: ${currentUser.email}")
                AuthResult.Success(currentUser)
            } else {
                Log.w(TAG, "Email not yet verified for user: ${currentUser.email}")
                AuthResult.Error("Email not verified. Please check your email and click the verification link.")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Email verification check failed", e)
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        }
    }

    /**
     * Clean up resources when service is no longer needed
     */
    fun cleanup() {
        auth.removeAuthStateListener(authStateListener)
    }
} 