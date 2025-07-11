package com.example.fairr.ui.screens.friends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import android.util.Log
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import com.example.fairr.ui.screens.settings.SettingsViewModel

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
    val preferredCurrency: String = "PHP"  // Changed from "USD" to "PHP" to match app default
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
    private val settlementService: SettlementService,
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(FriendProfileUiState())
    val uiState: StateFlow<FriendProfileUiState> = _uiState.asStateFlow()
    
    var snackbarMessage by mutableStateOf<String?>(null)
        private set
    
    fun loadFriendProfile(friendId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // Get the friend from the user's friends list
                val friends = friendService.getUserFriends().first()
                val friend = friends.find { it.id == friendId }
                
                if (friend == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Friend not found"
                    )
                    return@launch
                }
                
                // Calculate real balances from shared expenses
                val balanceData = calculateRealBalances(friendId)
                
                // Load recent transactions with this friend
                val transactions = loadRealTransactions(friendId, friend.name)
                
                // For now, bank details remain mock since this is sensitive data
                // In a real app, this would require user consent and proper privacy controls
                val bankDetails = loadBankDetails(friendId)
                
                _uiState.value = _uiState.value.copy(
                    friend = friend,
                    totalOwed = balanceData.first,  // They owe you
                    totalOwing = balanceData.second, // You owe them
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
    
    private suspend fun calculateRealBalances(friendId: String): Pair<Double, Double> {
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return Pair(0.0, 0.0)
            
            // Get all groups where both users are members
            val userGroups = groupRepository.getUserGroups().first()
            var theyOweYou = 0.0
            var youOweThem = 0.0
            
            userGroups.forEach { group ->
                // Check if the friend is in this group
                if (group.members.any { it.userId == friendId }) {
                    val groupExpenses = expenseRepository.getExpensesByGroupId(group.id)
                    
                    groupExpenses.forEach { expense ->
                        // Case 1: You paid, friend owes their share
                        if (expense.paidBy == currentUserId) {
                            val friendSplit = expense.splitBetween.find { it.userId == friendId }
                            if (friendSplit != null) {
                                theyOweYou += friendSplit.share
                            }
                        }
                        
                        // Case 2: Friend paid, you owe your share
                        if (expense.paidBy == friendId) {
                            val yourSplit = expense.splitBetween.find { it.userId == currentUserId }
                            if (yourSplit != null) {
                                youOweThem += yourSplit.share
                            }
                        }
                    }
                }
            }
            
            return Pair(theyOweYou, youOweThem)
        } catch (e: Exception) {
            Log.e("FriendProfileViewModel", "Error calculating balances", e)
            return Pair(0.0, 0.0)
        }
    }
    
    private suspend fun loadRealTransactions(friendId: String, friendName: String): List<FriendTransaction> {
        try {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
            val transactions = mutableListOf<FriendTransaction>()
            
            // Get all groups where both users are members
            val userGroups = groupRepository.getUserGroups().first()
            
            userGroups.forEach { group ->
                if (group.members.any { it.userId == friendId }) {
                    val groupExpenses = expenseRepository.getExpensesByGroupId(group.id)
                    
                    groupExpenses.forEach { expense ->
                        val expenseDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(expense.date.toDate())
                        
                        // Case 1: You paid, friend owes their share
                        if (expense.paidBy == currentUserId) {
                            val friendSplit = expense.splitBetween.find { it.userId == friendId }
                            if (friendSplit != null && friendSplit.share > 0) {
                                transactions.add(
                                    FriendTransaction(
                                        id = "${expense.id}_${friendId}_owes",
                                        description = expense.description,
                                        amount = friendSplit.share,
                                        currency = expense.currency,
                                        date = expenseDate,
                                        type = TransactionType.THEY_OWE,
                                        groupName = group.name
                                    )
                                )
                            }
                        }
                        
                        // Case 2: Friend paid, you owe your share
                        if (expense.paidBy == friendId) {
                            val yourSplit = expense.splitBetween.find { it.userId == currentUserId }
                            if (yourSplit != null && yourSplit.share > 0) {
                                transactions.add(
                                    FriendTransaction(
                                        id = "${expense.id}_${currentUserId}_owes",
                                        description = expense.description,
                                        amount = yourSplit.share,
                                        currency = expense.currency,
                                        date = expenseDate,
                                        type = TransactionType.YOU_OWE,
                                        groupName = group.name
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Sort by date (most recent first) and return latest 10
            return transactions.sortedByDescending { it.date }.take(10)
        } catch (e: Exception) {
            Log.e("FriendProfileViewModel", "Error loading transactions", e)
            return emptyList()
        }
    }
    
    private suspend fun loadBankDetails(friendId: String): FriendBankDetails? {
        // TODO: Implement actual bank details loading (with proper privacy controls)
        // For now, return mock data
        return FriendBankDetails(
            bankName = "Chase Bank",
            accountNumber = "****1234",
            routingNumber = "****5678",
            preferredCurrency = "PHP"  // Changed from "USD" to "PHP" to match app default
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
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val userCurrency = settingsViewModel.selectedCurrency
    
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
                                netBalance = uiState.netBalance,
                                userCurrency = userCurrency
                            )
                        }
                    
                        // Balance Summary
                        item {
                            BalanceSummaryCard(
                                totalOwed = uiState.totalOwed,
                                totalOwing = uiState.totalOwing,
                                netBalance = uiState.netBalance,
                                userCurrency = userCurrency
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
                                TransactionCard(
                                    transaction = transaction,
                                    userCurrency = userCurrency
                                )
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
    userCurrency: String,
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
                val (icon, color) = when {
                    netBalance > 0 -> Icons.AutoMirrored.Filled.TrendingUp to ComponentColors.Success
                    netBalance < 0 -> Icons.AutoMirrored.Filled.TrendingDown to ComponentColors.Error
                    else -> Icons.Default.Balance to TextSecondary
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                
                Text(
                    text = if (netBalance != 0.0) {
                        "${friend.name} $balanceText ${CurrencyFormatter.format(userCurrency, kotlin.math.abs(netBalance))}"
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
    userCurrency: String,
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
                    userCurrency = userCurrency,
                    color = ComponentColors.Success,
                    icon = Icons.AutoMirrored.Filled.CallReceived
                )
                
                BalanceItem(
                    title = "You owe them",
                    amount = totalOwing,
                    userCurrency = userCurrency,
                    color = ComponentColors.Error,
                    icon = Icons.AutoMirrored.Filled.CallMade
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    title: String,
    amount: Double,
    userCurrency: String,
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
            text = CurrencyFormatter.format(userCurrency, amount),
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
    userCurrency: String,
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
                    TransactionType.YOU_OWE -> Icons.AutoMirrored.Filled.CallMade to ComponentColors.Error
                    TransactionType.THEY_OWE -> Icons.AutoMirrored.Filled.CallReceived to ComponentColors.Success
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
                    TransactionType.YOU_OWE -> "-${CurrencyFormatter.format(userCurrency, transaction.amount)}"
                    TransactionType.THEY_OWE -> "+${CurrencyFormatter.format(userCurrency, transaction.amount)}"
                    TransactionType.SETTLED -> CurrencyFormatter.format(userCurrency, transaction.amount)
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