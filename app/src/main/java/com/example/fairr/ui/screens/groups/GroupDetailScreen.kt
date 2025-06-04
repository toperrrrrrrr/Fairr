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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.components.*
import com.example.fairr.ui.theme.*

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    navController: NavController,
    groupId: String,
    onNavigateToAddExpense: () -> Unit = {}
) {
    // Sample group data
    val group = remember {
        GroupDetail(
            id = groupId,
            name = "Weekend Trip",
            description = "Mountain hiking adventure",
            memberCount = 4,
            totalExpenses = 650.0,
            yourBalance = -125.75,
            currency = "$",
            members = listOf(
                Member("1", "John Doe", -125.75),
                Member("2", "Jane Smith", 50.25),
                Member("3", "Mike Johnson", 0.0),
                Member("4", "Sarah Wilson", 75.50)
            )
        )
    }

    val expenses = remember {
        listOf(
            ExpenseItem("1", "Lunch at Mountain Cafe", 80.0, "John Doe", "12/28"),
            ExpenseItem("2", "Gas for Car", 45.50, "Jane Smith", "12/27"),
            ExpenseItem("3", "Accommodation", 200.0, "Mike Johnson", "12/26"),
            ExpenseItem("4", "Groceries", 65.25, "Sarah Wilson", "12/26")
        )
    }

    var showGroupMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(
                            group.name,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 18.sp
                        )
                        Text(
                            "${group.memberCount} members",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
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
                    Box {
                        IconButton(onClick = { showGroupMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = IconTint
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showGroupMenu,
                            onDismissRequest = { showGroupMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Group Settings") },
                                onClick = {
                                    showGroupMenu = false
                                    navController.navigate("group_settings/$groupId")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("View Analytics") },
                                onClick = {
                                    showGroupMenu = false
                                    navController.navigate("analytics")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Analytics, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export Data") },
                                onClick = {
                                    showGroupMenu = false
                                    navController.navigate("export_data/$groupId")
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddExpense() },
                containerColor = Primary,
                contentColor = TextOnDark,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundSecondary)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Group Stats Overview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernStatsCard(
                        title = "Total Spent",
                        value = "${group.currency}${String.format("%.2f", group.totalExpenses)}",
                        icon = Icons.Default.Receipt,
                        changeValue = "This month",
                        modifier = Modifier.weight(1f)
                    )
                    
                    ModernStatsCard(
                        title = "Your Balance",
                        value = "${group.currency}${String.format("%.2f", kotlin.math.abs(group.yourBalance))}",
                        icon = Icons.Default.AccountBalance,
                        changeValue = if (group.yourBalance >= 0) "You're owed" else "You owe",
                        changePositive = group.yourBalance >= 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Add Expense",
                        icon = Icons.Default.Add,
                        onClick = { onNavigateToAddExpense() },
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickActionCard(
                        title = "Settle Up",
                        icon = Icons.Default.Payment,
                        onClick = { navController.navigate("settlements") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Scan Receipt",
                        icon = Icons.Default.CameraAlt,
                        onClick = { navController.navigate("photo_capture") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickActionCard(
                        title = "Analytics",
                        icon = Icons.Default.Analytics,
                        onClick = { navController.navigate("analytics") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Group Members
            item {
                Text(
                    text = "Members",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ModernCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        group.members.forEachIndexed { index, member ->
                            ModernListItem(
                                title = member.name,
                                subtitle = when {
                                    member.balance > 0 -> "Gets back ${group.currency}${String.format("%.2f", member.balance)}"
                                    member.balance < 0 -> "Owes ${group.currency}${String.format("%.2f", kotlin.math.abs(member.balance))}"
                                    else -> "All settled up"
                                },
                                leadingIcon = Icons.Default.Person,
                                trailingContent = {
                                    Text(
                                        text = if (member.balance >= 0) "+${group.currency}${String.format("%.2f", member.balance)}" 
                                               else "${group.currency}${String.format("%.2f", member.balance)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (member.balance >= 0) SuccessGreen else ErrorRed
                                    )
                                }
                            )
                            
                            if (index < group.members.size - 1) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    modifier = Modifier.padding(horizontal = 0.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Recent Expenses
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Expenses",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    TextButton(
                        onClick = { /* Navigate to all expenses */ }
                    ) {
                        Text(
                            text = "View All",
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ModernCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        expenses.take(5).forEachIndexed { index, expense ->
                            ModernListItem(
                                title = expense.description,
                                subtitle = "Paid by ${expense.paidBy} • ${expense.date}",
                                leadingIcon = Icons.Default.Receipt,
                                trailingContent = {
                                    Text(
                                        text = "${group.currency}${String.format("%.2f", expense.amount)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                },
                                onClick = { /* Navigate to expense detail */ }
                            )
                            
                            if (index < expenses.size - 1) {
                                HorizontalDivider(
                                    color = DividerColor,
                                    modifier = Modifier.padding(horizontal = 0.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

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
