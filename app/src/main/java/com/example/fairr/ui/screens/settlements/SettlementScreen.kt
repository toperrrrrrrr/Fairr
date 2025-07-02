package com.example.fairr.ui.screens.settlements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.example.fairr.ui.theme.*
import com.example.fairr.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_PARAMETER")
fun SettlementScreen(
    navController: NavController,
    groupId: String,
    onSettlementComplete: () -> Unit = {},
    viewModel: SettlementViewModel = hiltViewModel()
) {
    var selectedPaymentMethod by remember { mutableStateOf("Cash") }
    var customAmount by remember { mutableStateOf("") }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedSettlement by remember { mutableStateOf<UserDebt?>(null) }
    var showManualSettlementDialog by remember { mutableStateOf(false) }
    val state = viewModel.state
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val paymentMethods = listOf("Cash", "Venmo", "PayPal", "Bank Transfer", "Other")

    // Load settlements when screen opens
    LaunchedEffect(groupId) {
        viewModel.loadSettlements(groupId)
    }

    // Handle events
    LaunchedEffect(true) {
        viewModel.events.collect { event ->
            when (event) {
                is SettlementEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                SettlementEvent.SettlementRecorded -> {
                    snackbarHostState.showSnackbar("Settlement recorded successfully")
                    onSettlementComplete()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Settle Up",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NeutralWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showManualSettlementDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Record Manual Settlement",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            FairrLoadingCard(
                message = "Loading settlements...",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SettlementSummaryCard(
                        totalOwed = viewModel.getTotalAmountOwed(),
                        totalOwedToYou = viewModel.getTotalAmountOwedToYou(),
                        netBalance = viewModel.getNetBalance()
                    )
                }
                
                // Settlement Suggestions Section
                if (state.currentUserDebts.isNotEmpty()) {
                    item {
                        SettlementSuggestionsCard(
                            debts = state.currentUserDebts,
                            onSettleUp = { debt ->
                                selectedSettlement = debt
                                customAmount = String.format("%.2f", debt.amount)
                                showPaymentDialog = true
                            }
                        )
                    }
                }
                
                // Individual Debts Section
                if (state.currentUserDebts.isNotEmpty()) {
                    item {
                        Text(
                            text = "Individual Balances",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
                
                items(state.currentUserDebts) { debt ->
                    SettlementCard(
                        debt = debt,
                        onSettleUp = {
                            selectedSettlement = debt
                            customAmount = String.format("%.2f", debt.amount)
                            showPaymentDialog = true
                        }
                    )
                }
                
                if (state.currentUserDebts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(1.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "All settled",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "All Settled Up!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "No outstanding balances in this group",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Snackbar host
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )

    // Payment Dialog
    selectedSettlement?.let { settlement ->
        if (showPaymentDialog) {
            AlertDialog(
                onDismissRequest = { showPaymentDialog = false },
                title = {
                    Text("Record Payment")
                },
                text = {
                    Column {
                        Text(
                            text = if (settlement.type == DebtType.YOU_OWE) {
                                "Record payment to ${settlement.personName}"
                            } else {
                                "Record payment from ${settlement.personName}"
                            },
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("$${String.format("%.2f", settlement.amount)}") }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text("Payment Method", fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        paymentMethods.forEach { method ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedPaymentMethod == method,
                                    onClick = { selectedPaymentMethod = method }
                                )
                                Text(
                                    text = method,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amountValue = customAmount.toDoubleOrNull() ?: settlement.amount
                            viewModel.recordSettlement(
                                groupId = groupId,
                                debtInfo = settlement,
                                amount = amountValue,
                                paymentMethod = selectedPaymentMethod
                            )
                            showPaymentDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DarkGreen
                        )
                    ) {
                        Text("Record Payment")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showPaymentDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    // Manual Settlement Dialog
    if (showManualSettlementDialog) {
        ManualSettlementDialog(
            groupId = groupId,
            onDismiss = { showManualSettlementDialog = false },
            onSettlementRecorded = { 
                showManualSettlementDialog = false
                viewModel.loadSettlements(groupId)
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun SettlementSummaryCard(
    totalOwed: Double,
    totalOwedToYou: Double,
    netBalance: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.AccountBalance,
                contentDescription = "Settlement",
                tint = if (netBalance >= 0) DarkGreen else ErrorRed,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (netBalance >= 0) "You're Owed Money" else "You Owe Money",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = "Net Balance: ${if (netBalance >= 0) "+" else ""}$${String.format("%.2f", netBalance)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (netBalance >= 0) DarkGreen else ErrorRed,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$${String.format("%.2f", totalOwed)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalOwed > 0) ErrorRed else TextSecondary
                    )
                    Text(
                        text = "You Owe",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$${String.format("%.2f", totalOwedToYou)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (totalOwedToYou > 0) DarkGreen else TextSecondary
                    )
                    Text(
                        text = "Owed to You",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun SettlementCard(
    debt: UserDebt,
    onSettleUp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (debt.type == DebtType.YOU_OWE) ErrorRed.copy(alpha = 0.1f) 
                        else SuccessGreen.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = debt.personName.split(" ").map { it.first() }.joinToString(""),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (debt.type == DebtType.YOU_OWE) ErrorRed else SuccessGreen
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.personName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = if (debt.type == DebtType.YOU_OWE) {
                        "You owe $${String.format("%.2f", debt.amount)}"
                    } else {
                        "Owes you $${String.format("%.2f", debt.amount)}"
                    },
                    fontSize = 12.sp,
                    color = if (debt.type == DebtType.YOU_OWE) ErrorRed else SuccessGreen
                )
            }
            
            // Settle Button
            Button(
                onClick = onSettleUp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (debt.type == DebtType.YOU_OWE) ErrorRed else DarkGreen,
                    contentColor = NeutralWhite
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "Settle",
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun SettlementSuggestionsCard(
    debts: List<UserDebt>,
    onSettleUp: (UserDebt) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Settlement Suggestions",
                    tint = InfoBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Smart Settlement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Minimize transactions with optimized payments",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Show top 3 priority settlements
            val priorityDebts = debts.sortedByDescending { it.amount }.take(3)
            
            priorityDebts.forEachIndexed { index, debt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority indicator
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = when (index) {
                                    0 -> WarningOrange
                                    1 -> InfoBlue
                                    else -> SuccessGreen
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = NeutralWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (debt.type == DebtType.YOU_OWE) {
                                "Pay ${debt.personName}"
                            } else {
                                "Request from ${debt.personName}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "₱${String.format("%.2f", debt.amount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (debt.type == DebtType.YOU_OWE) ErrorRed else SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { onSettleUp(debt) },
                        modifier = Modifier.size(width = 80.dp, height = 32.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = if (debt.type == DebtType.YOU_OWE) "Pay" else "Request",
                            fontSize = 12.sp
                        )
                    }
                }
                
                if (index < priorityDebts.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = LightGray
                    )
                }
            }
            
            if (debts.size > 3) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "+${debts.size - 3} more settlements needed",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ManualSettlementDialog(
    groupId: String,
    onDismiss: () -> Unit,
    onSettlementRecorded: () -> Unit,
    viewModel: SettlementViewModel
) {
    var selectedMember by remember { mutableStateOf<UserDebt?>(null) }
    var amount by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cash") }
    var settlementType by remember { mutableStateOf("I_PAID") } // I_PAID or I_RECEIVED
    var showMemberDropdown by remember { mutableStateOf(false) }
    
    val paymentMethods = listOf("Cash", "Bank Transfer", "Venmo", "PayPal", "Other")
    val members = viewModel.state.currentUserDebts
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                "Record Manual Settlement",
                style = MaterialTheme.typography.titleLarge
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Settlement type selection
                Text(
                    text = "Settlement Type",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { settlementType = "I_PAID" },
                        label = { Text("I paid someone") },
                        selected = settlementType == "I_PAID"
                    )
                    FilterChip(
                        onClick = { settlementType = "I_RECEIVED" },
                        label = { Text("Someone paid me") },
                        selected = settlementType == "I_RECEIVED"
                    )
                }
                
                // Member selection
                Text(
                    text = "Select member",
                    style = MaterialTheme.typography.labelMedium
                )
                
                ExposedDropdownMenuBox(
                    expanded = showMemberDropdown,
                    onExpandedChange = { showMemberDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedMember?.personName ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Member") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showMemberDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showMemberDropdown,
                        onDismissRequest = { showMemberDropdown = false }
                    ) {
                        members.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.personName) },
                                onClick = {
                                    selectedMember = member
                                    showMemberDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("₱") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Payment method
                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelMedium
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(paymentMethods) { method ->
                        FilterChip(
                            onClick = { paymentMethod = method },
                            label = { Text(method) },
                            selected = paymentMethod == method
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedMember?.let { member ->
                        val settlementAmount = amount.toDoubleOrNull()
                        if (settlementAmount != null && settlementAmount > 0) {
                            // Create a UserDebt object based on settlement type
                            val debt = if (settlementType == "I_PAID") {
                                member.copy(type = DebtType.YOU_OWE)
                            } else {
                                member.copy(type = DebtType.OWES_YOU)
                            }
                            
                            viewModel.recordSettlement(
                                groupId = groupId,
                                debtInfo = debt,
                                amount = settlementAmount,
                                paymentMethod = paymentMethod
                            )
                            onSettlementRecorded()
                        }
                    }
                },
                enabled = selectedMember != null && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Record Settlement")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettlementScreenPreview() {
    FairrTheme {
        SettlementScreen(
            navController = rememberNavController(),
            groupId = "1"
        )
    }
} 
