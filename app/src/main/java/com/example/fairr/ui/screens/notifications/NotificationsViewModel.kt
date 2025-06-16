package com.example.fairr.ui.screens.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.groups.JoinRequestResult
import com.example.fairr.data.model.Notification
import com.example.fairr.data.model.NotificationType
import com.example.fairr.data.notifications.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val processingRequestId: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationService: NotificationService,
    private val groupJoinService: GroupJoinService
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                notificationService.getNotificationsForUser().collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load notifications"
                )
            }
        }
    }

    fun respondToJoinRequest(notificationId: String, requestId: String, approve: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(processingRequestId = requestId)

            when (val result = groupJoinService.respondToJoinRequest(requestId, approve)) {
                is JoinRequestResult.Success -> {
                    // Mark notification as read
                    notificationService.markNotificationAsRead(notificationId)
                    
                    val message = if (approve) "Join request approved" else "Join request rejected"
                    snackbarMessage = message
                    
                    _uiState.value = _uiState.value.copy(processingRequestId = null)
                }
                is JoinRequestResult.Error -> {
                    snackbarMessage = result.message
                    _uiState.value = _uiState.value.copy(processingRequestId = null)
                }
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationService.markNotificationAsRead(notificationId)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationService.deleteNotification(notificationId)
        }
    }

    fun clearSnackbarMessage() {
        snackbarMessage = null
    }
} 