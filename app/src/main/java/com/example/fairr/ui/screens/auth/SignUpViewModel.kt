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

data class SignUpState(
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SignUpUiEvent {
    data class ShowError(val message: String) : SignUpUiEvent()
    data object NavigateToHome : SignUpUiEvent()
}

class SignUpViewModel : ViewModel() {
    private val authService = AuthService()
    
    var state by mutableStateOf(SignUpState())
        private set
        
    private val _uiEvent = MutableSharedFlow<SignUpUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            viewModelScope.launch {
                _uiEvent.emit(SignUpUiEvent.ShowError("Please fill in all fields"))
            }
            return
        }

        if (password != confirmPassword) {
            viewModelScope.launch {
                _uiEvent.emit(SignUpUiEvent.ShowError("Passwords do not match"))
            }
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            when (val result = authService.signUp(email, password)) {
                is AuthResult.Success -> {
                    state = state.copy(isLoading = false)
                    _uiEvent.emit(SignUpUiEvent.NavigateToHome)
                }
                is AuthResult.Error -> {
                    state = state.copy(
                        isLoading = false,
                        error = result.message
                    )
                    _uiEvent.emit(SignUpUiEvent.ShowError(result.message))
                }
            }
        }
    }
} 