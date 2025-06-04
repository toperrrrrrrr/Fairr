package com.example.fairr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.screens.home.HomeScreen
import com.example.fairr.ui.screens.analytics.AnalyticsScreen
import com.example.fairr.ui.screens.settings.SettingsScreen
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController = rememberNavController(),
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToJoinGroup: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToGroupDetail: (String) -> Unit = {},
    onNavigateToBudgets: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            EnhancedBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = when (index) {
                        0 -> 0 // Home
                        1 -> 1 // Groups  
                        2 -> {
                            // Analytics - navigate to separate screen, don't change tab
                            navController.navigate("analytics")
                            selectedTab // Keep current tab
                        }
                        3 -> 3 // Settings
                        else -> selectedTab
                    }
                },
                onFabClick = onNavigateToAddExpense,
                modifier = Modifier.fillMaxWidth()
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        when (selectedTab) {
            0 -> HomeTabContent(
                navController = navController,
                paddingValues = paddingValues,
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToJoinGroup = onNavigateToJoinGroup,
                onNavigateToSearch = onNavigateToSearch,
                onNavigateToNotifications = onNavigateToNotifications,
                onNavigateToGroupDetail = onNavigateToGroupDetail,
                onNavigateToAnalytics = {
                    navController.navigate("analytics")
                },
                onNavigateToBudgets = onNavigateToBudgets,
                onNavigateToSettings = {
                    selectedTab = 3 // Switch to settings tab instead of navigating
                }
            )
            1 -> GroupsTabContent(
                paddingValues = paddingValues,
                onNavigateToCreateGroup = onNavigateToCreateGroup,
                onNavigateToGroupDetail = onNavigateToGroupDetail
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
    navController: NavController,
    paddingValues: PaddingValues,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    HomeScreen(
        onNavigateToCreateGroup = onNavigateToCreateGroup,
        onNavigateToJoinGroup = onNavigateToJoinGroup,
        onNavigateToSearch = onNavigateToSearch,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToSettings = onNavigateToSettings, // This goes to settings, not profile
        onNavigateToGroupDetail = onNavigateToGroupDetail,
        onNavigateToAnalytics = onNavigateToAnalytics,
        onNavigateToBudgets = onNavigateToBudgets,
        modifier = Modifier.padding(paddingValues)
    )
}

@Composable
private fun GroupsTabContent(
    paddingValues: PaddingValues,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ModernSectionHeader(
            title = "Your Groups",
            subtitle = "Manage your expense groups"
        )
        
        // Quick create group action
        ModernCard(
            onClick = onNavigateToCreateGroup
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Group",
                    tint = Primary
                )
                Column {
                    Text(
                        text = "Create New Group",
                        fontSize = 16.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Start splitting expenses with friends",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
        
        // Sample groups list
        ModernEmptyState(
            title = "No Groups Yet",
            description = "Create or join a group to start sharing expenses with friends.",
            buttonText = "Create Group",
            onButtonClick = { /* Handle create group */ },
            icon = Icons.Default.Group
        )
    }
}

@Composable
private fun SettingsTabContent(
    paddingValues: PaddingValues,
    navController: NavController,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Settings Top App Bar
        TopAppBar(
            title = { 
                Text(
                    "Settings",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ) 
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundPrimary
            )
        )
        
        // Settings Content
        SettingsScreen(
            navController = navController,
            onSignOut = onSignOut
        )
    }
}

/**
 * Enhanced Navigation Wrapper for Analytics Screen
 */
@Composable
fun AnalyticsScreenWrapper(
    navController: NavController
) {
    AnalyticsScreen(navController = navController)
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