package com.example.fairr.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.auth.AuthResult
import com.example.fairr.data.auth.AuthService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: String? = null
)

sealed class AuthUiEvent {
    data class ShowMessage(val message: String) : AuthUiEvent()
    data object NavigateToHome : AuthUiEvent()
}

class AuthViewModel : ViewModel() {
    private val authService = AuthService()
    
    var state by mutableStateOf(AuthState())
        private set
        
    private val _uiEvent = MutableSharedFlow<AuthUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        // Check if user is already signed in
        state = state.copy(isAuthenticated = authService.currentUser != null)
        if (state.isAuthenticated) {
            viewModelScope.launch {
                _uiEvent.emit(AuthUiEvent.NavigateToHome)
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = authService.signIn(email, password)) {
                is AuthResult.Success -> {
                    state = state.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    _uiEvent.emit(AuthUiEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    state = state.copy(
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
            state = state.copy(isLoading = true, error = null)
            when (val result = authService.signUp(email, password)) {
                is AuthResult.Success -> {
                    state = state.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    _uiEvent.emit(AuthUiEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    state = state.copy(
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
            
            state = state.copy(isLoading = true, error = null)
            when (val result = authService.resetPassword(email)) {
                is AuthResult.Success -> {
                    state = state.copy(isLoading = false)
                    _uiEvent.emit(AuthUiEvent.ShowMessage("Password reset email sent"))
                }
                is AuthResult.Error -> {
                    state = state.copy(
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
        state = state.copy(isAuthenticated = false)
    }

    private fun setError(message: String) {
        state = state.copy(error = message)
    }
} 