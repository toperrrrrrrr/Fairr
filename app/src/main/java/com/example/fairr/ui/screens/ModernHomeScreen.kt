package com.example.fairr.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: ImageVector,
    val onClick: () -> Unit
)

data class RecentActivity(
    val title: String,
    val subtitle: String,
    val amount: String,
    val icon: ImageVector,
    val timestamp: String
)

data class GroupPreview(
    val name: String,
    val members: Int,
    val totalExpenses: String,
    val yourShare: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernHomeScreen(
    navController: NavController,
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToAnalytics: () -> Unit = {}
) {
    val userName = "John"
    
    val quickActions = listOf(
        QuickAction("Add Expense", Icons.Default.Add) { onNavigateToAddExpense() },
        QuickAction("Split Bill", Icons.Default.Receipt) { },
        QuickAction("Settle Up", Icons.Default.AccountBalance) { },
        QuickAction("View Groups", Icons.Default.Group) { onNavigateToGroups() }
    )
    
    val recentActivities = listOf(
        RecentActivity(
            "Coffee with Team",
            "Split between 4 people",
            "-$15.60",
            Icons.Default.Coffee,
            "2 hours ago"
        ),
        RecentActivity(
            "Grocery Shopping",
            "Shared with roommates",
            "-$67.40",
            Icons.Default.ShoppingCart,
            "Yesterday"
        ),
        RecentActivity(
            "Movie Night",
            "Split equally",
            "-$24.00",
            Icons.Default.Movie,
            "3 days ago"
        )
    )
    
    val groupPreviews = listOf(
        GroupPreview("Roommates", 3, "$234.50", "$78.17"),
        GroupPreview("Work Team", 6, "$156.80", "$26.13"),
        GroupPreview("Vacation Group", 8, "$890.25", "$111.28")
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            ModernHeader(
                userName = userName,
                onProfileClick = onNavigateToProfile
            )
        }
        
        // Stats Overview
        item {
            ModernSectionHeader(title = "Overview")
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatsCard(
                    title = "Monthly Spending",
                    value = "$1,234.56",
                    changeValue = "+12.3% from last month",
                    changePositive = true,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
                
                ModernStatsCard(
                    title = "Monthly Savings",
                    value = "$567.89",
                    changeValue = "-5.2% from last month",
                    changePositive = false,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatsCard(
                    title = "Owed to You",
                    value = "$89.75",
                    changeValue = "+15.6%",
                    changePositive = true,
                    icon = Icons.Default.AccountBalance,
                    modifier = Modifier.weight(1f)
                )
                
                ModernStatsCard(
                    title = "Groups",
                    value = "8",
                    changeValue = "+2",
                    changePositive = true,
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Quick Actions
        item {
            ModernSectionHeader(title = "Quick Actions")
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                quickActions.chunked(2).forEach { rowActions ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowActions.forEach { action ->
                            QuickActionCard(
                                title = action.title,
                                icon = action.icon,
                                onClick = action.onClick
                            )
                        }
                    }
                }
            }
        }
        
        // Recent Activity
        item {
            ModernSectionHeader(
                title = "Recent Activity",
                action = {
                    TextButton(onClick = onNavigateToAnalytics) {
                        Text(
                            text = "View All",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            ModernCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    recentActivities.forEachIndexed { index, activity ->
                        ActivityItem(activity = activity)
                        if (index < recentActivities.size - 1) {
                            ModernDivider()
                        }
                    }
                }
            }
        }
        
        // Groups Preview
        item {
            ModernSectionHeader(
                title = "Your Groups",
                action = {
                    TextButton(onClick = onNavigateToGroups) {
                        Text(
                            text = "View All",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(groupPreviews) { group ->
                    GroupPreviewCard(
                        group = group,
                        modifier = Modifier.width(280.dp)
                    )
                }
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ModernHeader(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good morning,",
                fontSize = 16.sp,
                color = TextSecondary
            )
            Text(
                text = userName,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Primary
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = AccentBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ActivityItem(
    activity: RecentActivity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = LightGray,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = IconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Text(
                text = activity.subtitle,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = activity.amount,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (activity.amount.startsWith("-")) ErrorRed else AccentGreen
            )
            Text(
                text = activity.timestamp,
                fontSize = 12.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun GroupPreviewCard(
    group: GroupPreview,
    modifier: Modifier = Modifier
) {
    ModernCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                
                Text(
                    text = "${group.members} members",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier
                        .background(
                            color = LightGray,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            
            ModernDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Total Expenses",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = group.totalExpenses,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Your Share",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = group.yourShare,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentBlue
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModernHomeScreenPreview() {
    FairrTheme {
        ModernHomeScreen(
            navController = rememberNavController()
        )
    }
} 