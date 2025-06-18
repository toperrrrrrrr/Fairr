package com.example.fairr.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.fairr.data.model.NotificationType
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                // Show loading state
                uiState.isLoading && uiState.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                // Show empty state
                uiState.notifications.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "No notifications",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Show notifications list
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.notifications) { notification ->
                            val requestId = notification.data["requestId"] as? String
                            val inviteId = notification.data["inviteId"] as? String
                            val isProcessingForItem = when {
                                uiState.processingRequestId == null -> false
                                requestId != null && uiState.processingRequestId == requestId -> true
                                inviteId != null && uiState.processingRequestId == inviteId -> true
                                else -> false
                            }
                            val outcome = uiState.decisionResults[notification.id]
                            NotificationCard(
                                notification = notification,
                                onApprove = { reqId ->
                                    viewModel.respondToJoinRequest(notification.id, reqId, true)
                                },
                                onReject = { reqId ->
                                    viewModel.respondToJoinRequest(notification.id, reqId, false)
                                },
                                onAcceptInvite = { invId ->
                                    viewModel.respondToInvite(notification.id, invId, true)
                                },
                                onDeclineInvite = { invId ->
                                    viewModel.respondToInvite(notification.id, invId, false)
                                },
                                onMarkAsRead = {
                                    viewModel.markAsRead(notification.id)
                                },
                                isProcessing = isProcessingForItem,
                                outcome = outcome
                            )
                        }
                    }
                }
            }

            // Show error if any
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text(uiState.error!!)
                }
            }
        }
    }

    // Handle snackbar messages
    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearSnackbarMessage()
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: com.example.fairr.data.model.Notification,
    onApprove: (String) -> Unit = {},
    onReject: (String) -> Unit = {},
    onAcceptInvite: (String) -> Unit = {},
    onDeclineInvite: (String) -> Unit = {},
    onMarkAsRead: () -> Unit = {},
    isProcessing: Boolean = false,
    outcome: String? = null,
    modifier: Modifier = Modifier
) {
    val isJoinRequest = notification.type == NotificationType.GROUP_JOIN_REQUEST
    val isInvitation = notification.type == NotificationType.GROUP_INVITATION
    val requestId = notification.data["requestId"] as? String
    val inviteId = notification.data["inviteId"] as? String
    val currentProcessing = isProcessing && (requestId != null || inviteId != null)
    val cardPadding = if (notification.isRead) 8.dp else 16.dp

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = formatTimestamp(notification.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                if (!notification.isRead) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                    )
                }
            }
            
            // Show action buttons for group join requests (only if not already processed)
            if (isJoinRequest && requestId != null && !notification.isRead) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onReject(requestId) },
                        modifier = Modifier.weight(1f),
                        enabled = !currentProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (currentProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Reject")
                        }
                    }
                    
                    Button(
                        onClick = { onApprove(requestId) },
                        modifier = Modifier.weight(1f),
                        enabled = !currentProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (currentProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Approve")
                        }
                    }
                }
            }
            
            // Show action buttons for group invitations (only if not already processed)
            if (isInvitation && inviteId != null && !notification.isRead) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDeclineInvite(inviteId) },
                        modifier = Modifier.weight(1f),
                        enabled = !currentProcessing,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (currentProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text("Decline")
                        }
                    }
                    
                    Button(
                        onClick = { onAcceptInvite(inviteId) },
                        modifier = Modifier.weight(1f),
                        enabled = !currentProcessing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (currentProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Accept")
                        }
                    }
                }
            }
            
            // If the notification has been processed, show a subtle status instead of buttons
            if ((isJoinRequest || isInvitation) && notification.isRead) {
                Spacer(modifier = Modifier.height(16.dp))
                val displayOutcome = outcome ?: "Handled"
                Text(
                    text = displayOutcome,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
    
    // Mark as read when tapped (for non-action notifications)
    LaunchedEffect(Unit) {
        if (!isJoinRequest && !isInvitation && !notification.isRead) {
            onMarkAsRead()
        }
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val now = System.currentTimeMillis()
    val time = timestamp.toDate().time
    val diff = now - time
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> "${diff / 604800_000}w ago"
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    FairrTheme {
        NotificationsScreen(
            navController = rememberNavController()
        )
    }
} 
