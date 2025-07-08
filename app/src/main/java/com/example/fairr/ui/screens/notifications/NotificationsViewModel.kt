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
import com.example.fairr.data.groups.GroupInviteResult
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
    val processingRequestId: String? = null,
    val decisionResults: Map<String, String> = emptyMap()
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
                        _uiState.value = _uiState.value.copy(isLoading = true)
                        Log.d(TAG, "New state: ${_uiState.value}")
                    }
                    .onEach { notifications ->
                        Log.d(TAG, "Received ${notifications.size} notifications")
                    }
                    .catch { e ->
                        Log.e(TAG, "Error loading notifications", e)
                        val errorMsg = e.message ?: "Failed to load notifications"
                        _uiState.value = _uiState.value.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                        Log.d(TAG, "Error state: ${_uiState.value}")
                    }
                    .collect { notifications ->
                        Log.d(TAG, "Updating UI with ${notifications.size} notifications")
                        _uiState.value = _uiState.value.copy(
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
                        
                        _uiState.value = _uiState.value.copy(
                            processingRequestId = null,
                            decisionResults = _uiState.value.decisionResults + (notificationId to if (approve) "Accepted" else "Declined"),
                            notifications = _uiState.value.notifications.map { n ->
                                if (n.id == notificationId) n.copy(isRead = true) else n
                            }
                        )
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
        Log.d(TAG, "Responding to invite: notificationId=$notificationId, inviteId=$inviteId, accept=$accept")
        
        if (inviteId.isEmpty()) {
            Log.e(TAG, "InviteId is empty or null")
            snackbarMessage = "Invalid invitation - missing invite ID"
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(processingRequestId = inviteId)
                Log.d(TAG, "Set processing state: ${_uiState.value}")

                val result = if (accept) {
                    Log.d(TAG, "Calling acceptInvitation with inviteId: $inviteId")
                    groupInviteService.acceptInvitation(inviteId)
                } else {
                    Log.d(TAG, "Calling rejectInvitation with inviteId: $inviteId")
                    groupInviteService.rejectInvitation(inviteId)
                }

                when (result) {
                    is GroupInviteResult.Success -> {
                        Log.d(TAG, "Invite response successful: ${result.message}")
                        notificationService.markNotificationAsRead(notificationId)
                        
                        val message = if (accept) "Group invitation accepted" else "Group invitation declined"
                        snackbarMessage = message
                        
                        _uiState.value = _uiState.value.copy(
                            processingRequestId = null,
                            decisionResults = _uiState.value.decisionResults + (notificationId to if (accept) "Accepted" else "Declined"),
                            notifications = _uiState.value.notifications.map { n ->
                                if (n.id == notificationId) n.copy(isRead = true) else n
                            }
                        )
                        Log.d(TAG, "Updated state after success: ${_uiState.value}")
                    }
                    is GroupInviteResult.Error -> {
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

    fun dismissNotification(notificationId: String) {
        deleteNotification(notificationId)
    }

    fun clearSnackbarMessage() {
        snackbarMessage = null
    }

    fun retry() {
        Log.d(TAG, "Retrying to load notifications")
        // Clear error state and reload
        _uiState.value = _uiState.value.copy(error = null)
        loadNotifications()
    }

    fun refresh() {
        Log.d(TAG, "Refreshing notifications")
        loadNotifications()
    }

    fun markAllAsRead() {
        Log.d(TAG, "Marking all notifications as read")
        viewModelScope.launch {
            try {
                val success = notificationService.markAllAsRead()
                if (success) {
                    snackbarMessage = "All notifications marked as read"
                    Log.d(TAG, "All notifications marked as read")
                } else {
                    snackbarMessage = "Failed to mark all notifications as read"
                    Log.e(TAG, "Failed to mark all notifications as read")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error marking all notifications as read", e)
                snackbarMessage = "Error marking all notifications as read: ${e.message}"
            }
        }
    }
} 