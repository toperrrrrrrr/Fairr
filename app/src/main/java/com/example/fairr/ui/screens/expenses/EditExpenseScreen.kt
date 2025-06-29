package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.fairr.data.model.*
import com.example.fairr.ui.theme.FairrTheme
import com.example.fairr.ui.theme.Primary
import com.example.fairr.ui.theme.Secondary
import com.example.fairr.ui.theme.Surface
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    navController: NavController,
    expenseId: String,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state = viewModel.state
    
    LaunchedEffect(expenseId) {
        viewModel.loadExpense(expenseId)
    }
    
    LaunchedEffect(state.expense?.groupId) {
        state.expense?.groupId?.let { groupId ->
            viewModel.loadGroupMembers(groupId)
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditExpenseEvent.ShowError -> {
                    // Show error message
                }
                is EditExpenseEvent.ExpenseUpdated -> {
                    navController.popBackStack()
                }
                is EditExpenseEvent.ExpenseDeleted -> {
                    navController.popBackStack()
                }
            }
        }
    }

    FairrTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit Expense") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { 
                                state.expense?.let { expense ->
                                    viewModel.deleteExpense(expense.id)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                )
            }
        ) { paddingValues ->
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                state.expense?.let { expense ->
                    EditExpenseContent(
                        expense = expense,
                        groupMembers = state.groupMembers,
                        onUpdateExpense = { description: String, amount: Double, date: Date, paidBy: String, splitType: String, category: ExpenseCategory, notes: String, isRecurring: Boolean, recurrenceRule: RecurrenceRule?, splits: List<ExpenseSplit> ->
                            viewModel.updateExpense(
                                expenseId = expense.id,
                                description = description,
                                amount = amount,
                                date = date,
                                paidBy = paidBy,
                                splitType = splitType,
                                category = category,
                                notes = notes,
                                isRecurring = isRecurring,
                                recurrenceRule = recurrenceRule,
                                splits = splits
                            )
                        },
                        currencySymbol = viewModel.getCurrencySymbol(),
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditExpenseContent(
    expense: Expense,
    groupMembers: List<GroupMember>,
    onUpdateExpense: (
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String,
        category: ExpenseCategory,
        notes: String,
        isRecurring: Boolean,
        recurrenceRule: RecurrenceRule?,
        splits: List<ExpenseSplit>
    ) -> Unit,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var description by remember { mutableStateOf(expense.description) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var selectedDate by remember { mutableStateOf(expense.date.toDate()) }
    var selectedPaidBy by remember { mutableStateOf(expense.paidBy) }
    var selectedSplitType by remember { mutableStateOf(expense.splitType) }
    var selectedCategory by remember { mutableStateOf(expense.category) }
    var notes by remember { mutableStateOf(expense.notes) }
    var isRecurring by remember { mutableStateOf(expense.isRecurring) }
    var recurrenceRule by remember { mutableStateOf(expense.recurrenceRule) }
    var splits by remember { mutableStateOf(expense.splitBetween) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSplitTypePicker by remember { mutableStateOf(false) }
    var showPaidByPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description
        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Amount
        item {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                prefix = { Text(currencySymbol) }
            )
        }

        // Date
        item {
            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                onValueChange = { },
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            )
        }

        // Paid By
        item {
            OutlinedTextField(
                value = selectedPaidBy,
                onValueChange = { },
                label = { Text("Paid By") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPaidByPicker = true },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                }
            )
        }

        // Split Type
        item {
            OutlinedTextField(
                value = selectedSplitType,
                onValueChange = { },
                label = { Text("Split Type") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSplitTypePicker = true },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                }
            )
        }

        // Category
        item {
            OutlinedTextField(
                value = selectedCategory.name,
                onValueChange = { },
                label = { Text("Category") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryPicker = true },
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select")
                }
            )
        }

        // Notes
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }

        // Recurring Expense Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recurring Expense",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = isRecurring,
                    onCheckedChange = { isRecurring = it }
                )
            }
        }

        // Recurrence Rule (if recurring)
        if (isRecurring) {
            item {
                RecurrenceRuleSelector(
                    currentRule = recurrenceRule,
                    onRuleChanged = { recurrenceRule = it }
                )
            }
        }

        // Split Details
        item {
            Text(
                text = "Split Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        items(splits) { split ->
            SplitItem(
                split = split,
                groupMembers = groupMembers,
                onSplitChanged = { updatedSplit ->
                    splits = splits.map { if (it.userId == updatedSplit.userId) updatedSplit else it }
                }
            )
        }

        // Update Button
        item {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    onUpdateExpense(
                        description,
                        amountValue,
                        selectedDate,
                        selectedPaidBy,
                        selectedSplitType,
                        selectedCategory,
                        notes,
                        isRecurring,
                        recurrenceRule,
                        splits
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = description.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Update Expense")
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.time)
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { 
                        datePickerState.selectedDateMillis?.let { selectedDate = Date(it) }
                        showDatePicker = false 
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Category Picker Dialog
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("Select Category") },
            text = {
                LazyColumn {
                    items(ExpenseCategory.values()) { category ->
                        Text(
                            text = category.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategory = category
                                    showCategoryPicker = false
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Split Type Picker Dialog
    if (showSplitTypePicker) {
        AlertDialog(
            onDismissRequest = { showSplitTypePicker = false },
            title = { Text("Select Split Type") },
            text = {
                LazyColumn {
                    items(listOf("Equal", "Percentage", "Fixed Amount")) { splitType ->
                        Text(
                            text = splitType,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSplitType = splitType
                                    showSplitTypePicker = false
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSplitTypePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Paid By Picker Dialog
    if (showPaidByPicker) {
        AlertDialog(
            onDismissRequest = { showPaidByPicker = false },
            title = { Text("Select Who Paid") },
            text = {
                LazyColumn {
                    items(groupMembers) { member ->
                        Text(
                            text = member.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPaidBy = member.userId
                                    showPaidByPicker = false
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaidByPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SplitItem(
    split: ExpenseSplit,
    groupMembers: List<GroupMember>,
    onSplitChanged: (ExpenseSplit) -> Unit
) {
    val member = groupMembers.find { it.userId == split.userId }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Surface)
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
                    text = member?.name ?: "Unknown",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Amount: ${split.share}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Add more split editing controls here if needed
        }
    }
}

@Composable
private fun RecurrenceRuleSelector(
    currentRule: RecurrenceRule?,
    onRuleChanged: (RecurrenceRule?) -> Unit
) {
    var frequency by remember { mutableStateOf(currentRule?.frequency ?: RecurrenceFrequency.MONTHLY) }
    var interval by remember { mutableStateOf(currentRule?.interval?.toString() ?: "1") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Recurrence Settings",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Frequency selector
        Text("Frequency:", fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RecurrenceFrequency.values().forEach { freq ->
                FilterChip(
                    selected = frequency == freq,
                    onClick = { 
                        frequency = freq
                        onRuleChanged(RecurrenceRule(frequency = freq, interval = interval.toIntOrNull() ?: 1))
                    },
                    label = { Text(freq.name) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Interval selector
        Text("Interval:", fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = interval,
            onValueChange = { 
                interval = it
                onRuleChanged(RecurrenceRule(frequency = frequency, interval = it.toIntOrNull() ?: 1))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
} 
