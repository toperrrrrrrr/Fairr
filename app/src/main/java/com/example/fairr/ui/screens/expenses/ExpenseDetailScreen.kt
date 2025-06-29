package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseSplit
import com.example.fairr.ui.theme.*
import com.example.fairr.util.CurrencyFormatter
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.automirrored.filled.CallSplit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    expenseId: String,
    onEditExpense: () -> Unit = {},
    onDeleteExpense: () -> Unit = {},
    viewModel: ExpenseDetailViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Expense Details",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = TextSecondary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEditExpense()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onDeleteExpense()
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is ExpenseDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading expense...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
            is ExpenseDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ErrorRed
                        )
                        Text(
                            text = uiState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Button(onClick = { viewModel.refresh() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is ExpenseDetailUiState.Success -> {
                val expense = uiState.expense
                val groupName = uiState.groupName
                val currentUserId = uiState.currentUserId
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightBackground)
                        .padding(padding),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Expense Overview Card
                    item {
                        ExpenseOverviewCard(
                            expense = expense,
                            groupName = groupName,
                            viewModel = viewModel,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    // Split Details Section
                    item {
                        Text(
                            text = "Split Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    items(expense.splitBetween) { split ->
                        ParticipantCard(
                            split = split,
                            expense = expense,
                            currentUserId = currentUserId,
                            viewModel = viewModel,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseOverviewCard(
    expense: Expense,
    groupName: String,
    viewModel: ExpenseDetailViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header with description and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = groupName,
                        fontSize = 15.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Enhanced icon with better styling
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            DarkGreen.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = "Expense",
                        tint = DarkGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enhanced amount display
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Total Amount",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = CurrencyFormatter.format(expense.currency, expense.amount),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    lineHeight = 42.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enhanced details grid with better spacing
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnhancedDetailItem(
                        label = "Paid by",
                        value = expense.paidByName,
                        icon = Icons.Default.Person
                    )
                    EnhancedDetailItem(
                        label = "Date",
                        value = viewModel.formatDate(expense.date),
                        icon = Icons.Default.CalendarToday
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EnhancedDetailItem(
                        label = "Category",
                        value = expense.category.name.replace("_", " ").capitalize(),
                        icon = Icons.Default.Category
                    )
                    EnhancedDetailItem(
                        label = "Split Type",
                        value = expense.splitType,
                        icon = Icons.Default.CallSplit
                    )
                }
                
                if (expense.notes.isNotEmpty()) {
                    EnhancedDetailItem(
                        label = "Notes",
                        value = expense.notes,
                        icon = Icons.Default.Note,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun EnhancedDetailItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun DetailItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun ParticipantCard(
    split: ExpenseSplit,
    expense: Expense,
    currentUserId: String,
    viewModel: ExpenseDetailViewModel,
    modifier: Modifier = Modifier
) {
    val isPayer = split.userId == expense.paidBy
    val isCurrentUser = split.userId == currentUserId
    
    Card(
        modifier = modifier
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
                        if (isPayer) DarkGreen.copy(alpha = 0.2f) 
                        else if (isCurrentUser) DarkBlue.copy(alpha = 0.2f)
                        else PlaceholderText.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Participant",
                    tint = if (isPayer) DarkGreen 
                          else if (isCurrentUser) DarkBlue
                          else PlaceholderText,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and status
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCurrentUser) "${split.userName} (You)" else split.userName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (isPayer) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = DarkGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "PAID",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Amount
            Text(
                text = CurrencyFormatter.format(expense.currency, split.share),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// Extension function to capitalize first letter
fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1).lowercase()
    } else {
        this
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseDetailScreenPreview() {
    FairrTheme {
        ExpenseDetailScreen(
            navController = rememberNavController(),
            expenseId = "1"
        )
    }
} 
