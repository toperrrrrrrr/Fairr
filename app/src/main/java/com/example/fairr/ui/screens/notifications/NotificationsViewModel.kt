package com.example.fairr.ui.screens.notifications

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.groups.JoinRequestResult
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.groups.InviteResult
import com.example.fairr.data.model.Notification
import com.example.fairr.data.model.NotificationType
import com.example.fairr.data.notifications.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "NotificationsViewModel"

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val processingRequestId: String? = null
) {
    override fun toString(): String {
        return "NotificationsUiState(notifications.size=${notifications.size}, isLoading=$isLoading, error=$error, processingRequestId=$processingRequestId)"
    }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val groupJoinService: GroupJoinService,
    private val groupInviteService: GroupInviteService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    init {
        Log.d(TAG, "Initializing NotificationsViewModel")
        loadNotifications()
    }

    private fun loadNotifications() {
        Log.d(TAG, "Starting to load notifications")
        viewModelScope.launch {
            try {
                notificationService.getNotificationsForUser()
                    .onStart { 
                        Log.d(TAG, "Setting initial loading state")
                        _uiState.value = NotificationsUiState(isLoading = true)
                        Log.d(TAG, "New state: ${_uiState.value}")
                    }
                    .onEach { notifications ->
                        Log.d(TAG, "Received ${notifications.size} notifications")
                    }
                    .catch { e ->
                        Log.e(TAG, "Error loading notifications", e)
                        val errorMsg = e.message ?: "Failed to load notifications"
                        _uiState.value = NotificationsUiState(
                            error = errorMsg,
                            isLoading = false
                        )
                        Log.d(TAG, "Error state: ${_uiState.value}")
                    }
                    .collect { notifications ->
                        Log.d(TAG, "Updating UI with ${notifications.size} notifications")
                        _uiState.value = NotificationsUiState(
                            notifications = notifications,
                            isLoading = false
                        )
                        Log.d(TAG, "Updated state: ${_uiState.value}")
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in loadNotifications", e)
                _uiState.value = NotificationsUiState(
                    error = e.message ?: "An unexpected error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun respondToJoinRequest(notificationId: String, requestId: String, approve: Boolean) {
        Log.d(TAG, "Responding to join request: id=$requestId, approve=$approve")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(processingRequestId = requestId)
                Log.d(TAG, "Set processing state: ${_uiState.value}")

                when (val result = groupJoinService.respondToJoinRequest(requestId, approve)) {
                    is JoinRequestResult.Success -> {
                        Log.d(TAG, "Join request response successful")
                        notificationService.markNotificationAsRead(notificationId)
                        
                        val message = if (approve) "Join request approved" else "Join request rejected"
                        snackbarMessage = message
                        
                        // Update state before refreshing notifications to prevent race condition
                        _uiState.value = _uiState.value.copy(
                            processingRequestId = null,
                            notifications = _uiState.value.notifications.filter { it.id != notificationId }
                        )
                        
                        // Refresh notifications after state is updated
                        loadNotifications()
                        Log.d(TAG, "Updated state after success: ${_uiState.value}")
                    }
                    is JoinRequestResult.Error -> {
                        Log.e(TAG, "Join request response failed: ${result.message}")
                        snackbarMessage = result.message
                        _uiState.value = _uiState.value.copy(processingRequestId = null)
                        Log.d(TAG, "Updated state after error: ${_uiState.value}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in respondToJoinRequest", e)
                snackbarMessage = e.message ?: "An unexpected error occurred"
                _uiState.value = _uiState.value.copy(processingRequestId = null)
            }
        }
    }

    fun respondToInvite(notificationId: String, inviteId: String, accept: Boolean) {
        Log.d(TAG, "Responding to invite: id=$inviteId, accept=$accept")
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(processingRequestId = inviteId)
                Log.d(TAG, "Set processing state: ${_uiState.value}")

                when (val result = groupInviteService.respondToInvite(inviteId, accept)) {
                    is InviteResult.Success -> {
                        Log.d(TAG, "Invite response successful")
                        notificationService.markNotificationAsRead(notificationId)
                        
                        val message = if (accept) "Group invitation accepted" else "Group invitation declined"
                        snackbarMessage = message
                        
                        // Update state before refreshing notifications to prevent race condition
                        _uiState.value = _uiState.value.copy(
                            processingRequestId = null,
                            notifications = _uiState.value.notifications.filter { it.id != notificationId }
                        )
                        
                        // Refresh notifications after state is updated
                        loadNotifications()
                        Log.d(TAG, "Updated state after success: ${_uiState.value}")
                    }
                    is InviteResult.Error -> {
                        Log.e(TAG, "Invite response failed: ${result.message}")
                        snackbarMessage = result.message
                        _uiState.value = _uiState.value.copy(processingRequestId = null)
                        Log.d(TAG, "Updated state after error: ${_uiState.value}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error in respondToInvite", e)
                snackbarMessage = e.message ?: "An unexpected error occurred"
                _uiState.value = _uiState.value.copy(processingRequestId = null)
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Marking notification as read: $notificationId")
                notificationService.markNotificationAsRead(notificationId)
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting notification: $notificationId")
                notificationService.deleteNotification(notificationId)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting notification", e)
            }
        }
    }

    fun clearSnackbarMessage() {
        snackbarMessage = null
    }
} 