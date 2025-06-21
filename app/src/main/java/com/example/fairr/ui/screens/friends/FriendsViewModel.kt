package com.example.fairr.ui.screens.friends

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.friends.FriendResult
import com.example.fairr.data.friends.FriendService
import com.example.fairr.ui.model.Friend
import com.example.fairr.ui.model.FriendRequest
import com.example.fairr.ui.model.FriendStatus
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
        val emailInput: String = ""
    ) : FriendsUiState()
    data class Error(val message: String) : FriendsUiState()
}

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val friendService: FriendService
) : ViewModel() {

    var uiState by mutableStateOf<FriendsUiState>(FriendsUiState.Loading)
        private set

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    init {
        loadFriendsAndRequests()
    }

    private fun loadFriendsAndRequests() {
        viewModelScope.launch {
            try {
                // Combine both friends and requests flows
                combine(
                    friendService.getUserFriends(),
                    friendService.getAllFriendRequests()
                ) { friends, requests ->
                    val (pending, accepted) = requests.partition { it.status == FriendStatus.PENDING }
                    FriendsUiState.Success(
                        friends = friends,
                        pendingRequests = pending,
                        acceptedRequests = accepted,
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
        if (input.isBlank()) {
            viewModelScope.launch {
                _userMessage.emit("Please enter an email address")
            }
            return
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
} 