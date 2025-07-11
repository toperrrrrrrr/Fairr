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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import java.text.SimpleDateFormat
import java.util.*
import com.example.fairr.ui.components.ModernCard
import android.util.Log

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
        NotificationType.GROUP_INVITATION -> {
            // Check if the invite has already been accepted or declined
            val inviteStatus = data["status"] as? String
            val isAlreadyProcessed = inviteStatus in listOf("ACCEPTED", "DECLINED", "REJECTED")
            
            NotificationUiModel(
                notification = this,
                icon = Icons.Default.Group,
                iconColor = ComponentColors.Success,
                iconBackgroundColor = ComponentColors.IconBackgroundSuccess,
                canTakeAction = !isAlreadyProcessed, // Only show buttons if not already processed
                actionLabel = "Accept",
                secondaryActionLabel = "Decline"
            )
        }
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
    @Suppress("UNUSED_PARAMETER") navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarMessage = viewModel.snackbarMessage
    val snackbarHostState = remember { SnackbarHostState() }

    // Debug logging
    LaunchedEffect(Unit) {
        Log.d("NotificationsScreen", "NotificationsScreen composable started")
    }

    // Handle snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            Log.d("NotificationsScreen", "Showing snackbar message: $it")
            snackbarHostState.showSnackbar(it)
        }
    }

    val notificationUiModels = remember(uiState.notifications) {
        Log.d("NotificationsScreen", "Creating notification UI models. Count: ${uiState.notifications.size}")
        uiState.notifications.forEach { notification ->
            Log.d("NotificationsScreen", "Notification: id=${notification.id}, type=${notification.type}, data=${notification.data}")
        }
        uiState.notifications.map { it.toUiModel() }
    }

    val unreadCount = remember(uiState.notifications) {
        uiState.notifications.count { !it.isRead }
    }

    Log.d("NotificationsScreen", "Rendering notifications screen. Notifications: ${uiState.notifications.size}, Unread: $unreadCount, Loading: ${uiState.isLoading}, Error: ${uiState.error}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Primary, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Test button to create a sample notification
                    IconButton(
                        onClick = {
                            Log.d("NotificationsScreen", "Creating test notification")
                            viewModel.createTestNotification()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create test notification",
                            tint = TextPrimary
                        )
                    }
                    
                    if (uiState.notifications.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.markAllAsRead() }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        // Remove the debug floating action buttons
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundPrimary
    ) { innerPadding ->
        val pullToRefreshState = rememberPullToRefreshState()
        
        LaunchedEffect(pullToRefreshState.isRefreshing) {
            if (pullToRefreshState.isRefreshing) {
                viewModel.refresh()
            }
        }
        
        LaunchedEffect(uiState.isLoading) {
            if (!uiState.isLoading) {
                pullToRefreshState.endRefresh()
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when {
                uiState.isLoading && uiState.notifications.isEmpty() -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.retry() }
                    )
                }
                uiState.notifications.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = notificationUiModels,
                            key = { it.notification.id }
                        ) { notificationUiModel ->
                            NotificationCard(
                                notificationUiModel = notificationUiModel,
                                isProcessing = uiState.processingRequestId == notificationUiModel.notification.id,
                                decisionResult = uiState.decisionResults[notificationUiModel.notification.id],
                                onMarkAsRead = {
                                    if (!notificationUiModel.notification.isRead) {
                                        viewModel.markAsRead(notificationUiModel.notification.id)
                                    }
                                },
                                onAction = { accept ->
                                    Log.d("NotificationsScreen", "Action triggered: accept=$accept, notificationType=${notificationUiModel.notification.type}")
                                    when (notificationUiModel.notification.type) {
                                        NotificationType.GROUP_JOIN_REQUEST -> {
                                            val requestId = notificationUiModel.notification.data["requestId"] as? String ?: ""
                                            Log.d("NotificationsScreen", "Handling GROUP_JOIN_REQUEST: requestId='$requestId'")
                                            viewModel.respondToJoinRequest(
                                                notificationUiModel.notification.id,
                                                requestId,
                                                accept
                                            )
                                        }
                                        NotificationType.GROUP_INVITATION -> {
                                            val inviteId = notificationUiModel.notification.data["inviteId"] as? String ?: ""
                                            Log.d("NotificationsScreen", "GROUP_INVITATION action - NotificationId: ${notificationUiModel.notification.id}, InviteId: '$inviteId', Data: ${notificationUiModel.notification.data}")
                                            
                                            if (inviteId.isEmpty()) {
                                                Log.e("NotificationsScreen", "InviteId is empty for GROUP_INVITATION notification")
                                            }
                                            
                                            Log.d("NotificationsScreen", "Calling respondToInvite with accept=$accept")
                                            viewModel.respondToInvite(
                                                notificationUiModel.notification.id,
                                                inviteId,
                                                accept
                                            )
                                        }
                                        NotificationType.FRIEND_REQUEST -> {
                                            // TODO: Implement friend request handling
                                            Log.d("NotificationsScreen", "Friend request handling not implemented yet")
                                            viewModel.snackbarMessage = "Friend request handling not implemented yet"
                                        }
                                        else -> {
                                            Log.d("NotificationsScreen", "No action needed for notification type: ${notificationUiModel.notification.type}")
                                            // No action needed for other types
                                        }
                                    }
                                },
                                onDelete = {
                                    viewModel.deleteNotification(notificationUiModel.notification.id)
                                }
                            )
                        }
                    }
                }
            }
            
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Clear snackbar message after showing
    LaunchedEffect(snackbarMessage) {
        if (snackbarMessage != null) {
            viewModel.clearSnackbarMessage()
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
    
    ModernCard(
        onClick = { 
            if (!notification.isRead) {
                onMarkAsRead()
            }
        },
        backgroundColor = if (notification.isRead) {
            CardBackground
        } else {
            Primary.copy(alpha = 0.03f)
        },
        shadowElevation = if (notification.isRead) 1 else 2,
        cornerRadius = 12
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = notificationUiModel.iconBackgroundColor,
                        shape = RoundedCornerShape(10.dp)
                    ),
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
                                    .background(
                                        color = Primary,
                                        shape = CircleShape
                                    )
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
                
                // Action buttons or decision result
                if (decisionResult != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (decisionResult) {
                            "Accepted" -> ComponentColors.Success.copy(alpha = 0.1f)
                            "Declined" -> ComponentColors.Error.copy(alpha = 0.1f)
                            else -> ComponentColors.Info.copy(alpha = 0.1f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = decisionResult,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
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
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium
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
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
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