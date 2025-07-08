package com.example.fairr.ui.screens.friends

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.friends.FriendResult
import com.example.fairr.data.friends.FriendService
import com.example.fairr.data.friends.FriendActivityService
import com.example.fairr.data.friends.FriendSuggestionsService
import com.example.fairr.data.friends.FriendSuggestion
import com.example.fairr.data.model.*
import com.example.fairr.data.user.UserModerationService
import com.example.fairr.data.user.ModerationResult
import com.example.fairr.ui.model.Friend
import com.example.fairr.ui.model.FriendRequest
import com.example.fairr.ui.model.FriendStatus
import com.example.fairr.util.PerformanceOptimizer
import com.example.fairr.util.PerformanceOptimizer.launchOptimized
import com.example.fairr.util.PerformanceOptimizer.collectSafely
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class FriendsUiState {
    object Loading : FriendsUiState()
    data class Success(
        val friends: List<Friend> = emptyList(),
        val pendingRequests: List<FriendRequest> = emptyList(),
        val acceptedRequests: List<FriendRequest> = emptyList(),
        val friendActivities: List<FriendActivity> = emptyList(),
        val friendSuggestions: List<FriendSuggestion> = emptyList(),
        val emailInput: String = ""
    ) : FriendsUiState()
    data class Error(val message: String) : FriendsUiState()
}

/**
 * Sealed class for email validation results
 */
sealed class EmailValidationResult {
    object Success : EmailValidationResult()
    data class Error(val message: String) : EmailValidationResult()
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendService: FriendService,
    private val friendActivityService: FriendActivityService,
    private val friendSuggestionsService: FriendSuggestionsService,
    private val userModerationService: UserModerationService
) : ViewModel() {

    var uiState by mutableStateOf<FriendsUiState>(FriendsUiState.Loading)
        private set

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    init {
        loadFriendsAndRequests()
    }

    /**
     * Cleanup resources when ViewModel is destroyed
     */
    override fun onCleared() {
        super.onCleared()
        // Clean up any remaining resources
    }

    /**
     * Report a user - OPTIMIZED
     */
    fun reportUser(
        userId: String,
        userName: String,
        userEmail: String,
        reportType: UserReportType,
        reason: String,
        description: String
    ) {
        launchOptimized {
            when (val result = userModerationService.reportUser(
                userId, userName, userEmail, reportType, reason, description
            )) {
                is ModerationResult.Success -> {
                    _userMessage.emit(result.message)
                }
                is ModerationResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
        }
    }

    /**
     * Validates email format using Android's built-in email pattern
     * @param email The email address to validate
     * @return Validation result with specific error message if invalid
     */
    private fun validateEmail(email: String): EmailValidationResult {
        return when {
            email.isBlank() -> EmailValidationResult.Error("Please enter an email address")
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> EmailValidationResult.Error("Please enter a valid email address")
            email.length > 254 -> EmailValidationResult.Error("Email address is too long")
            email.contains("..") -> EmailValidationResult.Error("Email address contains invalid consecutive dots")
            email.startsWith(".") || email.endsWith(".") -> EmailValidationResult.Error("Email address cannot start or end with a dot")
            !email.contains("@") -> EmailValidationResult.Error("Email address must contain @ symbol")
            email.count { it == '@' } > 1 -> EmailValidationResult.Error("Email address can only contain one @ symbol")
            else -> EmailValidationResult.Success
        }
    }

    private fun loadFriendsAndRequests() {
        viewModelScope.launch {
            try {
                // Combine friends, requests, and activities flows
                combine(
                    friendService.getUserFriends(),
                    friendService.getAllFriendRequests(),
                    friendActivityService.getFriendActivities(),
                    friendSuggestionsService.getFriendSuggestions()
                ) { friends, requests, activities, suggestions ->
                    val (pending, accepted) = requests.partition { it.status == FriendStatus.PENDING }
                    FriendsUiState.Success(
                        friends = friends,
                        pendingRequests = pending,
                        acceptedRequests = accepted,
                        friendActivities = activities,
                        friendSuggestions = suggestions,
                        emailInput = (uiState as? FriendsUiState.Success)?.emailInput ?: ""
                    )
                }.catch { e ->
                    uiState = FriendsUiState.Error(e.message ?: "Failed to load friends")
                }.collect { state ->
                    uiState = state
                }
            } catch (e: Exception) {
                uiState = FriendsUiState.Error(e.message ?: "Failed to load friends")
            }
        }
    }

    fun onEmailInputChange(email: String) {
        val current = uiState
        if (current is FriendsUiState.Success) {
            uiState = current.copy(emailInput = email)
        }
    }

    fun sendFriendRequest() {
        val input = (uiState as? FriendsUiState.Success)?.emailInput ?: ""
        
        // Validate email format first
        when (val validation = validateEmail(input)) {
            is EmailValidationResult.Error -> {
                viewModelScope.launch {
                    _userMessage.emit(validation.message)
                }
                return
            }
            is EmailValidationResult.Success -> {
                // Continue with sending request
            }
        }

        viewModelScope.launch {
            when (val result = friendService.sendFriendRequest(input)) {
                is FriendResult.Success -> {
                    _userMessage.emit(result.message)
                    uiState = (uiState as FriendsUiState.Success).copy(emailInput = "")
                }
                is FriendResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = friendService.acceptFriendRequest(requestId)) {
                is FriendResult.Success -> {
                    _userMessage.emit(result.message)
                }
                is FriendResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
        }
    }

    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = friendService.rejectFriendRequest(requestId)) {
                is FriendResult.Success -> {
                    _userMessage.emit(result.message)
                }
                is FriendResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            when (val result = friendService.removeFriend(friendId)) {
                is FriendResult.Success -> {
                    _userMessage.emit(result.message)
                }
                is FriendResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
        }
    }

    fun sendFriendRequestToUser(email: String) {
        // Validate email format first
        when (val validation = validateEmail(email)) {
            is EmailValidationResult.Error -> {
                viewModelScope.launch {
                    _userMessage.emit(validation.message)
                }
                return
            }
            is EmailValidationResult.Success -> {
                // Continue with sending request
            }
        }

        viewModelScope.launch {
            when (val result = friendService.sendFriendRequest(email)) {
                is FriendResult.Success -> {
                    _userMessage.emit(result.message)
                }
                is FriendResult.Error -> {
                    _userMessage.emit(result.message)
                }
            }
        }
    }
} 