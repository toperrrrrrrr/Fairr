package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.RecurrenceRule
import com.example.fairr.data.model.RecurrenceFrequency
import com.example.fairr.data.analytics.RecurringExpenseAnalytics
import com.example.fairr.ui.components.CategoryIcon
import com.example.fairr.ui.components.FairrEmptyState
import com.example.fairr.util.CurrencyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.fairr.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseManagementScreen(
    groupId: String,
    navController: NavController,
    viewModel: RecurringExpenseManagementViewModel = hiltViewModel()
) {
    // Add validation at screen entry
    if (groupId.isBlank()) {
        LaunchedEffect(Unit) {
            android.util.Log.e("RecurringExpenseManagement", "Invalid group ID received: '$groupId'")
            navController.popBackStack()
        }
        return
    }
    
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Load recurring expenses when screen opens
    LaunchedEffect(groupId) {
        viewModel.loadRecurringExpenses(groupId)
    }
    
    // Handle events
    LaunchedEffect(true) {
        viewModel.events.collect { event ->
            when (event) {
                is RecurringExpenseManagementEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                is RecurringExpenseManagementEvent.InstancesGenerated -> {
                    scope.launch {
                        snackbarHostState.showSnackbar("Generated ${event.count} instances")
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Recurring Expenses",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IconTint
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.RecurringExpenseAnalytics.createRoute(groupId)) }
                    ) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = IconTint
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (state.recurringExpenses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = TextSecondary
                        )
                        Text(
                            text = "No Recurring Expenses",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Create recurring expenses to see them here",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recurringExpenses) { expense ->
                        RecurringExpenseCard(
                            expense = expense,
                            onGenerateInstances = { 
                                viewModel.generateInstances(expense)
                            },
                            onEditExpense = {
                                navController.navigate("edit_expense/${expense.id}")
                            },
                            onDeleteExpense = {
                                viewModel.deleteRecurringExpense(expense)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecurringExpenseCard(
    expense: Expense,
    onGenerateInstances: () -> Unit,
    onEditExpense: () -> Unit,
    onDeleteExpense: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with category icon and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryIcon(
                        category = expense.category,
                        size = 40.dp
                    )
                    
                    Column {
                        Text(
                            text = expense.description,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Repeat,
                                contentDescription = "Recurring",
                                tint = Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = getRecurrenceDisplayText(expense.recurrenceRule),
                                style = MaterialTheme.typography.bodySmall,
                                color = Primary
                            )
                        }
                    }
                }
                
                Text(
                    text = CurrencyFormatter.format(expense.currency, expense.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            
            // Recurrence details
            expense.recurrenceRule?.let { rule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Recurrence Details",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = buildRecurrenceSummary(rule),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )
                        if (rule.endDate != null) {
                            Text(
                                text = "Ends: ${SimpleDateFormat("MMM dd, yyyy").format(rule.endDate.toDate())}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onGenerateInstances,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Primary
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate Instances")
                }
                
                OutlinedButton(
                    onClick = onEditExpense,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Primary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                IconButton(
                    onClick = { showDeleteDialog = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = ErrorRed
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recurring Expense") },
            text = { Text("Are you sure you want to delete this recurring expense? This will also delete all future instances.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun getRecurrenceDisplayText(rule: RecurrenceRule?): String {
    return rule?.frequency?.displayName ?: "Unknown"
}

@Composable
private fun buildRecurrenceSummary(rule: RecurrenceRule): String {
    val intervalText = if (rule.interval == 1) "" else " every ${rule.interval} ${rule.frequency.displayName.lowercase()}"
    val frequencyText = if (rule.interval == 1) rule.frequency.displayName else "${rule.frequency.displayName}s"
    
    val summary = "Repeats $frequencyText$intervalText"
    
    return if (rule.endDate != null) {
        val dateStr = SimpleDateFormat("MMM dd, yyyy").format(rule.endDate.toDate())
        "$summary until $dateStr"
    } else {
        "$summary indefinitely"
    }
}

@Preview(showBackground = true)
@Composable
fun RecurringExpenseManagementScreenPreview() {
    FairrTheme {
        RecurringExpenseManagementScreen(
            groupId = "sample_group_preview",
            navController = rememberNavController()
        )
    }
} 