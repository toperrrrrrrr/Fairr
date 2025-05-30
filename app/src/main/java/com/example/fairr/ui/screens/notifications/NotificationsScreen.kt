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
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    onNotificationClick: (String) -> Unit = {}
) {
    // Sample notification data
    val notifications = remember {
        listOf(
            NotificationItem(
                id = "1",
                type = NotificationType.EXPENSE_ADDED,
                title = "New expense added",
                message = "Alice added \"Dinner at Restaurant\" ($45.00) to Weekend Trip",
                time = "2 hours ago",
                isRead = false,
                icon = Icons.Default.Receipt
            ),
            NotificationItem(
                id = "2",
                type = NotificationType.PAYMENT_REQUEST,
                title = "Payment request",
                message = "Bob is asking you to settle up $25.50 in Apartment Rent",
                time = "5 hours ago",
                isRead = false,
                icon = Icons.Default.Payment
            ),
            NotificationItem(
                id = "3",
                type = NotificationType.GROUP_ACTIVITY,
                title = "Joined group",
                message = "Charlie joined Office Lunch group",
                time = "1 day ago",
                isRead = true,
                icon = Icons.Default.Group
            ),
            NotificationItem(
                id = "4",
                type = NotificationType.EXPENSE_ADDED,
                title = "New expense added",
                message = "You added \"Gas Station\" ($30.00) to Weekend Trip",
                time = "2 days ago",
                isRead = true,
                icon = Icons.Default.Receipt
            ),
            NotificationItem(
                id = "5",
                type = NotificationType.PAYMENT_REQUEST,
                title = "Payment settled",
                message = "Diana settled up $15.25 with you",
                time = "3 days ago",
                isRead = true,
                icon = Icons.Default.Payment
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Notifications",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            // Empty state
            EmptyNotificationsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Today Section
                val todayNotifications = notifications.filter { it.time.contains("hour") }
                if (todayNotifications.isNotEmpty()) {
                    item {
                        Text(
                            text = "Today",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(todayNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = { onNotificationClick(notification.id) }
                        )
                    }
                }
                
                // Earlier Section
                val earlierNotifications = notifications.filter { !it.time.contains("hour") }
                if (earlierNotifications.isNotEmpty()) {
                    item {
                        Text(
                            text = "Earlier",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(earlierNotifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onClick = { onNotificationClick(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) PureWhite else DarkBlue.copy(alpha = 0.03f)
        )
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
                    .background(
                        when (notification.type) {
                            NotificationType.EXPENSE_ADDED -> DarkGreen.copy(alpha = 0.1f)
                            NotificationType.PAYMENT_REQUEST -> DarkBlue.copy(alpha = 0.1f)
                            NotificationType.GROUP_ACTIVITY -> PlaceholderText.copy(alpha = 0.1f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    notification.icon,
                    contentDescription = notification.type.name,
                    tint = when (notification.type) {
                        NotificationType.EXPENSE_ADDED -> DarkGreen
                        NotificationType.PAYMENT_REQUEST -> DarkBlue
                        NotificationType.GROUP_ACTIVITY -> PlaceholderText
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 16.sp,
                        fontWeight = if (notification.isRead) FontWeight.Medium else FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(DarkBlue, CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = notification.time,
                    fontSize = 12.sp,
                    color = PlaceholderText
                )
            }
        }
    }
}

@Composable
fun EmptyNotificationsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(LightBackground)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    PlaceholderText.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = "No notifications",
                modifier = Modifier.size(48.dp),
                tint = PlaceholderText
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Notifications",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "You're all caught up! New notifications will appear here when there's activity in your groups.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )
    }
}

// Data classes and enums
enum class NotificationType {
    EXPENSE_ADDED,
    PAYMENT_REQUEST,
    GROUP_ACTIVITY
}

data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val time: String,
    val isRead: Boolean,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    FairrTheme {
        NotificationsScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyNotificationsStatePreview() {
    FairrTheme {
        EmptyNotificationsState()
    }
} 