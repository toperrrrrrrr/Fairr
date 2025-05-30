package com.example.fairr.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.fairr.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToCreateGroup: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToGroupDetail: (String) -> Unit = {}
) {
    // Sample data for groups
    val groups = remember { 
        mutableStateListOf(
            GroupItem("1", "Weekend Trip", 4, -125.75, "$"),
            GroupItem("2", "Apartment Rent", 3, 150.25, "$"),
            GroupItem("3", "Dinner Party", 6, 0.00, "$"),
            GroupItem("4", "Office Lunch", 8, -45.50, "$")
        )
    }
    
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "FairShare",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    ) 
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = "Profile",
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
                onClick = { onNavigateToCreateGroup() },
                containerColor = DarkGreen,
                contentColor = PureWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Group")
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        ) 
                    },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkGreen,
                        selectedTextColor = DarkGreen,
                        unselectedIconColor = PlaceholderText,
                        unselectedTextColor = PlaceholderText
                    )
                )
                NavigationBarItem(
                    icon = { 
                        Icon(
                            if (selectedTab == 1) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        ) 
                    },
                    label = { Text("Profile") },
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        onNavigateToProfile()
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkGreen,
                        selectedTextColor = DarkGreen,
                        unselectedIconColor = PlaceholderText,
                        unselectedTextColor = PlaceholderText
                    )
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground)
                .padding(padding)
        ) {
            if (groups.isEmpty()) {
                // Empty state
                EmptyGroupsState(
                    onCreateGroup = { onNavigateToCreateGroup() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        // Summary Card
                        SummaryCard(groups = groups)
                    }
                    
                    item {
                        Text(
                            text = "Your Groups",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(groups) { group ->
                        GroupCard(
                            group = group,
                            onClick = { onNavigateToGroupDetail(group.id) }
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    groups: List<GroupItem>
) {
    val totalBalance = groups.sumOf { it.balance }
    val totalGroups = groups.size
    
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
                Column {
                    Text(
                        text = "Total Groups",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = totalGroups.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                
                Column {
                    Text(
                        text = "Your Balance",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when {
                            totalBalance > 0 -> "+$${String.format("%.2f", totalBalance)}"
                            totalBalance < 0 -> "-$${String.format("%.2f", kotlin.math.abs(totalBalance))}"
                            else -> "$0.00"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            totalBalance > 0 -> SuccessGreen
                            totalBalance < 0 -> ErrorRed
                            else -> TextPrimary
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCard(
    group: GroupItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
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
            // Group icon
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
                    Icons.Default.Group,
                    contentDescription = "Group",
                    tint = DarkGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Group info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${group.memberCount} members",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            // Balance
            Column(horizontalAlignment = Alignment.End) {
                val balanceColor = when {
                    group.balance > 0 -> SuccessGreen
                    group.balance < 0 -> ErrorRed
                    else -> TextSecondary
                }
                
                Text(
                    text = when {
                        group.balance > 0 -> "you get"
                        group.balance < 0 -> "you owe"
                        else -> "settled up"
                    },
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                
                Text(
                    text = when {
                        group.balance > 0 -> "+${group.currency}${String.format("%.2f", group.balance)}"
                        group.balance < 0 -> "-${group.currency}${String.format("%.2f", kotlin.math.abs(group.balance))}"
                        else -> "${group.currency}0.00"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = balanceColor
                )
            }
        }
    }
}

@Composable
fun EmptyGroupsState(
    onCreateGroup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Empty state icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    PlaceholderText.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Group,
                contentDescription = "No groups",
                modifier = Modifier.size(48.dp),
                tint = PlaceholderText
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Groups Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Create your first group to start splitting expenses with friends and family",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onCreateGroup,
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkGreen,
                contentColor = PureWhite
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Create Group",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Data class for group items
data class GroupItem(
    val id: String,
    val name: String,
    val memberCount: Int,
    val balance: Double,
    val currency: String = "$"
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FairrTheme {
        HomeScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyGroupsStatePreview() {
    FairrTheme {
        EmptyGroupsState(onCreateGroup = {})
    }
}
