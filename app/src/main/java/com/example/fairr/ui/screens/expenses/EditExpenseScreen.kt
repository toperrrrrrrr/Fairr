package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fairr.data.groups.GroupService
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.rememberDatePickerState
import com.example.fairr.data.model.RecurrenceRule
import com.example.fairr.data.model.RecurrenceFrequency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun EditExpenseScreen(
    navController: NavController,
    expenseId: String,
    onExpenseUpdated: () -> Unit = {},
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // UI fields bound to loaded expense
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    // Recurrence fields
    var isRecurring by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf("Weekly") }
    var interval by remember { mutableStateOf("1") }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    val categories = listOf(
        "Food & Dining", "Transportation", "Entertainment", 
        "Shopping", "Accommodation", "Groceries", "Bills", "Other"
    )
    
    val frequencies = listOf("Daily", "Weekly", "Monthly", "Yearly")

    // Load real group members from the expense's group
    var groupMembers by remember { mutableStateOf<List<Member>>(emptyList()) }
    
    // Load group members when expense loads
    LaunchedEffect(state.expense) {
        state.expense?.let { exp ->
            try {
                // Load group members from the group service via ViewModel
                viewModel.loadGroupMembers(exp.groupId)
            } catch (e: Exception) {
                // Fallback to expense split data if group loading fails
                groupMembers = exp.splitBetween.map { split ->
                    Member(
                        id = split.userId,
                        name = split.userName,
                        isSelected = true
                    )
                }
            }
        }
    }
    var memberSplits by remember { mutableStateOf(groupMembers) }

    // Populate UI fields when expense loads
    LaunchedEffect(state.expense) {
        state.expense?.let { exp ->
            description = exp.description
            amount = exp.amount.toString()
            selectedCategory = exp.category.name.replace("_", " ").capitalize()
            notes = exp.notes
            
            // Populate recurrence fields
            isRecurring = exp.recurrenceRule != null
            if (exp.recurrenceRule != null) {
                val rule = exp.recurrenceRule!!
                selectedFrequency = rule.frequency.displayName
                interval = rule.interval.toString()
                endDate = rule.endDate?.toDate()?.time
            }
            
            // Populate memberSplits from exp.splitBetween
            memberSplits = exp.splitBetween.map { split ->
                Member(
                    id = split.userId,
                    name = split.userName,
                    isSelected = true
                )
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Handle ViewModel events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is EditExpenseEvent.ExpenseUpdated -> {
                    onExpenseUpdated()
                    navController.popBackStack()
                }
                is EditExpenseEvent.ExpenseDeleted -> {
                    navController.popBackStack()
                }
                is EditExpenseEvent.ShowError -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
                null -> {}
            }
        }
    }

    // Load expense data when screen opens
    LaunchedEffect(expenseId) {
        viewModel.loadExpense(expenseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Edit Expense",
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
                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = NeutralWhite,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        val trimmedDescription = description.trim()
                        val amountValue = amount.trim().toDoubleOrNull()
                        if (trimmedDescription.length < 3) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Description must be at least 3 characters")
                            }
                            return@Button
                        }
                        if (amountValue == null || amountValue <= 0.0) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please enter a valid amount greater than 0")
                            }
                            return@Button
                        }
                        isLoading = true
                        // Update ViewModel's expense with new values
                        viewModel.onFieldChange { exp ->
                            exp.copy(
                                description = trimmedDescription,
                                amount = amountValue,
                                category = try {
                                    com.example.fairr.data.model.ExpenseCategory.valueOf(selectedCategory.uppercase().replace(" ", "_"))
                                } catch (e: Exception) {
                                    exp.category
                                },
                                notes = notes,
                                recurrenceRule = if (isRecurring) {
                                    buildRecurrenceRule(selectedFrequency, interval.toIntOrNull() ?: 1, endDate)
                                } else null
                                // TODO: Update splitBetween if memberSplits are edited
                            )
                        }
                        viewModel.saveChanges()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = NeutralWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && description.isNotBlank() && amount.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeutralWhite,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Update Expense",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Expense Details",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                    
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                    
                    // Category Selection
                    var expanded by remember { mutableStateOf(false) }
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
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
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkGreen,
                            focusedLabelColor = DarkGreen
                        )
                    )
                }
            }
            
            // Split Options Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NeutralWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Split Between",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        
                        TextButton(
                            onClick = {
                                // Toggle all members
                                val allSelected = memberSplits.all { it.isSelected }
                                memberSplits = memberSplits.map { 
                                    it.copy(isSelected = !allSelected) 
                                }
                            }
                        ) {
                            Text(
                                if (memberSplits.all { it.isSelected }) "Deselect All" else "Select All",
                                color = DarkGreen
                            )
                        }
                    }
                    
                    memberSplits.forEach { member ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    memberSplits = memberSplits.map {
                                        if (it.id == member.id) it.copy(isSelected = !it.isSelected)
                                        else it
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = member.isSelected,
                                onCheckedChange = { checked ->
                                    memberSplits = memberSplits.map {
                                        if (it.id == member.id) it.copy(isSelected = checked)
                                        else it
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = DarkGreen
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = member.name,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            if (member.isSelected && amount.isNotBlank()) {
                                val splitAmount = try {
                                    val totalAmount = amount.toDouble()
                                    val includedCount = memberSplits.count { it.isSelected }
                                    if (includedCount > 0) totalAmount / includedCount else 0.0
                                } catch (e: NumberFormatException) {
                                    0.0
                                }
                                
                                Text(
                                    text = "$${String.format("%.2f", splitAmount)}",
                                    fontSize = 14.sp,
                                    color = DarkGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Recurrence Options Card (only show if expense is recurring or user wants to make it recurring)
            if (isRecurring || state.expense?.recurrenceRule != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recurrence Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = DarkGreen,
                                    checkedTrackColor = DarkGreen.copy(alpha = 0.5f)
                                )
                            )
                        }
                        
                        if (isRecurring) {
                            // Frequency Selection
                            var frequencyExpanded by remember { mutableStateOf(false) }
                            
                            ExposedDropdownMenuBox(
                                expanded = frequencyExpanded,
                                onExpandedChange = { frequencyExpanded = !frequencyExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = selectedFrequency,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Frequency") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = frequencyExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = DarkGreen,
                                        focusedLabelColor = DarkGreen
                                    )
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = frequencyExpanded,
                                    onDismissRequest = { frequencyExpanded = false }
                                ) {
                                    frequencies.forEach { freq ->
                                        DropdownMenuItem(
                                            text = { Text(freq) },
                                            onClick = {
                                                selectedFrequency = freq
                                                frequencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            // Interval Input
                            OutlinedTextField(
                                value = interval,
                                onValueChange = { interval = it },
                                label = { Text("Interval") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DarkGreen,
                                    focusedLabelColor = DarkGreen
                                )
                            )
                            
                            // End Date Selection
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "End Date",
                                    fontSize = 16.sp,
                                    color = TextPrimary
                                )
                                
                                TextButton(
                                    onClick = { showEndDatePicker = true }
                                ) {
                                    Text(
                                        endDate?.let { 
                                            val date = java.util.Date(it)
                                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
                                        } ?: "Set End Date",
                                        color = DarkGreen
                                    )
                                }
                            }
                            
                            // Recurrence Summary
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkGreen.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {
                                    Text(
                                        text = "Recurrence Summary",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = buildRecurrenceSummary(selectedFrequency, interval.toIntOrNull() ?: 1, endDate),
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpense()
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

    // Date Picker Dialog
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { timestamp ->
                            endDate = timestamp
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun buildRecurrenceRule(frequency: String, interval: Int, endDate: Long?): RecurrenceRule {
    val freq = when (frequency) {
        "Daily" -> RecurrenceFrequency.DAILY
        "Weekly" -> RecurrenceFrequency.WEEKLY
        "Monthly" -> RecurrenceFrequency.MONTHLY
        "Yearly" -> RecurrenceFrequency.YEARLY
        else -> RecurrenceFrequency.WEEKLY
    }
    
    val endTimestamp = endDate?.let { com.google.firebase.Timestamp(java.util.Date(it)) }
    
    return RecurrenceRule(
        frequency = freq,
        interval = interval,
        endDate = endTimestamp
    )
}

private fun buildRecurrenceSummary(frequency: String, interval: Int, endDate: Long?): String {
    val intervalText = if (interval == 1) "" else " every $interval ${frequency.lowercase()}"
    val frequencyText = if (interval == 1) frequency else "${frequency}s"
    
    val summary = "Repeats $frequencyText$intervalText"
    
    return if (endDate != null) {
        val date = java.util.Date(endDate)
        val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(date)
        "$summary until $dateStr"
    } else {
        "$summary indefinitely"
    }
}

data class Member(
    val id: String,
    val name: String,
    val isSelected: Boolean
)

@Preview(showBackground = true)
@Composable
fun EditExpenseScreenPreview() {
    FairrTheme {
        EditExpenseScreen(
            navController = rememberNavController(),
            expenseId = "1"
        )
    }
} 
