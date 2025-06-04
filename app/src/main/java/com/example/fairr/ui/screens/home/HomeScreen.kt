package com.example.fairr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToJoinGroup: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToGroupDetail: (String) -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val groups = remember {
        listOf(
            GroupItem("1", "Weekend Trip", 4, -125.75, "$"),
            GroupItem("2", "Apartment Rent", 3, 150.25, "$"),
            GroupItem("3", "Dinner Party", 6, 0.00, "$"),
            GroupItem("4", "Office Lunch", 8, -45.50, "$")
        )
    }
    
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Fairr",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                actions = {
                    // Search action
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeTabContent(
                groups = groups,
                onNavigateToGroupDetail = onNavigateToGroupDetail,
                onNavigateToBudgets = onNavigateToBudgets,
                modifier = Modifier.padding(padding)
            )
            1 -> GroupsTabContent(
                groups = groups,
                onNavigateToGroupDetail = onNavigateToGroupDetail,
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToJoinGroup = onNavigateToJoinGroup,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
fun HomeTabContent(
    groups: List<GroupItem>,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToBudgets: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Overview Card
        OverviewCard(
            totalBalance = 1234.56,
            currency = "$",
            onNavigateToBudgets = onNavigateToBudgets
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recent Groups
        Text(
            text = "Recent Groups",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Group List
        groups.forEach { group ->
            GroupCard(
                group = group,
                onClick = { onNavigateToGroupDetail(group.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun GroupsTabContent(
    groups: List<GroupItem>,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Groups List
        Text(
            text = "Your Groups",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Group Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateToCreateGroup,
                modifier = Modifier.weight(1f)
            ) {
                Text("Create Group")
            }
            
            OutlinedButton(
                onClick = onNavigateToJoinGroup,
                modifier = Modifier.weight(1f)
            ) {
                Text("Join Group")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Group List
        groups.forEach { group ->
            GroupCard(
                group = group,
                onClick = { onNavigateToGroupDetail(group.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SettingsTabContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSecondary)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModernCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "John Doe",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "john.doe@example.com",
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = { /* Navigate to edit profile */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary.copy(alpha = 0.1f),
                            contentColor = Primary
                        )
                    ) {
                        Text("Edit Profile")
                    }
                }
            }
        }

        // Settings List
        ModernCard {
            Column {
                SettingsItem(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    onClick = { /* Handle click */ }
                )
                SettingsItem(
                    title = "Privacy",
                    icon = Icons.Default.Lock,
                    onClick = { /* Handle click */ }
                )
                SettingsItem(
                    title = "Help & Support",
                    icon = Icons.Default.Help,
                    onClick = { /* Handle click */ }
                )
                SettingsItem(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = { /* Handle click */ }
                )
                SettingsItem(
                    title = "Sign Out",
                    icon = Icons.Default.ExitToApp,
                    onClick = { /* Handle sign out */ },
                    textColor = ErrorRed,
                    iconTint = ErrorRed
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    textColor: Color = TextPrimary,
    iconTint: Color = Primary
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun ModernGroupCard(
    group: GroupItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group Icon
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
                    imageVector = Icons.Default.Group,
                    contentDescription = group.name,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Group Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${group.memberCount} members",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Balance
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val balanceColor = if (group.balance >= 0) SuccessGreen else ErrorRed
                val balanceText = if (group.balance >= 0) 
                    "+${group.currency}${String.format("%.2f", group.balance)}" 
                else 
                    "${group.currency}${String.format("%.2f", group.balance)}"
                
                Text(
                    text = balanceText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = balanceColor
                )
                
                Text(
                    text = if (group.balance >= 0) "You're owed" else "You owe",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ModernStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    changeValue: String,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = changeValue,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ModernButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Primary,
            contentColor = TextOnDark,
            disabledContainerColor = Primary.copy(alpha = 0.5f),
            disabledContentColor = TextOnDark.copy(alpha = 0.5f)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}

// Data class for group items
data class GroupItem(
    val id: String,
    val name: String,
    val memberCount: Int,
    val balance: Double,
    val currency: String = "$"
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FairrTheme {
        HomeScreen()
    }
}

