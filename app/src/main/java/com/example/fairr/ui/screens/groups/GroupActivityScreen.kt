package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.FairrEmptyState
import com.example.fairr.ui.components.FairrFilterChip
import com.example.fairr.ui.components.FairrLoadingCard
import com.example.fairr.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fairr.ui.screens.groups.ActivityViewModel
import com.example.fairr.data.model.GroupActivity
import com.example.fairr.data.model.ActivityType
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning

// Extension function to get display properties for ActivityType
fun ActivityType.getDisplayProperties(): Triple<String, ImageVector, Color> {
    return when (this) {
        ActivityType.EXPENSE_ADDED -> Triple("Expense Added", Icons.Default.Add, DarkGreen)
        ActivityType.EXPENSE_EDITED -> Triple("Expense Updated", Icons.Default.Edit, DarkBlue)
        ActivityType.EXPENSE_DELETED -> Triple("Expense Deleted", Icons.Default.Delete, ErrorRed)
        ActivityType.MEMBER_ADDED -> Triple("Member Joined", Icons.Default.PersonAdd, DarkGreen)
        ActivityType.MEMBER_REMOVED -> Triple("Member Left", Icons.Default.PersonRemove, WarningOrange)
        ActivityType.SETTLEMENT_MADE -> Triple("Payment Made", Icons.Default.Payment, SuccessGreen)
        ActivityType.GROUP_CREATED -> Triple("Group Created", Icons.Default.Group, DarkGreen)
        ActivityType.GROUP_SETTINGS_UPDATED -> Triple("Group Updated", Icons.Default.Settings, DarkBlue)
        ActivityType.COMMENT_ADDED -> Triple("Comment Added", Icons.AutoMirrored.Filled.Chat, DarkBlue)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupActivityScreen(
    navController: NavController,
    groupId: String,
    groupName: String = "Group",
    viewModel: ActivityViewModel = hiltViewModel()
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val state by viewModel.state.collectAsState()
    
    // Load activities when the screen is first displayed
    LaunchedEffect(groupId) {
        viewModel.loadActivities(groupId)
    }
    
    val filters = listOf("All", "Expenses", "Payments", "Members", "Settings")
    
    val filteredActivities = remember(selectedFilter, state.activities) {
        when (selectedFilter) {
            "All" -> state.activities
            "Expenses" -> state.activities.filter { 
                it.type in listOf(ActivityType.EXPENSE_ADDED, ActivityType.EXPENSE_EDITED, ActivityType.EXPENSE_DELETED) 
            }
            "Payments" -> state.activities.filter { 
                it.type == ActivityType.SETTLEMENT_MADE
            }
            "Members" -> state.activities.filter { 
                it.type in listOf(ActivityType.MEMBER_ADDED, ActivityType.MEMBER_REMOVED) 
            }
            "Settings" -> state.activities.filter { 
                it.type in listOf(ActivityType.GROUP_CREATED, ActivityType.GROUP_SETTINGS_UPDATED) 
            }
            else -> state.activities
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            "$groupName Activity",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            "Recent group activities",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
        ) {
            // Filter Chips
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                LazyRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FairrFilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = filter,
                            leadingIcon = when (filter) {
                                "All" -> Icons.AutoMirrored.Filled.List
                                "Expenses" -> Icons.Default.Receipt
                                "Payments" -> Icons.Default.Payment
                                "Members" -> Icons.Default.Group
                                "Settings" -> Icons.Default.Settings
                                else -> null
                            }
                        )
                    }
                }
            }
            
            // Activity List
            if (state.isLoading) {
                FairrLoadingCard(
                    message = "Loading activity...",
                    modifier = Modifier.padding(16.dp)
                )
            } else if (filteredActivities.isEmpty()) {
                FairrEmptyState(
                    title = "No Activity Yet",
                    message = "Activities will appear here as group members add expenses and make payments",
                    icon = Icons.Default.Timeline,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredActivities) { activity ->
                        ActivityCard(activity = activity)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ActivityCard(
    activity: GroupActivity,
    modifier: Modifier = Modifier
) {
    val (displayName, icon, color) = activity.type.getDisplayProperties()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Activity Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = displayName,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Activity Content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activity.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = activity.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Avatar
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(DarkGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activity.userInitials,
                                    fontSize = 8.sp,
                                    color = NeutralWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            Text(
                                text = activity.userName,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = PlaceholderText
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = formatRelativeTime(activity.timestamp),
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    // Amount (if applicable)
                    activity.amount?.let { amount ->
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = if (activity.isPositive) "+$${String.format("%.2f", amount)}" 
                                       else "-$${String.format("%.2f", amount)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (activity.isPositive) SuccessGreen else ErrorRed
                            )
                            
                            Text(
                                text = displayName,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}

private fun getSampleActivities(): List<GroupActivity> {
    val now = System.currentTimeMillis()
    return listOf(
        GroupActivity(
            id = "1",
            type = ActivityType.EXPENSE_ADDED,
            title = "Dinner at Mario's",
            description = "Added expense for group dinner",
            amount = 120.50,
            userName = "Alex Johnson",
            userInitials = "AJ",
            timestamp = now - 30 * 60 * 1000, // 30 minutes ago
            isPositive = false
        ),
        GroupActivity(
            id = "2",
            type = ActivityType.SETTLEMENT_MADE,
            title = "Payment to Sarah",
            description = "Settled dinner expenses",
            amount = 25.00,
            userName = "Mike Chen",
            userInitials = "MC",
            timestamp = now - 2 * 60 * 60 * 1000, // 2 hours ago
            isPositive = false
        ),
        GroupActivity(
            id = "3",
            type = ActivityType.MEMBER_ADDED,
            title = "New member joined",
            description = "Emma Davis joined the group",
            userName = "Emma Davis",
            userInitials = "ED",
            timestamp = now - 6 * 60 * 60 * 1000 // 6 hours ago
        ),
        GroupActivity(
            id = "4",
            type = ActivityType.EXPENSE_EDITED,
            title = "Updated grocery expense",
            description = "Changed split method to equal",
            amount = 85.30,
            userName = "Sarah Wilson",
            userInitials = "SW",
            timestamp = now - 24 * 60 * 60 * 1000, // 1 day ago
            isPositive = false
        ),
        GroupActivity(
            id = "5",
            type = ActivityType.SETTLEMENT_MADE,
            title = "Group settlement",
            description = "Optimized payments between all members",
            amount = 240.75,
            userName = "Alex Johnson",
            userInitials = "AJ",
            timestamp = now - 3 * 24 * 60 * 60 * 1000, // 3 days ago
            isPositive = true
        )
    )
}

@Preview(showBackground = true)
@Composable
fun GroupActivityScreenPreview() {
    FairrTheme {
        GroupActivityScreen(
            navController = rememberNavController(),
            groupId = "1",
            groupName = "Weekend Trip"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityCardPreview() {
    FairrTheme {
        ActivityCard(
            activity = GroupActivity(
                id = "1",
                type = ActivityType.EXPENSE_ADDED,
                title = "Dinner at Mario's",
                description = "Added expense for group dinner with friends",
                amount = 120.50,
                userName = "Alex Johnson",
                userInitials = "AJ",
                timestamp = System.currentTimeMillis() - 30 * 60 * 1000,
                isPositive = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
} 
