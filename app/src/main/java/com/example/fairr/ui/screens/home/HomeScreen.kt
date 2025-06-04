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
    onNavigateToAnalytics: () -> Unit = {},
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
                        text = when (selectedTab) {
                            0 -> "Fairr"
                            1 -> "Groups"
                            2 -> "Settings"
                            else -> "Fairr"
                        },
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                actions = {
                    // Search action
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = IconTint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreateGroup() },
                containerColor = Primary,
                contentColor = TextOnDark,
                shape = CircleShape,
                modifier = Modifier.offset(y = (-40).dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp,
                modifier = Modifier.height(80.dp)
            ) {
                // Home Tab
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        ) 
                    },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
                
                // Notifications Tab
                NavigationBarItem(
                    icon = { 
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        ) 
                    },
                    label = { Text("Notifications") },
                    selected = false,
                    onClick = onNavigateToNotifications,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
                
                // Spacer for FAB
                NavigationBarItem(
                    icon = { Box {} },
                    label = { Text("") },
                    selected = false,
                    onClick = { },
                    enabled = false,
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = Color.Transparent,
                        unselectedTextColor = Color.Transparent
                    )
                )
                
                // Groups Tab
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 1) Icons.Filled.Group else Icons.Outlined.Group,
                            contentDescription = "Groups"
                        ) 
                    },
                    label = { Text("Groups") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
                
                // Settings Tab
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        ) 
                    },
                    label = { Text("Settings") },
                    selected = selectedTab == 2,
                    onClick = { 
                        selectedTab = 2
                        onNavigateToSettings()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Primary,
                        selectedTextColor = Primary,
                        unselectedIconColor = IconTint,
                        unselectedTextColor = IconTint
                    )
                )
            }
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
            2 -> SettingsTabContent(
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundSecondary),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header
        item {
            ModernHeader(
                title = "Welcome back!",
                subtitle = "Manage your shared expenses"
            )
        }
        
        // Quick Stats
        item {
            Text(
                text = "Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernStatsCard(
                    title = "Total Balance",
                    value = "$${String.format("%.2f", groups.sumOf { kotlin.math.abs(it.balance) })}",
                    icon = Icons.Default.AccountBalance,
                    changeValue = "Active",
                    modifier = Modifier.weight(1f)
                )
                
                ModernStatsCard(
                    title = "Groups",
                    value = "${groups.size}",
                    icon = Icons.Default.Group,
                    changeValue = "Joined",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Recent Groups Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Groups",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                TextButton(
                    onClick = { /* Navigate to all groups */ }
                ) {
                    Text(
                        text = "View All",
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Groups List
        items(groups.take(3)) { group ->
            ModernGroupCard(
                group = group,
                onClick = { onNavigateToGroupDetail(group.id) }
            )
        }
        
        // Bottom spacing for FAB
        item {
            Spacer(modifier = Modifier.height(80.dp))
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
            .background(BackgroundSecondary)
            .padding(16.dp)
    ) {
        // Group Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ModernButton(
                text = "Create Group",
                onClick = onNavigateToCreateGroup,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Add
            )
            ModernButton(
                text = "Join Group",
                onClick = onNavigateToJoinGroup,
                modifier = Modifier.weight(1f),
                icon = Icons.Default.PersonAdd
            )
        }

        // Groups List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups) { group ->
                ModernGroupCard(
                    group = group,
                    onClick = { onNavigateToGroupDetail(group.id) }
                )
            }
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

