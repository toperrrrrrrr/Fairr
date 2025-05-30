package com.example.fairr.ui.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
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
fun GroupDetailScreen(
    navController: NavController,
    groupId: String,
    onNavigateToAddExpense: () -> Unit = {}
) {
    // Sample data
    val group = remember {
        GroupDetailData(
            id = groupId,
            name = "Weekend Trip",
            memberCount = 4,
            totalExpenses = 845.50,
            currency = "$",
            userBalance = -125.75
        )
    }
    
    val members = remember {
        listOf(
            MemberData("1", "Alice Johnson", 150.25, true),
            MemberData("2", "Bob Smith", -75.50, false),
            MemberData("3", "Charlie Brown", -125.75, false),
            MemberData("4", "Diana Prince", 51.00, false)
        )
    }
    
    val recentExpenses = remember {
        listOf(
            ExpenseData("1", "Hotel Booking", 300.00, "Alice Johnson", "2 days ago"),
            ExpenseData("2", "Gas Station", 85.50, "Bob Smith", "3 days ago"),
            ExpenseData("3", "Restaurant", 145.75, "Charlie Brown", "4 days ago"),
            ExpenseData("4", "Groceries", 67.25, "Diana Prince", "5 days ago")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        group.name,
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
                    IconButton(onClick = { /* TODO: Show group options */ }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextSecondary
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
                onClick = { onNavigateToAddExpense() },
                containerColor = DarkGreen,
                contentColor = PureWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
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
            
            // Group Summary Card
            item {
                GroupSummaryCard(
                    group = group,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Your Balance Card
            item {
                BalanceCard(
                    balance = group.userBalance,
                    currency = group.currency,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Members Section
            item {
                Text(
                    text = "Members",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(members) { member ->
                MemberCard(
                    member = member,
                    currency = group.currency,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Recent Expenses Section
            item {
                Text(
                    text = "Recent Expenses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(recentExpenses) { expense ->
                ExpenseCard(
                    expense = expense,
                    currency = group.currency,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

@Composable
fun GroupSummaryCard(
    group: GroupDetailData,
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
                Column {
                    Text(
                        text = "Total Expenses",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${group.currency}${String.format("%.2f", group.totalExpenses)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
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
                        contentDescription = "Expenses",
                        tint = DarkGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Members",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${group.memberCount}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                
                Column {
                    Text(
                        text = "Currency",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = group.currency,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun BalanceCard(
    balance: Double,
    currency: String,
    modifier: Modifier = Modifier
) {
    val isOwed = balance > 0
    val balanceColor = if (isOwed) SuccessGreen else ErrorRed
    val balanceText = if (isOwed) "You are owed" else "You owe"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwed) SuccessGreen.copy(alpha = 0.05f) else ErrorRed.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = balanceText,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$currency${String.format("%.2f", kotlin.math.abs(balance))}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
            
            if (!isOwed) {
                Button(
                    onClick = { /* TODO: Settle up */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = PureWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Settle Up",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    member: MemberData,
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
                        if (member.isCurrentUser) DarkGreen.copy(alpha = 0.2f) else PlaceholderText.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Member",
                    tint = if (member.isCurrentUser) DarkGreen else PlaceholderText,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (member.isCurrentUser) "${member.name} (You)" else member.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            
            // Balance
            val balanceColor = when {
                member.balance > 0 -> SuccessGreen
                member.balance < 0 -> ErrorRed
                else -> TextSecondary
            }
            
            Text(
                text = when {
                    member.balance > 0 -> "+$currency${String.format("%.2f", member.balance)}"
                    member.balance < 0 -> "-$currency${String.format("%.2f", kotlin.math.abs(member.balance))}"
                    else -> "$currency 0.00"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = balanceColor
            )
        }
    }
}

@Composable
fun ExpenseCard(
    expense: ExpenseData,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Paid by ${expense.paidBy} • ${expense.date}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Text(
                text = "$currency${String.format("%.2f", expense.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// Data classes
data class GroupDetailData(
    val id: String,
    val name: String,
    val memberCount: Int,
    val totalExpenses: Double,
    val currency: String,
    val userBalance: Double
)

data class MemberData(
    val id: String,
    val name: String,
    val balance: Double,
    val isCurrentUser: Boolean
)

data class ExpenseData(
    val id: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val date: String
)

@Preview(showBackground = true)
@Composable
fun GroupDetailScreenPreview() {
    FairrTheme {
        GroupDetailScreen(
            navController = rememberNavController(),
            groupId = "1"
        )
    }
} 