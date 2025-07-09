package com.example.fairr.ui.screens.friends

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.fairr.data.friends.FriendService
import com.example.fairr.data.settlements.SettlementService
import com.example.fairr.ui.components.ModernCard
import com.example.fairr.ui.model.Friend
import com.example.fairr.ui.theme.*
import com.example.fairr.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendProfileUiState(
    val friend: Friend? = null,
    val totalOwed: Double = 0.0,
    val totalOwing: Double = 0.0,
    val netBalance: Double = 0.0,
    val bankDetails: FriendBankDetails? = null,
    val recentTransactions: List<FriendTransaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FriendBankDetails(
    val bankName: String,
    val accountNumber: String,
    val routingNumber: String? = null,
    val iban: String? = null,
    val preferredCurrency: String = "USD"
)

data class FriendTransaction(
    val id: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val date: String,
    val type: TransactionType,
    val groupName: String? = null
)

enum class TransactionType {
    YOU_OWE,    // You owe them money
    THEY_OWE,   // They owe you money
    SETTLED     // Transaction was settled
}

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    private val friendService: FriendService,
    private val settlementService: SettlementService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FriendProfileUiState())
    val uiState: StateFlow<FriendProfileUiState> = _uiState.asStateFlow()
    
    var snackbarMessage by mutableStateOf<String?>(null)
        private set
    
    fun loadFriendProfile(friendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // For now, create a mock friend for demonstration
                // TODO: Implement actual friend loading from the friends list
                val friend = Friend(
                    id = friendId,
                    name = "John Doe",
                    email = "john.doe@example.com",
                    photoUrl = null,
                    status = com.example.fairr.ui.model.FriendStatus.ACCEPTED
                )
                
                // Calculate balances (mock data for now)
                val balanceData = calculateBalances(friendId)
                
                // Load bank details (mock data for now)
                val bankDetails = loadBankDetails(friendId)
                
                // Load recent transactions (mock data for now)
                val transactions = loadRecentTransactions(friendId)
                
                _uiState.value = _uiState.value.copy(
                    friend = friend,
                    totalOwed = balanceData.first,
                    totalOwing = balanceData.second,
                    netBalance = balanceData.first - balanceData.second,
                    bankDetails = bankDetails,
                    recentTransactions = transactions,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load friend profile"
                )
            }
        }
    }
    
    private suspend fun calculateBalances(friendId: String): Pair<Double, Double> {
        // TODO: Implement actual balance calculation from shared expenses
        // For now, return mock data
        return Pair(125.50, 75.25) // (they owe you, you owe them)
    }
    
    private suspend fun loadBankDetails(friendId: String): FriendBankDetails? {
        // TODO: Implement actual bank details loading (with proper privacy controls)
        // For now, return mock data
        return FriendBankDetails(
            bankName = "Chase Bank",
            accountNumber = "****1234",
            routingNumber = "****5678",
            preferredCurrency = "USD"
        )
    }
    
    private suspend fun loadRecentTransactions(friendId: String): List<FriendTransaction> {
        // TODO: Implement actual transaction loading
        // For now, return mock data
        return listOf(
            FriendTransaction(
                id = "1",
                description = "Dinner at Pizza Palace",
                amount = 45.50,
                currency = "USD",
                date = "2024-01-15",
                type = TransactionType.YOU_OWE,
                groupName = "Weekend Friends"
            ),
            FriendTransaction(
                id = "2",
                description = "Movie tickets",
                amount = 30.00,
                currency = "USD",
                date = "2024-01-10",
                type = TransactionType.THEY_OWE,
                groupName = "Movie Night"
            ),
            FriendTransaction(
                id = "3",
                description = "Groceries",
                amount = 80.00,
                currency = "USD",
                date = "2024-01-05",
                type = TransactionType.SETTLED,
                groupName = "Household"
            )
        )
    }
    
    fun clearSnackbarMessage() {
        snackbarMessage = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    friendId: String,
    navController: NavController,
    onNavigateBack: () -> Unit,
    viewModel: FriendProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Load friend profile on screen load
    LaunchedEffect(friendId) {
        viewModel.loadFriendProfile(friendId)
    }
    
    // Handle snackbar messages
    LaunchedEffect(viewModel.snackbarMessage) {
        viewModel.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.friend?.name ?: "Friend Profile",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundPrimary
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            
            uiState.error != null -> {
                val errorMessage = uiState.error ?: "Unknown error"
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = ComponentColors.Error,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.loadFriendProfile(friendId) }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            else -> {
                val friend = uiState.friend
                if (friend != null) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Header
                        item {
                            FriendProfileHeader(
                                friend = friend,
                                netBalance = uiState.netBalance
                            )
                        }
                    
                        // Balance Summary
                        item {
                            BalanceSummaryCard(
                                totalOwed = uiState.totalOwed,
                                totalOwing = uiState.totalOwing,
                                netBalance = uiState.netBalance
                            )
                        }
                    
                        // Bank Details (if available and user has permission)
                        uiState.bankDetails?.let { bankDetails ->
                            item {
                                BankDetailsCard(bankDetails = bankDetails)
                            }
                        }
                    
                        // Recent Transactions
                        if (uiState.recentTransactions.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Recent Transactions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                            
                            items(uiState.recentTransactions) { transaction ->
                                TransactionCard(transaction = transaction)
                            }
                        }
                    }
                } else {
                    // This should not happen if uiState.friend is expected to be non-null
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Friend not found")
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendProfileHeader(
    friend: Friend,
    netBalance: Double,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CardBackground,
        cornerRadius = 16
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (friend.photoUrl != null && friend.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = friend.photoUrl,
                        contentDescription = "Profile picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default avatar with initials
                    Text(
                        text = friend.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Name and Email
            Text(
                text = friend.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Text(
                text = friend.email,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Net Balance
            val balanceColor = when {
                netBalance > 0 -> ComponentColors.Success
                netBalance < 0 -> ComponentColors.Error
                else -> TextSecondary
            }
            
            val balanceText = when {
                netBalance > 0 -> "owes you"
                netBalance < 0 -> "you owe"
                else -> "settled up"
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = when {
                        netBalance > 0 -> Icons.Default.TrendingUp
                        netBalance < 0 -> Icons.Default.TrendingDown
                        else -> Icons.Default.Balance
                    },
                    contentDescription = null,
                    tint = balanceColor,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = if (netBalance != 0.0) {
                        "${friend.name} $balanceText ${CurrencyFormatter.format("USD", kotlin.math.abs(netBalance))}"
                    } else {
                        "All settled up!"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = balanceColor
                )
            }
        }
    }
}

@Composable
private fun BalanceSummaryCard(
    totalOwed: Double,
    totalOwing: Double,
    netBalance: Double,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CardBackground,
        cornerRadius = 16
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Balance Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BalanceItem(
                    title = "They owe you",
                    amount = totalOwed,
                    color = ComponentColors.Success,
                    icon = Icons.Default.CallReceived
                )
                
                BalanceItem(
                    title = "You owe them",
                    amount = totalOwing,
                    color = ComponentColors.Error,
                    icon = Icons.Default.CallMade
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    title: String,
    amount: Double,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = CurrencyFormatter.format("USD", amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun BankDetailsCard(
    bankDetails: FriendBankDetails,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CardBackground,
        cornerRadius = 16
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Payment Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BankDetailItem(
                    label = "Bank",
                    value = bankDetails.bankName
                )
                
                BankDetailItem(
                    label = "Account",
                    value = bankDetails.accountNumber
                )
                
                bankDetails.routingNumber?.let {
                    BankDetailItem(
                        label = "Routing",
                        value = it
                    )
                }
                
                BankDetailItem(
                    label = "Currency",
                    value = bankDetails.preferredCurrency
                )
            }
        }
    }
}

@Composable
private fun BankDetailItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
private fun TransactionCard(
    transaction: FriendTransaction,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = CardBackground,
        cornerRadius = 12
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                val (icon, color) = when (transaction.type) {
                    TransactionType.YOU_OWE -> Icons.Default.CallMade to ComponentColors.Error
                    TransactionType.THEY_OWE -> Icons.Default.CallReceived to ComponentColors.Success
                    TransactionType.SETTLED -> Icons.Default.CheckCircle to ComponentColors.Info
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    transaction.groupName?.let { groupName ->
                        Text(
                            text = groupName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    
                    Text(
                        text = transaction.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Text(
                text = when (transaction.type) {
                    TransactionType.YOU_OWE -> "-${CurrencyFormatter.format(transaction.currency, transaction.amount)}"
                    TransactionType.THEY_OWE -> "+${CurrencyFormatter.format(transaction.currency, transaction.amount)}"
                    TransactionType.SETTLED -> CurrencyFormatter.format(transaction.currency, transaction.amount)
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = when (transaction.type) {
                    TransactionType.YOU_OWE -> ComponentColors.Error
                    TransactionType.THEY_OWE -> ComponentColors.Success
                    TransactionType.SETTLED -> TextSecondary
                }
            )
        }
    }
} 