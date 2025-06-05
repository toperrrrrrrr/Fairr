package com.example.fairr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.screens.home.HomeScreen
import com.example.fairr.ui.screens.notifications.NotificationsScreen
import com.example.fairr.ui.screens.settings.SettingsScreen
import com.example.fairr.ui.theme.*

// Data class for group items
private data class GroupItem(
    val id: String,
    val name: String,
    val memberCount: Int,
    val balance: Double,
    val currency: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToAddExpense: () -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            ModernNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeTabContent(
                paddingValues = paddingValues,
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToJoinGroup = onNavigateToJoinGroup,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToGroupDetail = onNavigateToGroupDetail,
                onNavigateToBudgets = onNavigateToBudgets,
                onNavigateToSettings = onNavigateToSettings
            )
            1 -> GroupsTabContent(
                paddingValues = paddingValues,
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToGroupDetail = onNavigateToGroupDetail,
                onNavigateToJoinGroup = onNavigateToJoinGroup,
                modifier = Modifier
            )
            2 -> NotificationsScreen(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
            3 -> SettingsTabContent(
                paddingValues = paddingValues,
                navController = navController,
                onSignOut = onSignOut
            )
        }
    }
}

@Composable
private fun HomeTabContent(
    paddingValues: PaddingValues,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Box(modifier = Modifier.padding(paddingValues)) {
        HomeScreen(
            onNavigateToCreateGroup = onNavigateToCreateGroup,
            onNavigateToJoinGroup = onNavigateToJoinGroup,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToGroupDetail = onNavigateToGroupDetail,
            onNavigateToBudgets = onNavigateToBudgets
        )
    }
}

@Composable
private fun GroupsTabContent(
    paddingValues: PaddingValues,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Your Groups",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // Group actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Create group card
            Card(
                onClick = onNavigateToCreateGroup,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Group",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Create Group",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Start a new group",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Join group card
            Card(
                onClick = onNavigateToJoinGroup,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = "Join Group",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "Join Group",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Join existing group",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        
        // Sample groups list
        val sampleGroups = listOf(
            GroupItem("1", "Weekend Trip", 4, -125.75, "$"),
            GroupItem("2", "Apartment Rent", 3, 150.25, "$"),
            GroupItem("3", "Dinner Party", 6, 0.00, "$")
        )
        
        // Groups list
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleGroups) { group ->
                Card(
                    onClick = { onNavigateToGroupDetail(group.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = group.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${group.memberCount} members",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    paddingValues: PaddingValues,
    navController: NavController,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsScreen(
        navController = navController,
        onSignOut = onSignOut,
        modifier = modifier.padding(paddingValues)
    )
}

/**
 * Enhanced Navigation Wrapper for Settings Screen (Profile)
 */
@Composable
fun SettingsScreenWrapper(
    navController: NavController,
    onSignOut: () -> Unit = {}
) {
    SettingsScreen(
        navController = navController,
        onSignOut = onSignOut
    )
} 