package com.example.fairr.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import com.example.fairr.ui.theme.*
import com.example.fairr.data.model.Notification
import com.example.fairr.data.model.NotificationType
import com.google.firebase.Timestamp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.SimpleDateFormat
import java.util.*

// UI model that extends the data model with display properties
data class NotificationUiModel(
    val notification: Notification,
    val icon: ImageVector,
    val iconColor: Color,
    val iconBackgroundColor: Color,
    val canTakeAction: Boolean = false,
    val actionLabel: String? = null,
    val secondaryActionLabel: String? = null
)

// Extension function to convert data model to UI model
fun Notification.toUiModel(): NotificationUiModel {
    return when (type) {
        NotificationType.GROUP_JOIN_REQUEST -> NotificationUiModel(
            notification = this,
            icon = Icons.Default.PersonAdd,
            iconColor = ComponentColors.Info,
            iconBackgroundColor = ComponentColors.IconBackgroundInfo,
            canTakeAction = true,
            actionLabel = "Accept",
            secondaryActionLabel = "Decline"
        )
        NotificationType.GROUP_INVITATION -> NotificationUiModel(
            notification = this,
            icon = Icons.Default.Group,
            iconColor = ComponentColors.Success,
            iconBackgroundColor = ComponentColors.IconBackgroundSuccess,
            canTakeAction = true,
            actionLabel = "Accept",
            secondaryActionLabel = "Decline"
        )
        NotificationType.FRIEND_REQUEST -> NotificationUiModel(
            notification = this,
            icon = Icons.Default.PersonAdd,
            iconColor = AccentBlue,
            iconBackgroundColor = AccentBlue.copy(alpha = 0.1f),
            canTakeAction = true,
            actionLabel = "Accept",
            secondaryActionLabel = "Decline"
        )
        NotificationType.EXPENSE_ADDED -> NotificationUiModel(
            notification = this,
            icon = Icons.Default.Receipt,
            iconColor = ComponentColors.Success,
            iconBackgroundColor = ComponentColors.IconBackgroundSuccess
        )
        NotificationType.SETTLEMENT_REMINDER -> NotificationUiModel(
            notification = this,
            icon = Icons.Default.AccountBalance,
            iconColor = ComponentColors.Warning,
            iconBackgroundColor = ComponentColors.IconBackgroundWarning
        )
        NotificationType.UNKNOWN -> NotificationUiModel(
            notification = this,
            icon = Icons.Default.Info,
            iconColor = TextSecondary,
            iconBackgroundColor = TextSecondary.copy(alpha = 0.1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage = viewModel.snackbarMessage
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val notificationUiModels = remember(uiState.notifications) {
        uiState.notifications.map { it.toUiModel() }
    }

    val unreadCount = remember(uiState.notifications) {
        uiState.notifications.count { !it.isRead }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = Primary,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Text(
                                    text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0 && !uiState.isLoading) {
                        TextButton(
                            onClick = { 
                                // Mark all as read functionality
                                uiState.notifications
                                    .filter { !it.isRead }
                                    .forEach { viewModel.markAsRead(it.id) }
                            }
                        ) {
                            Text(
                                "Mark all read",
                                style = MaterialTheme.typography.labelMedium,
                                color = Primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.notifications.isEmpty() -> {
                    // Initial loading state
                    LoadingState()
                }
                uiState.error != null -> {
                    // Error state
                    ErrorState(
                        error = uiState.error ?: "Unknown error",
                        onRetry = { viewModel.retry() }
                    )
                }
                uiState.notifications.isEmpty() -> {
                    // Empty state
                    EmptyState()
                }
                else -> {
                    // Notifications list with pull-to-refresh
                    val swipeRefreshState = rememberSwipeRefreshState(
                        isRefreshing = uiState.isLoading && uiState.notifications.isNotEmpty()
                    )
                    
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = notificationUiModels,
                                key = { it.notification.id }
                            ) { notificationUiModel ->
                                NotificationCard(
                                    notificationUiModel = notificationUiModel,
                                    isProcessing = uiState.processingRequestId == getActionId(notificationUiModel.notification),
                                    decisionResult = uiState.decisionResults[notificationUiModel.notification.id],
                                    onMarkAsRead = { viewModel.markAsRead(notificationUiModel.notification.id) },
                                    onAction = { accept ->
                                        handleNotificationAction(
                                            notification = notificationUiModel.notification,
                                            accept = accept,
                                            viewModel = viewModel
                                        )
                                    },
                                    onDelete = { viewModel.deleteNotification(notificationUiModel.notification.id) }
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Primary,
            strokeWidth = 3.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Loading notifications...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = ComponentColors.Error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Unable to Load Notifications",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary,
                contentColor = Color.White
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "No notifications",
            modifier = Modifier.size(64.dp),
            tint = TextSecondary.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Notifications Yet",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You'll see notifications about group activities, new expenses, and settlement requests here.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = TextSecondary,
            lineHeight = 20.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notificationUiModel: NotificationUiModel,
    isProcessing: Boolean,
    decisionResult: String?,
    onMarkAsRead: () -> Unit,
    onAction: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val notification = notificationUiModel.notification
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { 
            if (!notification.isRead) {
                onMarkAsRead()
            }
        },
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                BackgroundPrimary
            } else {
                Primary.copy(alpha = 0.03f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (notification.isRead) 1.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (!notification.isRead) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(notificationUiModel.iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = notificationUiModel.icon,
                        contentDescription = null,
                        tint = notificationUiModel.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatRelativeTime(notification.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            
                            if (!notification.isRead) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
            
            // Action buttons or decision result
            if (decisionResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (decisionResult) {
                        "Accepted" -> ComponentColors.SuccessLight
                        "Declined" -> ComponentColors.ErrorLight
                        else -> ComponentColors.InfoLight
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = decisionResult,
                        style = MaterialTheme.typography.labelMedium,
                        color = when (decisionResult) {
                            "Accepted" -> ComponentColors.Success
                            "Declined" -> ComponentColors.Error
                            else -> ComponentColors.Info
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            } else if (notificationUiModel.canTakeAction && !notification.isRead) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Accept button
                    Button(
                        onClick = { onAction(true) },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ComponentColors.Success,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = notificationUiModel.actionLabel ?: "Accept",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    
                    // Decline button
                    OutlinedButton(
                        onClick = { onAction(false) },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ComponentColors.Error
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, ComponentColors.Error
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = notificationUiModel.secondaryActionLabel ?: "Decline",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (1000 * 60)
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24

    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "${diffInMinutes}m"
        diffInHours < 24 -> "${diffInHours}h"
        diffInDays < 7 -> "${diffInDays}d"
        diffInDays < 30 -> "${diffInDays / 7}w"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}

private fun getActionId(notification: Notification): String {
    return when (notification.type) {
        NotificationType.GROUP_JOIN_REQUEST -> notification.data["requestId"] as? String ?: ""
        NotificationType.GROUP_INVITATION -> notification.data["inviteId"] as? String ?: ""
        NotificationType.FRIEND_REQUEST -> notification.data["requestId"] as? String ?: ""
        else -> ""
    }
}

private fun handleNotificationAction(
    notification: Notification,
    accept: Boolean,
    viewModel: NotificationsViewModel
) {
    when (notification.type) {
        NotificationType.GROUP_JOIN_REQUEST -> {
            val requestId = notification.data["requestId"] as? String
            if (requestId != null) {
                viewModel.respondToJoinRequest(notification.id, requestId, accept)
            }
        }
        NotificationType.GROUP_INVITATION -> {
            val inviteId = notification.data["inviteId"] as? String
            if (inviteId != null) {
                viewModel.respondToInvite(notification.id, inviteId, accept)
            }
        }
        NotificationType.FRIEND_REQUEST -> {
            // TODO: Implement friend request handling
        }
        else -> {
            // No action needed
        }
    }
} 