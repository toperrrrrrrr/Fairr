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
import com.example.fairr.data.model.RecurrenceRule

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

                        items(
                            items = uiState.expenses,
                            key = { expense -> expense.id }
                        ) { expense ->
                            ExpenseCard(
                                expense = expense,
                                currency = group.currency,
                                viewModel = viewModel,
                                onClick = { navController.navigate(Screen.ExpenseDetail.createRoute(expense.id)) }
                            )
                        }
                    }
                    
                    // Activity Feed Section
                    if (uiState.activities.isNotEmpty()) {
                        item {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        items(
                            items = uiState.activities.take(10),
                            key = { activity -> "${activity.id}_${activity.timestamp}" }
                        ) { activity ->
                            ActivityCard(
                                activity = activity,
                                viewModel = viewModel,
                                onClick = {
                                    activity.expenseId?.let { expenseId ->
                                        navController.navigate(Screen.ExpenseDetail.createRoute(expenseId))
                                    }
                                }
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatDate(expense.date),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Show recurrence info
                        expense.recurrenceRule?.let { rule ->
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                text = CurrencyFormatter.format(currency, expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun getRecurrenceDisplayText(rule: RecurrenceRule): String {
    return rule.frequency.displayName
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Add Expense",
                icon = Icons.Default.AddCircle,
                onClick = onAddExpenseClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                title = "Recurring",
                icon = Icons.Default.Repeat,
                onClick = { 
                    try {
                        if (groupId.isNotBlank()) {
                            navController.navigate(Screen.RecurringExpenseManagement.createRoute(groupId))
                        } else {
                            android.util.Log.e("GroupDetailScreen", "Group ID is blank, cannot navigate to recurring expenses")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GroupDetailScreen", "Failed to navigate to recurring expenses", e)
                    }
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
            QuickActionCard(
                title = "Settle Up",
                icon = Icons.Default.SwapHoriz,
                onClick = { 
                    navController.navigate(Screen.Settlement.createRoute(groupId))
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActivityCard(
    activity: GroupActivity,
    viewModel: GroupDetailViewModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = activity.expenseId != null) { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = when (activity.type) {
                            ActivityType.EXPENSE_ADDED -> Color(0xFF4CAF50)
                            ActivityType.MEMBER_JOINED -> Color(0xFF2196F3)
                            ActivityType.MEMBER_LEFT -> Color(0xFFFF5722)
                            ActivityType.EXPENSE_SETTLED -> Color(0xFF9C27B0)
                            ActivityType.GROUP_CREATED -> Color(0xFF607D8B)
                        }.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (activity.type) {
                        ActivityType.EXPENSE_ADDED -> Icons.Default.Receipt
                        ActivityType.MEMBER_JOINED -> Icons.Default.PersonAdd
                        ActivityType.MEMBER_LEFT -> Icons.Default.PersonRemove
                        ActivityType.EXPENSE_SETTLED -> Icons.Default.CheckCircle
                        ActivityType.GROUP_CREATED -> Icons.Default.Group
                    },
                    contentDescription = activity.title,
                    tint = when (activity.type) {
                        ActivityType.EXPENSE_ADDED -> Color(0xFF4CAF50)
                        ActivityType.MEMBER_JOINED -> Color(0xFF2196F3)
                        ActivityType.MEMBER_LEFT -> Color(0xFFFF5722)
                        ActivityType.EXPENSE_SETTLED -> Color(0xFF9C27B0)
                        ActivityType.GROUP_CREATED -> Color(0xFF607D8B)
                    },
                    modifier = Modifier.size(16.dp)
                )
            }
            
            // Activity content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = viewModel.formatActivityDate(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            // Amount (if applicable)
            activity.amount?.let { amount ->
                Text(
                    text = CurrencyFormatter.format("PHP", amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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