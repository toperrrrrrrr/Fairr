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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.Expense
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.text.NumberFormat
import java.util.*
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Box
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import com.example.fairr.ui.components.*
import com.example.fairr.util.CurrencyFormatter
import androidx.compose.material.ExperimentalMaterialApi
import com.example.fairr.navigation.Screen
import com.example.fairr.data.model.RecurrenceRule
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToCreateGroup: () -> Unit,
    onNavigateToJoinGroup: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToSettlements: () -> Unit,
    onNavigateToAddExpense: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val refreshing = state.isLoading
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { viewModel.refresh() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Fairr",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
                .padding(paddingValues)
                .semantics { 
                    contentDescription = "Home screen showing your groups, recent expenses, and financial overview"
                }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview Cards
                item {
                    OverviewSection(
                        totalBalance = state.totalBalance,
                        totalExpenses = state.totalExpenses,
                        activeGroups = state.activeGroups,
                        viewModel = viewModel,
                        modifier = Modifier.semantics {
                            contentDescription = "Financial overview: Balance ${CurrencyFormatter.format("USD", state.totalBalance)}, ${state.totalExpenses} total expenses, ${state.activeGroups} active groups"
                        }
                    )
                }

                // Quick Actions
                item {
                    QuickActionsSection(
                        onCreateGroupClick = onNavigateToCreateGroup,
                        onJoinGroupClick = onNavigateToJoinGroup,
                        onSettleUpClick = onNavigateToSettlements
                    )
                }

                // Groups
                item {
                    Text(
                        text = "Your Groups",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (state.groups.isEmpty()) {
                    item {
                        FairrEmptyState(
                            title = "No Groups Yet",
                            message = "Create your first group to start tracking shared expenses with friends and family.",
                            actionText = "Create Group",
                            onActionClick = onNavigateToCreateGroup,
                            icon = Icons.Default.Group,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    items(state.groups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onNavigateToGroupDetail(group.id) },
                            onAddExpenseClick = { onNavigateToAddExpense(group.id) }
                        )
                    }
                }

                // Recent Expenses
                if (state.recentExpenses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Expenses",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    items(state.recentExpenses) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onClick = { navController.navigate(Screen.ExpenseDetail.createRoute(expense.id)) },
                            viewModel = viewModel
                        )
                    }
                } else if (state.groups.isNotEmpty()) {
                    item {
                        FairrEmptyState(
                            title = "No Recent Expenses",
                            message = "Add your first expense to start tracking shared costs.",
                            actionText = "Add Expense",
                            onActionClick = { 
                                if (state.groups.isNotEmpty()) {
                                    onNavigateToAddExpense(state.groups.first().id)
                                }
                            },
                            icon = Icons.Default.Receipt,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense,
    onClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIcon(
                    category = expense.category,
                    size = 32.dp
                )
                
                Column {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        // Show recurring indicator
                        if (expense.recurrenceRule != null) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Recurring expense",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Paid by ${expense.paidByName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        // Show recurrence info
                        expense.recurrenceRule?.let { rule ->
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = getRecurrenceDisplayText(rule),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Text(
                text = viewModel.formatCurrency(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun getRecurrenceDisplayText(rule: RecurrenceRule): String {
    return rule.frequency.displayName
}

@Composable
private fun GroupCard(
    group: Group,
    onClick: () -> Unit,
    onAddExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (group.avatar.isNotEmpty()) {
                        Text(
                            text = group.avatar,
                            style = MaterialTheme.typography.headlineMedium,
                            fontSize = 24.sp
                        )
                    } else {
                        Text(
                            text = group.name.first().toString(),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${group.members.size} members",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(
    totalBalance: Double,
    totalExpenses: Double,
    activeGroups: Int,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InfoCard(
                title = "Total Balance",
                value = viewModel.formatCurrency(totalBalance),
                icon = Icons.Default.AccountBalance,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "Total Expenses",
                value = viewModel.formatCurrency(totalExpenses),
                icon = Icons.Default.Receipt,
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                title = "Active Groups",
                value = activeGroups.toString(),
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    onCreateGroupClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onSettleUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(
                title = "Create Group",
                icon = Icons.Default.Add,
                onClick = onCreateGroupClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Join Group",
                icon = Icons.Default.GroupAdd,
                onClick = onJoinGroupClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Settle Up",
                icon = Icons.Default.Payment,
                onClick = onSettleUpClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}




