package com.example.fairr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
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
import com.example.fairr.ui.screens.groups.GroupListScreen
import com.example.fairr.ui.screens.profile.ProfileScreen
import com.example.fairr.ui.screens.friends.FriendsScreen
import com.example.fairr.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fairr.ui.screens.groups.GroupListViewModel
import com.example.fairr.ui.screens.groups.GroupListUiState
import com.example.fairr.data.model.Group
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fairr.util.CurrencyFormatter
import com.example.fairr.ui.components.ModernNavigationBar
import com.example.fairr.ui.screens.friends.FriendsViewModel

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
    navController: NavController,
    onNavigateToAddExpense: (String) -> Unit,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettlements: () -> Unit,
    onNavigateToFriends: () -> Unit,
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            ModernNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen(
                    navController = navController,
                    onNavigateToCreateGroup = onNavigateToCreateGroup,
                    onNavigateToJoinGroup = onNavigateToJoinGroup,
                    onNavigateToSearch = onNavigateToSearch,
                    onNavigateToNotifications = onNavigateToNotifications,
                    onNavigateToGroupDetail = onNavigateToGroupDetail,
                    onNavigateToSettlements = onNavigateToSettlements,
                    onNavigateToAddExpense = onNavigateToAddExpense
                )
                1 -> GroupListScreen(
                    navController = navController,
                    onNavigateToCreateGroup = onNavigateToCreateGroup,
                    onNavigateToGroupDetail = onNavigateToGroupDetail,
                    onNavigateToJoinGroup = onNavigateToJoinGroup
                )
                2 -> {
                    val friendsViewModel: FriendsViewModel = hiltViewModel()
                    FriendsScreen(
                        navController = navController,
                        viewModel = friendsViewModel,
                        onNavigateBack = { selectedTab = 0 }
                    )
                }
                3 -> NotificationsScreen(
                    navController = navController
                )
                4 -> SettingsScreen(
                    navController = navController,
                    onSignOut = onSignOut
                )
            }
        }
    }
}

@Composable
private fun GroupsTabContent(
    paddingValues: PaddingValues,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupListViewModel = hiltViewModel()
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

        // Group list
        when (val state = viewModel.uiState) {
            is GroupListUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is GroupListUiState.Success -> {
                if (state.groups.isEmpty()) {
                    EmptyGroupsMessage()
                } else {
                    GroupList(
                        groups = state.groups,
                        onGroupClick = onNavigateToGroupDetail,
                        viewModel = viewModel
                    )
                }
            }
            is GroupListUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
private fun GroupList(
    groups: List<Group>,
    onGroupClick: (String) -> Unit,
    viewModel: GroupListViewModel = hiltViewModel()
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(groups) { group ->
            GroupCard(
                group = group,
                balance = viewModel.getBalanceForGroup(group.id),
                onClick = { onGroupClick(group.id) }
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    balance: Double,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Group Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (group.avatar.isNotEmpty()) {
                    Text(
                        text = group.avatar,
                        fontSize = 24.sp
                    )
                } else {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Group Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${group.members.size} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = CurrencyFormatter.format(group.currency, balance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            balance > 0 -> SuccessGreen
                            balance < 0 -> ErrorRed
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyGroupsMessage() {
    FairrEmptyState(
        title = "No Groups Yet",
        message = "Create your first group to start tracking shared expenses with friends, family, or colleagues.",
        icon = Icons.Default.Group,
        modifier = Modifier.padding(horizontal = 24.dp)
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