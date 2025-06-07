package com.example.fairr.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserState(
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            auth.currentUser?.let { user ->
                _userState.value = UserState(
                    displayName = user.displayName ?: user.email?.substringBefore("@"),
                    email = user.email,
                    photoUrl = user.photoUrl?.toString()
                )
            }
        }
    }
} 