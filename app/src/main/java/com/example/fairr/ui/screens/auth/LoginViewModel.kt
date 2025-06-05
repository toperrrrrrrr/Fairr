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

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginUiEvent {
    data class ShowError(val message: String) : LoginUiEvent()
    data object NavigateToHome : LoginUiEvent()
}

class LoginViewModel : ViewModel() {
    private val authService = AuthService()
    
    var state by mutableStateOf(LoginState())
        private set
        
    private val _uiEvent = MutableSharedFlow<LoginUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(LoginUiEvent.ShowError("Please fill in all fields"))
            }
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = authService.signIn(email, password)) {
                is AuthResult.Success -> {
                    state = state.copy(isLoading = false)
                    _uiEvent.emit(LoginUiEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _uiEvent.emit(LoginUiEvent.ShowError(result.message))
                }
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(LoginUiEvent.ShowError("Please enter your email address"))
            }
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = authService.resetPassword(email)) {
                is AuthResult.Success -> {
                    state = state.copy(isLoading = false)
                    _uiEvent.emit(LoginUiEvent.ShowError("Password reset email sent"))
                }
                is AuthResult.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _uiEvent.emit(LoginUiEvent.ShowError(result.message))
                }
            }
        }
    }
} 