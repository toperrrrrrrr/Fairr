package com.example.fairr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*

data class QuickAction(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)

data class RecentActivity(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: String,
    val timestamp: String,
    val type: ActivityType
)

enum class ActivityType {
    EXPENSE, PAYMENT, GROUP_JOINED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun ModernHomeScreen(
    navController: NavController
) {
    val quickActions = remember {
        listOf(
            QuickAction("Add Expense", Icons.Default.Receipt, "add_expense"),
            QuickAction("Create Group", Icons.Default.Group, "create_group"),
            QuickAction("Scan Receipt", Icons.Default.CameraAlt, "photo_capture"),
            QuickAction("Settle Up", Icons.Default.Payment, "settlements")
        )
    }
    
    val recentActivities = remember { getSampleActivities() }
    val userStats = remember { getSampleStats() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
    ) {
        // Welcome Header
        ModernHeader(
            title = "Welcome back, Alex",
            subtitle = "Here's your expense overview",
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Stats Overview
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Overview",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        userStats.take(2).forEach { stat ->
                            ModernStatsCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                changeValue = stat.change,
                                changePositive = stat.isPositive,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        userStats.drop(2).forEach { stat ->
                            ModernStatsCard(
                                title = stat.title,
                                value = stat.value,
                                icon = stat.icon,
                                changeValue = stat.change,
                                changePositive = stat.isPositive,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Quick Actions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Quick Actions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        quickActions.take(2).forEach { action ->
                            QuickActionCard(
                                title = action.title,
                                icon = action.icon,
                                onClick = { /* Navigate to ${action.route} */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        quickActions.drop(2).forEach { action ->
                            QuickActionCard(
                                title = action.title,
                                icon = action.icon,
                                onClick = { /* Navigate to ${action.route} */ },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Recent Activity
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Activity",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        TextButton(
                            onClick = { /* Navigate to full activity */ }
                        ) {
                            Text(
                                text = "View All",
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    ModernCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            recentActivities.take(5).forEachIndexed { index, activity ->
                                ModernListItem(
                                    title = activity.title,
                                    subtitle = activity.subtitle,
                                    leadingIcon = when (activity.type) {
                                        ActivityType.EXPENSE -> Icons.Default.Receipt
                                        ActivityType.PAYMENT -> Icons.Default.Payment
                                        ActivityType.GROUP_JOINED -> Icons.Default.Group
                                    },
                                    trailingContent = {
                                        Column(
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = activity.amount,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (activity.type == ActivityType.EXPENSE) ErrorRed else SuccessGreen
                                            )
                                            Text(
                                                text = activity.timestamp,
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                )
                                
                                if (index < recentActivities.size - 1) {
                                    HorizontalDivider(
                                        color = DividerColor,
                                        modifier = Modifier.padding(horizontal = 0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Groups Preview
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Groups",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        
                        TextButton(
                            onClick = { /* Navigate to groups */ }
                        ) {
                            Text(
                                text = "View All",
                                color = Primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(getSampleGroups()) { group ->
                            GroupPreviewCard(
                                groupName = group.name,
                                memberCount = group.memberCount,
                                balance = group.balance,
                                isOwed = group.isOwed,
                                onClick = { /* Navigate to group detail */ }
                            )
                        }
                    }
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .aspectRatio(1f)
            .then(Modifier.clickable { onClick() })
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun GroupPreviewCard(
    groupName: String,
    memberCount: Int,
    balance: String,
    isOwed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column {
            Text(
                text = groupName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "$memberCount members",
                fontSize = 12.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOwed) "You're owed" else "You owe",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                
                Text(
                    text = balance,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOwed) SuccessGreen else ErrorRed
                )
            }
        }
    }
}

// Sample Data
data class UserStat(
    val title: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val change: String,
    val isPositive: Boolean
)

data class GroupPreview(
    val name: String,
    val memberCount: Int,
    val balance: String,
    val isOwed: Boolean
)

private fun getSampleStats(): List<UserStat> {
    return listOf(
        UserStat("Total Spent", "$1,234", Icons.Default.Receipt, "+12%", false),
        UserStat("Groups", "8", Icons.Default.Group, "+2", true),
        UserStat("This Month", "$456", Icons.Default.CalendarToday, "-5%", true),
        UserStat("Balance", "$78", Icons.Default.AccountBalance, "+$23", true)
    )
}

private fun getSampleActivities(): List<RecentActivity> {
    return listOf(
        RecentActivity("1", "Dinner at Mario's", "Weekend Trip group", "-$25.50", "2h ago", ActivityType.EXPENSE),
        RecentActivity("2", "Payment received", "From Mike Chen", "+$30.00", "5h ago", ActivityType.PAYMENT),
        RecentActivity("3", "Coffee meeting", "Work Lunch group", "-$4.50", "1d ago", ActivityType.EXPENSE),
        RecentActivity("4", "Emma joined group", "Roommates", "", "2d ago", ActivityType.GROUP_JOINED),
        RecentActivity("5", "Grocery shopping", "Roommates", "-$67.23", "3d ago", ActivityType.EXPENSE)
    )
}

private fun getSampleGroups(): List<GroupPreview> {
    return listOf(
        GroupPreview("Weekend Trip", 5, "$45.20", true),
        GroupPreview("Roommates", 3, "$23.15", false),
        GroupPreview("Work Lunch", 8, "$12.50", true),
        GroupPreview("Study Group", 4, "$8.75", false)
    )
}

@Preview(showBackground = true)
@Composable
fun ModernHomeScreenPreview() {
    FairrTheme {
        ModernHomeScreen(navController = rememberNavController())
    }
} 