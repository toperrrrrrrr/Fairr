package com.example.fairr.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
fun AnalyticsScreen(
    navController: NavController
) {
    // Sample analytics data
    val monthlySpending = remember {
        listOf(
            MonthlyData("Jan", 245.50),
            MonthlyData("Feb", 380.75),
            MonthlyData("Mar", 532.25),
            MonthlyData("Apr", 298.90),
            MonthlyData("May", 445.60),
            MonthlyData("Jun", 612.30)
        )
    }
    
    val categoryBreakdown = remember {
        listOf(
            CategoryData("Food & Dining", 435.80, 35.2f, DarkGreen),
            CategoryData("Transportation", 298.50, 24.1f, DarkBlue),
            CategoryData("Entertainment", 187.25, 15.1f, ErrorRed),
            CategoryData("Shopping", 156.70, 12.7f, WarningOrange),
            CategoryData("Accommodation", 98.40, 7.9f, SuccessGreen),
            CategoryData("Other", 62.35, 5.0f, PlaceholderText)
        )
    }
    
    val groupStats = remember {
        listOf(
            GroupStats("Weekend Trip", 3, 245.80),
            GroupStats("Apartment Rent", 4, 1200.00),
            GroupStats("Office Lunch", 8, 156.40),
            GroupStats("Dinner Party", 6, 89.20)
        )
    }
    
    var selectedPeriod by remember { mutableStateOf("This Month") }
    val periods = listOf("This Week", "This Month", "Last 3 Months", "This Year")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Analytics",
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
            
            // Period Selector
            item {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(periods) { period ->
                        FilterChip(
                            onClick = { selectedPeriod = period },
                            label = { Text(period, fontSize = 12.sp) },
                            selected = selectedPeriod == period,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkGreen,
                                selectedLabelColor = NeutralWhite,
                                containerColor = NeutralWhite,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
            
            // Overview Stats Card
            item {
                OverviewStatsCard(
                    totalSpent = 1239.00,
                    totalGroups = 4,
                    avgPerExpense = 28.50,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Monthly Spending Chart
            item {
                Text(
                    text = "Spending Trend",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                MonthlySpendingChart(
                    data = monthlySpending,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Category Breakdown
            item {
                Text(
                    text = "Spending by Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            item {
                CategoryBreakdownCard(
                    categories = categoryBreakdown,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Group Statistics
            item {
                Text(
                    text = "Group Statistics",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            items(groupStats) { group ->
                GroupStatsCard(
                    group = group,
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
fun OverviewStatsCard(
    totalSpent: Double,
    totalGroups: Int,
    avgPerExpense: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Overview",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Spent",
                    value = "$${String.format("%.2f", totalSpent)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = DarkGreen
                )
                StatItem(
                    label = "Active Groups",
                    value = totalGroups.toString(),
                    icon = Icons.Default.Group,
                    color = DarkBlue
                )
                StatItem(
                    label = "Avg per Expense",
                    value = "$${String.format("%.2f", avgPerExpense)}",
                    icon = Icons.Default.Receipt,
                    color = WarningOrange
                )
            }
        }
    }
}

@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun MonthlySpendingChart(
    data: List<MonthlyData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Last 6 Months",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Simple bar chart representation
            val maxValue = data.maxOfOrNull { it.amount } ?: 1.0
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEach { monthData ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(((monthData.amount / maxValue) * 80).dp)
                                .background(
                                    DarkGreen.copy(alpha = 0.8f),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = monthData.month,
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    categories: List<CategoryData>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            categories.forEach { category ->
                CategoryItem(
                    category = category,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CategoryData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(category.color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            LinearProgressIndicator(
                progress = { category.percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = category.color,
                trackColor = category.color.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format("%.2f", category.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${String.format("%.1f", category.percentage)}%",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun GroupStatsCard(
    group: GroupStats,
    modifier: Modifier = Modifier
) {
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Group,
                    contentDescription = "Group",
                    tint = DarkGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "${group.memberCount} members",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Text(
                text = "$${String.format("%.2f", group.totalSpent)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
        }
    }
}

// Data classes
data class MonthlyData(
    val month: String,
    val amount: Double
)

data class CategoryData(
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: androidx.compose.ui.graphics.Color
)

data class GroupStats(
    val name: String,
    val memberCount: Int,
    val totalSpent: Double
)

@Preview(showBackground = true)
@Composable
fun AnalyticsScreenPreview() {
    FairrTheme {
        AnalyticsScreen(
            navController = rememberNavController()
        )
    }
} 
