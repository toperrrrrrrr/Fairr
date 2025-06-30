package com.example.fairr.ui.screens.settlements

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.ui.theme.*
import com.example.fairr.navigation.Screen
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.settlements.SettlementService
import com.example.fairr.util.CurrencyFormatter
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// Data classes for overview
data class GroupBalance(
    val groupId: String,
    val groupName: String,
    val memberCount: Int,
    val userBalance: Double, // Positive = user is owed money, Negative = user owes money
    val hasActiveDebts: Boolean
)

data class OverviewUiState(
    val isLoading: Boolean = false,
    val totalBalance: Double = 0.0,
    val groupBalances: List<GroupBalance> = emptyList(),
    val error: String? = null
)

// ViewModel for the overview screen
@HiltViewModel
class SettlementsOverviewViewModel @Inject constructor(
    private val groupService: GroupService,
    private val settlementService: SettlementService,
    private val auth: FirebaseAuth
) : ViewModel() {
    
    var uiState by mutableStateOf(OverviewUiState())
        private set
    
    init {
        loadOverviewData()
    }
    
    private fun loadOverviewData() {
        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null)
                
                val userGroups = groupService.getUserGroups().first()
                val groupBalances = mutableListOf<GroupBalance>()
                var totalBalance = 0.0
                
                userGroups.forEach { group ->
                    try {
                        val settlementSummary = settlementService.getSettlementSummary(group.id)
                        val currentUserId = auth.currentUser?.uid ?: ""
                        val userSummary = settlementSummary.find { it.userId == currentUserId }
                        
                        val balance = userSummary?.netBalance ?: 0.0
                        val hasDebts = userSummary?.let { it.totalOwed > 0 || it.totalOwedToThem > 0 } ?: false
                        
                        groupBalances.add(
                            GroupBalance(
                                groupId = group.id,
                                groupName = group.name,
                                memberCount = group.members.size,
                                userBalance = balance,
                                hasActiveDebts = hasDebts
                            )
                        )
                        
                        totalBalance += balance
                    } catch (e: Exception) {
                        // Continue with other groups if one fails
                    }
                }
                
                uiState = uiState.copy(
                    isLoading = false,
                    totalBalance = totalBalance,
                    groupBalances = groupBalances.filter { it.hasActiveDebts }
                )
                
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load settlement overview"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettlementsOverviewScreen(
    navController: NavController,
    viewModel: SettlementsOverviewViewModel = hiltViewModel()
) {
    val state = viewModel.uiState
    val snackbarHostState = remember { SnackbarHostState() }

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
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightBackground)
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overview Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = "Overall Balance",
                                tint = Primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Overall Balance",
                                fontSize = 16.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = CurrencyFormatter.format(state.totalBalance),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    state.totalBalance > 0 -> SuccessGreen
                                    state.totalBalance < 0 -> ErrorRed
                                    else -> TextPrimary
                                }
                            )
                            
                            Text(
                                text = when {
                                    state.totalBalance > 0 -> "You are owed"
                                    state.totalBalance < 0 -> "You owe"
                                    else -> "All settled"
                                },
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Groups with settlements
                if (state.groupBalances.isNotEmpty()) {
                    item {
                        Text(
                            text = "Groups with Outstanding Balances",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    items(state.groupBalances) { groupBalance ->
                        GroupBalanceCard(
                            groupBalance = groupBalance,
                            onNavigateToGroup = {
                                navController.navigate(Screen.Settlement.createRoute(groupBalance.groupId))
                            }
                        )
                    }
                } else {
                    // Empty state - all settled
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
                                    text = "No outstanding balances across all groups",
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
}

@Composable
private fun GroupBalanceCard(
    groupBalance: GroupBalance,
    onNavigateToGroup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToGroup() }
            .shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = NeutralWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = groupBalance.groupName.take(1).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Column {
                    Text(
                        text = groupBalance.groupName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${groupBalance.memberCount} members",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CurrencyFormatter.format(kotlin.math.abs(groupBalance.userBalance)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (groupBalance.userBalance < 0) ErrorRed else SuccessGreen
                )
                Text(
                    text = if (groupBalance.userBalance < 0) "You owe" else "You're owed",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
} 