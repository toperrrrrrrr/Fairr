package com.example.fairr.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Data class representing the stored authentication state
 */
data class AuthState(
    val userId: String? = null,
    val userEmail: String? = null,
    val userDisplayName: String? = null,
    val sessionTimestamp: Long = 0L,
    val isAuthenticated: Boolean = false
)

@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // Authentication state keys
        val AUTH_USER_ID = stringPreferencesKey("auth_user_id")
        val AUTH_USER_EMAIL = stringPreferencesKey("auth_user_email")
        val AUTH_USER_DISPLAY_NAME = stringPreferencesKey("auth_user_display_name")
        val AUTH_SESSION_TIMESTAMP = longPreferencesKey("auth_session_timestamp")
        val AUTH_IS_AUTHENTICATED = booleanPreferencesKey("auth_is_authenticated")
        
        // Sign-out tracking
        val FORCE_ACCOUNT_SELECTION = booleanPreferencesKey("force_account_selection")
    }
    
    // Onboarding preferences
    val onboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }
        
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    // Authentication state preferences
    val authState: Flow<AuthState> = context.dataStore.data
        .map { preferences ->
            AuthState(
                userId = preferences[PreferencesKeys.AUTH_USER_ID],
                userEmail = preferences[PreferencesKeys.AUTH_USER_EMAIL],
                userDisplayName = preferences[PreferencesKeys.AUTH_USER_DISPLAY_NAME],
                sessionTimestamp = preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L,
                isAuthenticated = preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] ?: false
            )
        }

    /**
     * Save authentication state when user signs in
     */
    suspend fun saveAuthState(user: FirebaseUser) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_USER_ID] = user.uid
            preferences[PreferencesKeys.AUTH_USER_EMAIL] = user.email ?: ""
            preferences[PreferencesKeys.AUTH_USER_DISPLAY_NAME] = user.displayName ?: ""
            preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] = System.currentTimeMillis()
            preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] = true
            
            // Clear force account selection flag when user manually signs in
            preferences.remove(PreferencesKeys.FORCE_ACCOUNT_SELECTION)
        }
    }

    /**
     * Get the current authentication state
     */
    suspend fun getAuthState(): AuthState? {
        return try {
            context.dataStore.data.map { preferences ->
                AuthState(
                    userId = preferences[PreferencesKeys.AUTH_USER_ID],
                    userEmail = preferences[PreferencesKeys.AUTH_USER_EMAIL],
                    userDisplayName = preferences[PreferencesKeys.AUTH_USER_DISPLAY_NAME],
                    sessionTimestamp = preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L,
                    isAuthenticated = preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] ?: false
                )
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Clear authentication state when user signs out
     */
    suspend fun clearAuthState() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_USER_ID)
            preferences.remove(PreferencesKeys.AUTH_USER_EMAIL)
            preferences.remove(PreferencesKeys.AUTH_USER_DISPLAY_NAME)
            preferences.remove(PreferencesKeys.AUTH_SESSION_TIMESTAMP)
            preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] = false
        }
    }

    /**
     * Clear all user data including onboarding and authentication state
     * This is used for complete sign-out to force fresh login
     */
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            // Clear onboarding status
            preferences.remove(PreferencesKeys.ONBOARDING_COMPLETED)
            
            // Clear all authentication data
            preferences.remove(PreferencesKeys.AUTH_USER_ID)
            preferences.remove(PreferencesKeys.AUTH_USER_EMAIL)
            preferences.remove(PreferencesKeys.AUTH_USER_DISPLAY_NAME)
            preferences.remove(PreferencesKeys.AUTH_SESSION_TIMESTAMP)
            preferences.remove(PreferencesKeys.AUTH_IS_AUTHENTICATED)
            
            // Set flag to force account selection on next login
            preferences[PreferencesKeys.FORCE_ACCOUNT_SELECTION] = true
        }
    }

    /**
     * Check if the stored session is still valid
     * Default session timeout is 7 days
     */
    suspend fun isSessionValid(sessionTimeoutDays: Long = 7): Boolean {
        val authState = try {
            context.dataStore.data.map { preferences ->
                val timestamp = preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L
                val isAuthenticated = preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] ?: false
                timestamp > 0 && isAuthenticated
            }.first()
        } catch (e: Exception) {
            false
        }

        if (!authState) return false

        val sessionTimestamp = try {
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L
            }.first()
        } catch (e: Exception) {
            0L
        }

        if (sessionTimestamp == 0L) return false

        val currentTime = System.currentTimeMillis()
        val sessionTimeoutMillis = TimeUnit.DAYS.toMillis(sessionTimeoutDays)
        
        return (currentTime - sessionTimestamp) < sessionTimeoutMillis
    }

    /**
     * Update session timestamp to extend session validity
     */
    suspend fun updateSessionTimestamp() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] = System.currentTimeMillis()
        }
    }

    /**
     * Get user ID from stored preferences
     */
    suspend fun getUserId(): String? {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.AUTH_USER_ID]
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get user email from stored preferences
     */
    suspend fun getUserEmail(): String? {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.AUTH_USER_EMAIL]
            }.first()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if user is authenticated based on stored preferences
     */
    suspend fun isUserAuthenticated(): Boolean {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] ?: false
            }.first()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if account selection should be forced (after complete sign-out)
     */
    suspend fun shouldForceAccountSelection(): Boolean {
        return try {
            context.dataStore.data.map { preferences ->
                preferences[PreferencesKeys.FORCE_ACCOUNT_SELECTION] ?: false
            }.first()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Clear the force account selection flag (after user manually chooses authentication)
     */
    suspend fun clearForceAccountSelection() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.FORCE_ACCOUNT_SELECTION)
        }
    }
} 