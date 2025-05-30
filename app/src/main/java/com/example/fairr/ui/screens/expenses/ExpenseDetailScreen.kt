package com.example.fairr.ui.screens.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    expenseId: String,
    onEditExpense: () -> Unit = {},
    onDeleteExpense: () -> Unit = {}
) {
    // Sample expense data
    val expense = remember {
        ExpenseDetailData(
            id = expenseId,
            description = "Dinner at Italian Restaurant",
            amount = 120.00,
            currency = "$",
            category = "Food & Dining",
            paidBy = "Alice Johnson",
            paidByUserId = "1",
            date = "March 15, 2024",
            splitType = "Equal Split",
            groupName = "Weekend Trip"
        )
    }
    
    val participants = remember {
        listOf(
            ExpenseParticipant("1", "Alice Johnson", 30.00, true, true),
            ExpenseParticipant("2", "Bob Smith", 30.00, false, false),
            ExpenseParticipant("3", "Charlie Brown", 30.00, false, false),
            ExpenseParticipant("4", "Diana Prince", 30.00, false, false)
        )
    }
    
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
                            Icons.AutoMirrored.Filled.ArrowBack,
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
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Edit Expense")
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onEditExpense()
                                }
                            )
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = ErrorRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Delete Expense", color = ErrorRed)
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteExpense()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PureWhite
                )
            )
        }
    ) { padding ->
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
            
            items(participants) { participant ->
                ParticipantCard(
                    participant = participant,
                    currency = expense.currency,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ExpenseOverviewCard(
    expense: ExpenseDetailData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.description,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = expense.groupName,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            DarkGreen.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = "Expense",
                        tint = DarkGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Amount
            Text(
                text = "${expense.currency}${String.format("%.2f", expense.amount)}",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Paid by",
                    value = expense.paidBy
                )
                DetailItem(
                    label = "Date",
                    value = expense.date
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DetailItem(
                    label = "Category",
                    value = expense.category
                )
                DetailItem(
                    label = "Split Type",
                    value = expense.splitType
                )
            }
        }
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
    participant: ExpenseParticipant,
    currency: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite)
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
                        if (participant.isPayer) DarkGreen.copy(alpha = 0.2f) 
                        else if (participant.isCurrentUser) DarkBlue.copy(alpha = 0.2f)
                        else PlaceholderText.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Participant",
                    tint = if (participant.isPayer) DarkGreen 
                          else if (participant.isCurrentUser) DarkBlue
                          else PlaceholderText,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and status
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (participant.isCurrentUser) "${participant.name} (You)" else participant.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    if (participant.isPayer) {
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
                text = "$currency${String.format("%.2f", participant.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// Data classes
data class ExpenseDetailData(
    val id: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val category: String,
    val paidBy: String,
    val paidByUserId: String,
    val date: String,
    val splitType: String,
    val groupName: String
)

data class ExpenseParticipant(
    val id: String,
    val name: String,
    val amount: Double,
    val isPayer: Boolean,
    val isCurrentUser: Boolean
)

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