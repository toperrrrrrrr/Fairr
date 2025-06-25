package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*
import com.example.fairr.navigation.Screen
import com.example.fairr.data.model.Group
import com.example.fairr.ui.model.GroupMember as UiGroupMember
import com.example.fairr.util.CurrencyFormatter
import com.example.fairr.data.model.Expense

// Data classes
data class GroupDetail(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val totalExpenses: Double,
    val yourBalance: Double,
    val currency: String,
    val members: List<Member>
)

data class Member(
    val id: String,
    val name: String,
    val balance: Double
)

data class ExpenseItem(
    val id: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val date: String
)

@Composable
fun GroupDetailScreen(
    groupId: String,
    navController: NavController,
    onNavigateToAddExpense: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    when (uiState) {
        is GroupDetailUiState.Loading -> {
            FairrLoadingCard(
                message = "Loading group details...",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
        is GroupDetailUiState.Error -> {
            FairrErrorState(
                message = uiState.message,
                onActionClick = { viewModel.refresh() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }
        is GroupDetailUiState.Success -> {
            val group = uiState.group
            Column(
                modifier = modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text(text = group.name) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.GroupSettings.createRoute(groupId)) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Group Settings")
                        }
                    }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Group Overview Section
                    item {
                        GroupOverview(
                            memberCount = uiState.members.size,
                            totalExpenses = uiState.totalExpenses,
                            currentUserBalance = uiState.currentUserBalance,
                            currency = group.currency,
                            groupId = groupId,
                            navController = navController
                        )
                    }

                    // Quick Actions Section
                    item {
                        QuickActionsSection(
                            groupId = groupId,
                            navController = navController,
                            onAddExpenseClick = onNavigateToAddExpense
                        )
                    }

                    if (uiState.expenses.isEmpty()) {
                        item {
                            FairrEmptyState(
                                title = "No expenses yet",
                                message = "Start tracking your shared costs by adding the first expense.",
                                actionText = "Add Expense",
                                onActionClick = onNavigateToAddExpense,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        // Expenses Section header
                        item {
                            Text(
                                text = "Recent Expenses",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(uiState.expenses) { expense ->
                            ExpenseCard(
                                expense = expense,
                                currency = group.currency,
                                viewModel = viewModel,
                                onClick = { navController.navigate(Screen.ExpenseDetail.createRoute(expense.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun ExpenseCard(
    expense: Expense,
    currency: String,
    viewModel: GroupDetailViewModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = viewModel.formatDate(expense.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = CurrencyFormatter.format(currency, expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MemberCard(
    member: UiGroupMember,
    currency: String,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.first().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = member.name + (if (member.isCurrentUser) " (You)" else ""),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            // For now, show 0.00 as balance since GroupMember doesn't track balance
            Text(
                text = CurrencyFormatter.format(currency, 0.00),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
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
            .clickable { onClick() }
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
                        color = Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GroupOverview(
    memberCount: Int,
    totalExpenses: Double,
    currentUserBalance: Double,
    currency: String,
    groupId: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Members",
                    value = memberCount.toString(),
                    icon = Icons.Default.Group,
                    modifier = Modifier.clickable { navController.navigate(Screen.GroupSettings.createRoute(groupId)) }
                )
                InfoItem(
                    label = "Total Expenses",
                    value = CurrencyFormatter.format(currency, totalExpenses),
                    icon = Icons.Default.Receipt
                )
                InfoItem(
                    label = "Your Balance",
                    value = CurrencyFormatter.format(currency, currentUserBalance),
                    icon = Icons.Default.AccountBalance
                )
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    groupId: String,
    navController: NavController,
    onAddExpenseClick: () -> Unit,
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
                title = "Add Expense",
                icon = Icons.Default.AddCircle,
                onClick = onAddExpenseClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Settle Up",
                icon = Icons.Default.SwapHoriz,
                onClick = { 
                    navController.navigate(Screen.Settlement.createRoute(groupId))
                },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Activity",
                icon = Icons.Default.History,
                onClick = { 
                    navController.navigate(Screen.GroupActivity.createRoute(groupId))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupDetailScreenPreview() {
    FairrTheme {
        GroupDetailScreen(
            groupId = "1",
            navController = rememberNavController(),
            onNavigateToAddExpense = {}
        )
    }
}