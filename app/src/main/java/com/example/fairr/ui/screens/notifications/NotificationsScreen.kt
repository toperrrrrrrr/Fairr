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
import com.example.fairr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType(val icon: ImageVector, val color: Color) {
    EXPENSE_ADDED(Icons.Default.Receipt, DarkGreen),
    GROUP_INVITE(Icons.Default.Group, Primary),
    SETTLEMENT_REQUEST(Icons.Default.AccountBalance, WarningOrange),
    REMINDER(Icons.Default.Notifications, AccentBlue),
    SYSTEM(Icons.Default.Info, TextSecondary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController
) {
    // Sample notifications data - in real app this would come from ViewModel
    val notifications = remember {
        listOf(
            NotificationItem(
                id = "1",
                title = "New Expense Added",
                message = "John added \"Dinner at Restaurant\" (₱850.00) to Family Trip",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 30), // 30 min ago
                type = NotificationType.EXPENSE_ADDED,
                isRead = false
            ),
            NotificationItem(
                id = "2",
                title = "Group Invitation",
                message = "Sarah invited you to join \"Office Lunch Group\"",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 2), // 2 hours ago
                type = NotificationType.GROUP_INVITE,
                isRead = false
            ),
            NotificationItem(
                id = "3",
                title = "Settlement Request",
                message = "You owe ₱425.50 to Alex for shared expenses",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24), // 1 day ago
                type = NotificationType.SETTLEMENT_REQUEST,
                isRead = true
            ),
            NotificationItem(
                id = "4",
                title = "Reminder",
                message = "Don't forget to add your coffee expenses from this morning",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2), // 2 days ago
                type = NotificationType.REMINDER,
                isRead = true
            ),
            NotificationItem(
                id = "5",
                title = "System Update",
                message = "Fairr has been updated with new features and improvements",
                timestamp = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 3), // 3 days ago
                type = NotificationType.SYSTEM,
                isRead = true
            )
        )
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Notifications",
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
                                    text = unreadCount.toString(),
                                    fontSize = 10.sp,
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
                    if (unreadCount > 0) {
                        TextButton(
                            onClick = { /* Mark all as read */ }
                        ) {
                            Text(
                                "Mark all read",
                                color = Primary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { paddingValues ->
        if (notifications.isEmpty()) {
            // Empty state
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
        } else {
            // Notifications list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = { /* Handle notification click */ }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) NeutralWhite else Primary.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(notification.type.color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notification.type.icon,
                    contentDescription = null,
                    tint = notification.type.color,
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
                        fontSize = 14.sp,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Primary)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = formatRelativeTime(notification.timestamp),
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

fun formatRelativeTime(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    val diffInMinutes = diffInMillis / (1000 * 60)
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24

    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "${diffInMinutes}m ago"
        diffInHours < 24 -> "${diffInHours}h ago"
        diffInDays < 7 -> "${diffInDays}d ago"
        else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
} 