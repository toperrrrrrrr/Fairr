package com.example.fairr.ui.screens.auth

import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.auth.AuthResult
import com.example.fairr.data.auth.AuthService
import com.example.fairr.data.auth.GoogleAuthService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService,
    private val googleAuthService: GoogleAuthService
) : ViewModel() {
    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent: SharedFlow<AuthUiEvent> = _uiEvent.asSharedFlow()

    init {
        // Check if user is already signed in
        _state.value = _state.value.copy(isAuthenticated = authService.currentUser != null)
        if (_state.value.isAuthenticated) {
            viewModelScope.launch {
                _uiEvent.emit(AuthUiEvent.NavigateToHome)
            }
        }

        // Initialize Google Sign-In with the web client ID from google-services.json
        googleAuthService.initialize("670995472503-mtt69kbdep2jhesjtem9dip37qngc1i5.apps.googleusercontent.com")
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = authService.signIn(email, password)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    _uiEvent.emit(AuthUiEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _uiEvent.emit(AuthUiEvent.ShowMessage(result.message))
                }
            }
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = authService.signUp(email, password)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    _uiEvent.emit(AuthUiEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _uiEvent.emit(AuthUiEvent.ShowMessage(result.message))
                }
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            if (email.isBlank()) {
                setError("Please enter your email address")
                _uiEvent.emit(AuthUiEvent.ShowMessage("Please enter your email address"))
                return@launch
            }
            
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = authService.resetPassword(email)) {
                is AuthResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvent.emit(AuthUiEvent.ShowMessage("Password reset email sent"))
                }
                is AuthResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _uiEvent.emit(AuthUiEvent.ShowMessage(result.message))
                }
            }
        }
    }

    fun signOut() {
        authService.signOut()
        _state.value = _state.value.copy(isAuthenticated = false)
    }

    private fun setError(message: String) {
        _state.value = _state.value.copy(error = message)
    }

    fun showMessage(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.ShowMessage(message))
        }
    }

    fun signInWithGoogle() {
        _state.value = _state.value.copy(isLoading = true)
        val signInIntent = googleAuthService.getSignInIntent()
        // This will be handled by the activity
        viewModelScope.launch {
            _uiEvent.emit(AuthUiEvent.LaunchGoogleSignIn(signInIntent))
        }
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    googleAuthService.firebaseAuthWithGoogle(token)
                        .onSuccess {
                            _uiEvent.emit(AuthUiEvent.NavigateToHome)
                        }
                        .onFailure { e ->
                            _uiEvent.emit(AuthUiEvent.ShowMessage(e.message ?: "Google sign in failed"))
                        }
                }
            } catch (e: ApiException) {
                _uiEvent.emit(AuthUiEvent.ShowMessage("Google sign in failed: ${e.message}"))
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

sealed class AuthUiEvent {
    data class ShowMessage(val message: String) : AuthUiEvent()
    data object NavigateToHome : AuthUiEvent()
    data class LaunchGoogleSignIn(val intent: Intent) : AuthUiEvent()
} 