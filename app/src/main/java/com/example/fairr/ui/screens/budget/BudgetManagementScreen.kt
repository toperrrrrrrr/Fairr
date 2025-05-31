package com.example.fairr.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.screens.categories.ExpenseCategory
import com.example.fairr.ui.screens.categories.getDefaultCategories
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetManagementScreen(
    navController: NavController,
    onSaveBudgets: (List<Budget>) -> Unit = {}
) {
    var budgets by remember { mutableStateOf(getSampleBudgets()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedBudget by remember { mutableStateOf<Budget?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Category, 1 = Monthly
    
    val categories = remember { getDefaultCategories() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Budget Management",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
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
                actions = {
                    TextButton(
                        onClick = { 
                            onSaveBudgets(budgets)
                            navController.popBackStack()
                        }
                    ) {
                        Text(
                            "Save",
                            color = DarkGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = DarkGreen,
                contentColor = PureWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = "Budget",
                        tint = DarkGreen,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Smart Budgeting",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Set budgets and track your spending habits",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Tab Selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .shadow(1.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = PureWhite,
                    contentColor = DarkGreen
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Category Budgets") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Monthly Overview") }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    CategoryBudgetContent(
                        budgets = budgets.filter { it.type == BudgetType.CATEGORY },
                        onEditBudget = { budget ->
                            selectedBudget = budget
                            showEditDialog = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                1 -> {
                    MonthlyOverviewContent(
                        budgets = budgets,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    
    // Add Budget Dialog
    if (showAddDialog) {
        AddEditBudgetDialog(
            budget = null,
            categories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { newBudget ->
                budgets = budgets + newBudget
                showAddDialog = false
            }
        )
    }
    
    // Edit Budget Dialog
    if (showEditDialog && selectedBudget != null) {
        AddEditBudgetDialog(
            budget = selectedBudget,
            categories = categories,
            onDismiss = { showEditDialog = false },
            onSave = { updatedBudget ->
                budgets = budgets.map { 
                    if (it.id == selectedBudget!!.id) updatedBudget else it 
                }
                showEditDialog = false
                selectedBudget = null
            }
        )
    }
}

@Composable
fun CategoryBudgetContent(
    budgets: List<Budget>,
    onEditBudget: (Budget) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(budgets) { budget ->
            BudgetCard(
                budget = budget,
                onEdit = { onEditBudget(budget) }
            )
        }
        
        if (budgets.isEmpty()) {
            item {
                EmptyBudgetState()
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}

@Composable
fun MonthlyOverviewContent(
    budgets: List<Budget>,
    modifier: Modifier = Modifier
) {
    val totalBudget = budgets.sumOf { it.amount }
    val totalSpent = budgets.sumOf { it.spent }
    val remainingBudget = totalBudget - totalSpent
    
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OverallBudgetCard(
                totalBudget = totalBudget,
                totalSpent = totalSpent,
                remainingBudget = remainingBudget
            )
        }
        
        item {
            Text(
                text = "Budget Breakdown",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        
        items(budgets.sortedByDescending { it.spent / it.amount }) { budget ->
            BudgetSummaryCard(budget = budget)
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetCard(
    budget: Budget,
    onEdit: () -> Unit
) {
    val progress = (budget.spent / budget.amount).coerceIn(0.0, 1.0)
    val isOverBudget = budget.spent > budget.amount
    
    Card(
        onClick = onEdit,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(budget.color.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            budget.icon,
                            contentDescription = budget.name,
                            tint = budget.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = budget.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${String.format("%.0f", budget.spent)} / $${String.format("%.0f", budget.amount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOverBudget) ErrorRed else TextPrimary
                    )
                    Text(
                        text = if (isOverBudget) 
                            "$${String.format("%.0f", budget.spent - budget.amount)} over" 
                        else 
                            "$${String.format("%.0f", budget.amount - budget.spent)} left",
                        fontSize = 12.sp,
                        color = if (isOverBudget) ErrorRed else SuccessGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            Column {
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        isOverBudget -> ErrorRed
                        progress > 0.8 -> WarningOrange
                        else -> SuccessGreen
                    },
                    trackColor = PlaceholderText.copy(alpha = 0.1f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}% used",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun OverallBudgetCard(
    totalBudget: Double,
    totalSpent: Double,
    remainingBudget: Double
) {
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).coerceIn(0.0, 1.0) else 0.0
    val isOverBudget = totalSpent > totalBudget
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "This Month",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BudgetStatItem(
                    value = "$${String.format("%.0f", totalBudget)}",
                    label = "Total Budget",
                    color = DarkGreen
                )
                BudgetStatItem(
                    value = "$${String.format("%.0f", totalSpent)}",
                    label = "Spent",
                    color = if (isOverBudget) ErrorRed else DarkBlue
                )
                BudgetStatItem(
                    value = if (isOverBudget) "-$${String.format("%.0f", kotlin.math.abs(remainingBudget))}" 
                           else "$${String.format("%.0f", remainingBudget)}",
                    label = if (isOverBudget) "Over Budget" else "Remaining",
                    color = if (isOverBudget) ErrorRed else SuccessGreen
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    isOverBudget -> ErrorRed
                    progress > 0.8 -> WarningOrange
                    else -> SuccessGreen
                },
                trackColor = PlaceholderText.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun BudgetSummaryCard(budget: Budget) {
    val progress = (budget.spent / budget.amount).coerceIn(0.0, 1.0)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(budget.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    budget.icon,
                    contentDescription = budget.name,
                    tint = budget.color,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = budget.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = when {
                        progress > 1.0 -> ErrorRed
                        progress > 0.8 -> WarningOrange
                        else -> SuccessGreen
                    },
                    trackColor = PlaceholderText.copy(alpha = 0.1f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    progress > 1.0 -> ErrorRed
                    progress > 0.8 -> WarningOrange
                    else -> TextPrimary
                }
            )
        }
    }
}

@Composable
fun BudgetStatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyBudgetState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountBalance,
            contentDescription = "No budgets",
            modifier = Modifier.size(64.dp),
            tint = PlaceholderText
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Budgets Set",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
        
        Text(
            text = "Create your first budget to start tracking your spending",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetDialog(
    budget: Budget?,
    categories: List<ExpenseCategory>,
    onDismiss: () -> Unit,
    onSave: (Budget) -> Unit
) {
    var name by remember { mutableStateOf(budget?.name ?: "") }
    var amount by remember { mutableStateOf(budget?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(budget?.categoryId ?: categories.first().id) }
    var budgetType by remember { mutableStateOf(budget?.type ?: BudgetType.CATEGORY) }
    
    val isEditing = budget != null
    val selectedCat = categories.find { it.id == selectedCategory } ?: categories.first()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (isEditing) "Edit Budget" else "Add Budget") 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Budget Type Selection
                Text(
                    text = "Budget Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                
                Row {
                    BudgetType.values().forEach { type ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = budgetType == type,
                                onClick = { budgetType = type }
                            )
                            Text(
                                text = type.displayName,
                                modifier = Modifier.padding(start = 4.dp, end = 16.dp)
                            )
                        }
                    }
                }
                
                // Category Selection (only for category budgets)
                if (budgetType == BudgetType.CATEGORY) {
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCat.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(selectedCat.color.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        selectedCat.icon,
                                        contentDescription = "Category",
                                        tint = selectedCat.color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DarkGreen,
                                focusedLabelColor = DarkGreen
                            )
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(category.color.copy(alpha = 0.1f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    category.icon,
                                                    contentDescription = category.name,
                                                    tint = category.color,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category.name)
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = category.id
                                        name = category.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Custom name for monthly budgets
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Budget Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                }
                
                // Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = it
                        }
                    },
                    label = { Text("Budget Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Text("$", color = PlaceholderText, fontSize = 16.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DarkGreen,
                        focusedLabelColor = DarkGreen
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && amount.isNotBlank()) {
                        val newBudget = Budget(
                            id = budget?.id ?: generateBudgetId(),
                            name = name.trim(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            spent = budget?.spent ?: 0.0,
                            type = budgetType,
                            categoryId = if (budgetType == BudgetType.CATEGORY) selectedCategory else null,
                            icon = selectedCat.icon,
                            color = selectedCat.color,
                            period = BudgetPeriod.MONTHLY
                        )
                        onSave(newBudget)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
                enabled = name.isNotBlank() && amount.isNotBlank()
            ) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Data classes and helper functions
data class Budget(
    val id: String,
    val name: String,
    val amount: Double,
    val spent: Double,
    val type: BudgetType,
    val categoryId: String? = null,
    val icon: ImageVector,
    val color: Color,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY
)

enum class BudgetType(val displayName: String) {
    CATEGORY("Category"),
    MONTHLY("Monthly")
}

enum class BudgetPeriod(val displayName: String) {
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
}

fun getSampleBudgets(): List<Budget> {
    return listOf(
        Budget("1", "Food & Dining", 500.0, 325.50, BudgetType.CATEGORY, "1", Icons.Default.Restaurant, DarkGreen),
        Budget("2", "Transportation", 200.0, 180.25, BudgetType.CATEGORY, "2", Icons.Default.DirectionsCar, DarkBlue),
        Budget("3", "Entertainment", 150.0, 95.00, BudgetType.CATEGORY, "3", Icons.Default.Movie, ErrorRed),
        Budget("4", "Shopping", 300.0, 320.75, BudgetType.CATEGORY, "4", Icons.Default.ShoppingBag, WarningOrange),
        Budget("5", "Monthly Total", 1200.0, 921.50, BudgetType.MONTHLY, null, Icons.Default.AccountBalance, DarkGreen)
    )
}

fun generateBudgetId(): String {
    return "budget_${System.currentTimeMillis()}"
}

@Preview(showBackground = true)
@Composable
fun BudgetManagementScreenPreview() {
    FairrTheme {
        BudgetManagementScreen(navController = rememberNavController())
    }
} 