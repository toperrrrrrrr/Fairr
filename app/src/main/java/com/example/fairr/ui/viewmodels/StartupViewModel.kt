package com.example.fairr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.auth.AuthService
import com.example.fairr.data.auth.AuthState
import com.example.fairr.data.preferences.UserPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val authService: AuthService
) : ViewModel() {

    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authLoading = MutableStateFlow(true)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    init {
        validateSessionOnStartup()
        observeAuthState()
    }

    /**
     * Validate existing session on app startup
     */
    private fun validateSessionOnStartup() {
        viewModelScope.launch {
            try {
                _authLoading.value = true
                _authError.value = null

                // Check if onboarding is completed
                val onboardingCompleted = userPreferencesManager.onboardingCompleted.first()
                _isOnboardingCompleted.value = onboardingCompleted

                if (!onboardingCompleted) {
                    _startupState.value = StartupState.Onboarding
                    _authLoading.value = false
                    return@launch
                }

                // Check if account selection should be forced (after complete sign-out)
                val forceAccountSelection = userPreferencesManager.shouldForceAccountSelection()
                if (forceAccountSelection) {
                    _startupState.value = StartupState.Authentication
                    _authLoading.value = false
                    return@launch
                }

                // Check if user has stored authentication state
                val storedAuthState = userPreferencesManager.getAuthState()
                val hasStoredAuth = storedAuthState?.isAuthenticated == true

                if (!hasStoredAuth) {
                    _startupState.value = StartupState.Authentication
                    _authLoading.value = false
                    return@launch
                }

                // Validate current Firebase session
                val isFirebaseAuthenticated = authService.isUserAuthenticated()
                
                if (isFirebaseAuthenticated) {
                    // Session is valid, update timestamp and proceed
                    userPreferencesManager.updateSessionTimestamp()
                    _isAuthenticated.value = true
                    _startupState.value = StartupState.Main
                } else {
                    // Session is invalid, clear stored state and redirect to auth
                    userPreferencesManager.clearAuthState()
                    _isAuthenticated.value = false
                    _startupState.value = StartupState.Authentication
                }

            } catch (e: Exception) {
                _authError.value = "Failed to validate session: ${e.message}"
                _startupState.value = StartupState.Authentication
            } finally {
                _authLoading.value = false
            }
        }
    }

    /**
     * Observe Firebase Auth state changes
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authService.authState.collect { authState ->
                when (authState) {
                    is AuthState.Loading -> {
                        _authLoading.value = true
                    }
                    is AuthState.Authenticated -> {
                        _isAuthenticated.value = true
                        _authLoading.value = false
                        _authError.value = null
                        
                        // Save auth state to preferences
                        userPreferencesManager.saveAuthState(authState.user)
                        
                        // Update startup state if onboarding is completed
                        if (_isOnboardingCompleted.value) {
                            _startupState.value = StartupState.Main
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        _isAuthenticated.value = false
                        _authLoading.value = false
                        
                        // Clear stored auth state
                        userPreferencesManager.clearAuthState()
                        
                        // Update startup state based on onboarding status
                        if (_isOnboardingCompleted.value) {
                            _startupState.value = StartupState.Authentication
                        } else {
                            _startupState.value = StartupState.Onboarding
                        }
                    }
                    is AuthState.Error -> {
                        _authError.value = authState.message
                        _authLoading.value = false
                        _isAuthenticated.value = false
                        
                        // Clear stored auth state on error
                        userPreferencesManager.clearAuthState()
                        
                        if (_isOnboardingCompleted.value) {
                            _startupState.value = StartupState.Authentication
                        } else {
                            _startupState.value = StartupState.Onboarding
                        }
                    }
                }
            }
        }
    }

    /**
     * Handle authentication state changes
     */
    fun handleAuthStateChange() {
        viewModelScope.launch {
            try {
                _authLoading.value = true
                
                // Validate current session
                val isValid = authService.validateCurrentSession()
                
                if (isValid) {
                    _isAuthenticated.value = true
                    _authError.value = null
                    
                    // Update session timestamp
                    userPreferencesManager.updateSessionTimestamp()
                    
                    if (_isOnboardingCompleted.value) {
                        _startupState.value = StartupState.Main
                    }
                } else {
                    _isAuthenticated.value = false
                    userPreferencesManager.clearAuthState()
                    
                    if (_isOnboardingCompleted.value) {
                        _startupState.value = StartupState.Authentication
                    } else {
                        _startupState.value = StartupState.Onboarding
                    }
                }
            } catch (e: Exception) {
                _authError.value = "Authentication check failed: ${e.message}"
                _isAuthenticated.value = false
            } finally {
                _authLoading.value = false
            }
        }
    }

    /**
     * Clear authentication state (e.g., on sign out)
     */
    fun clearAuthState() {
        viewModelScope.launch {
            try {
                userPreferencesManager.clearAuthState()
                _isAuthenticated.value = false
                _authError.value = null
                
                if (_isOnboardingCompleted.value) {
                    _startupState.value = StartupState.Authentication
                } else {
                    _startupState.value = StartupState.Onboarding
                }
            } catch (e: Exception) {
                _authError.value = "Failed to clear auth state: ${e.message}"
            }
        }
    }

    /**
     * Set onboarding as completed
     */
    fun setOnboardingCompleted() {
        viewModelScope.launch {
            try {
                userPreferencesManager.setOnboardingCompleted(true)
                _isOnboardingCompleted.value = true
                
                // Check authentication status after onboarding
                if (_isAuthenticated.value) {
                    _startupState.value = StartupState.Main
                } else {
                    _startupState.value = StartupState.Authentication
                }
            } catch (e: Exception) {
                _authError.value = "Failed to complete onboarding: ${e.message}"
            }
        }
    }

    /**
     * Retry authentication validation
     */
    fun retryAuthValidation() {
        validateSessionOnStartup()
    }

    /**
     * Clear authentication error
     */
    fun clearAuthError() {
        _authError.value = null
    }

    /**
     * Reset ViewModel to initial state and clear all cached data
     * This is called during complete sign-out to force fresh startup
     */
    fun resetToInitialState() {
        viewModelScope.launch {
            try {
                // Clear all stored data and set force account selection flag
                userPreferencesManager.clearAllData()
                
                // Reset all state flows to initial values
                _isOnboardingCompleted.value = false
                _isAuthenticated.value = false
                _authLoading.value = false
                _authError.value = null
                _startupState.value = StartupState.Loading
                
                Log.d("StartupViewModel", "Reset to initial state completed")
            } catch (e: Exception) {
                Log.e("StartupViewModel", "Error resetting to initial state", e)
                _authError.value = "Failed to reset app state: ${e.message}"
            }
        }
    }
}