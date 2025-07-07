# AI TRAINING DATA: User Scenarios & Real-World Usage Patterns - Fairr Android App

## 1. PRIMARY USER JOURNEYS

### **User Journey 1: New User Onboarding**
```
SCENARIO: First-time user creates account and joins a group
User Flow: WelcomeScreen → SignUpScreen → OnboardingScreen → GroupJoinScreen → HomeScreen

Implementation Files:
├── ui/screens/auth/WelcomeScreen.kt → Initial app experience
├── ui/screens/auth/ModernSignUpScreen.kt → Account creation
├── ui/screens/onboarding/OnboardingScreen.kt → Feature introduction  
├── ui/screens/groups/GroupJoinScreen.kt → Group invitation handling
└── ui/screens/home/HomeScreen.kt → Main dashboard

State Management:
├── ui/screens/auth/AuthViewModel.kt → Authentication state
├── ui/screens/onboarding/OnboardingViewModel.kt → Onboarding progress
├── ui/viewmodels/StartupViewModel.kt → App initialization
└── data/preferences/UserPreferencesManager.kt → User preferences
```

**Critical Success Metrics:**
- Account creation completion rate: >85%
- Onboarding completion rate: >75%
- First group join rate: >60%
- Time to first expense: <10 minutes

**Edge Cases Handled:**
```kotlin
// EDGE CASE: Network failure during signup (AuthViewModel.kt)
sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String, val isRetryable: Boolean) : AuthState()
    object NetworkError : AuthState() // Specific network handling
}

// IMPLEMENTATION: Robust signup with retry logic
suspend fun signUpWithEmail(email: String, password: String): Result<Unit> {
    return try {
        _authState.value = AuthState.Loading
        
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            // Send verification email with retry
            user.sendEmailVerification().await()
            _authState.value = AuthState.Authenticated(user)
            Result.success(Unit)
        } ?: Result.failure(Exception("Account creation failed"))
        
    } catch (e: FirebaseAuthException) {
        handleAuthException(e)
    } catch (e: Exception) {
        _authState.value = AuthState.NetworkError
        Result.failure(e)
    }
}
```

### **User Journey 2: Trip Expense Management**
```
SCENARIO: Friends planning a vacation, tracking shared expenses
User Flow: CreateGroupScreen → AddExpenseScreen → ExpenseDetailScreen → SettlementScreen

Typical Trip Expenses:
├── Accommodation: $800 (paid by User A, split equally among 4 people)
├── Car Rental: $300 (paid by User B, split equally)
├── Dinner Day 1: $120 (paid by User C, split equally)
├── Groceries: $80 (paid by User A, split equally)
├── Gas: $60 (paid by User B, split equally)
└── Activities: $200 (paid by User D, split equally)

Expected Settlement:
├── User A owes: $185 (paid $880, owes $365)
├── User B owes: $85 (paid $360, owes $365) 
├── User C owes: $245 (paid $120, owes $365)
└── User D owes: $165 (paid $200, owes $365)
```

**Implementation with Business Logic:**
```kotlin
// SCENARIO: Complex expense creation (AddExpenseViewModel.kt)
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupService: GroupService,
    private val splitCalculator: SplitCalculator
) : ViewModel() {
    
    // Real-world validation for trip expenses
    fun validateExpense(expense: ExpenseData): ValidationResult {
        return when {
            expense.amount <= 0 -> ValidationResult.Error("Amount must be greater than 0")
            expense.amount > 10000 -> ValidationResult.Error("Amount seems unusually high for group expense")
            expense.description.isBlank() -> ValidationResult.Error("Description is required")
            expense.description.length > 100 -> ValidationResult.Error("Description too long")
            expense.splitBetween.isEmpty() -> ValidationResult.Error("At least one person must be included")
            expense.splitBetween.sumOf { it.share } != expense.amount -> 
                ValidationResult.Error("Split amounts don't match total")
            else -> ValidationResult.Success
        }
    }
    
    // Handle complex split scenarios
    suspend fun createExpense(expenseData: ExpenseData) {
        val validation = validateExpense(expenseData)
        if (validation is ValidationResult.Error) {
            _uiState.value = AddExpenseUiState.Error(validation.message)
            return
        }
        
        try {
            _uiState.value = AddExpenseUiState.Loading
            
            // Calculate splits based on user selection
            val splits = when (expenseData.splitType) {
                "Equal Split" -> splitCalculator.calculateEqualSplit(
                    expenseData.amount, 
                    expenseData.selectedMembers
                )
                "Custom Amount" -> expenseData.customSplits
                "Percentage" -> splitCalculator.calculatePercentageSplit(
                    expenseData.amount,
                    expenseData.percentageSplits
                )
                else -> throw IllegalArgumentException("Unknown split type")
            }
            
            val expense = Expense(
                id = "", // Will be generated by Firestore
                groupId = expenseData.groupId,
                description = expenseData.description,
                amount = expenseData.amount,
                currency = expenseData.currency,
                date = expenseData.date,
                paidBy = auth.currentUser?.uid ?: "",
                paidByName = auth.currentUser?.displayName ?: "",
                splitBetween = splits,
                category = expenseData.category
            )
            
            val expenseId = expenseRepository.addExpense(expense)
            _uiState.value = AddExpenseUiState.Success(expenseId)
            
        } catch (e: Exception) {
            _uiState.value = AddExpenseUiState.Error("Failed to create expense: ${e.message}")
        }
    }
}
```

### **User Journey 3: Household Expense Tracking**
```
SCENARIO: Roommates tracking monthly recurring expenses
User Flow: RecurringExpenseManagementScreen → Analytics → Settlement

Monthly Recurring Expenses:
├── Rent: $2400 (split equally among 3 roommates = $800 each)
├── Utilities: ~$150 (varies monthly, split equally)
├── Internet: $80 (split equally = $26.67 each)
├── Groceries: ~$300 (shared groceries, split equally)
└── Cleaning supplies: ~$40 (split equally)

Settlement Pattern: Monthly on 1st of each month
```

**Implementation for Recurring Expenses:**
```kotlin
// IMPLEMENTATION: Recurring expense automation (RecurringExpenseScheduler.kt)
@Component
class RecurringExpenseScheduler @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val notificationService: NotificationService
) {
    
    // Process monthly recurring expenses for household scenario
    suspend fun processRecurringExpenses() {
        val rules = getActiveRecurringRules()
        
        rules.forEach { rule ->
            try {
                if (shouldCreateExpense(rule)) {
                    val expense = createRecurringExpense(rule)
                    val expenseId = expenseRepository.addExpense(expense)
                    
                    // Notify group members about new recurring expense
                    notificationService.notifyGroupMembers(
                        groupId = rule.groupId,
                        title = "Recurring Expense Added",
                        message = "${rule.description} - ${rule.amount}",
                        expenseId = expenseId
                    )
                    
                    // Update rule's last execution
                    updateRuleLastExecution(rule.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process recurring rule ${rule.id}", e)
                // Continue with other rules even if one fails
            }
        }
    }
    
    private suspend fun createRecurringExpense(rule: AdvancedRecurrenceRule): Expense {
        return Expense(
            id = "",
            groupId = rule.groupId,
            description = rule.description,
            amount = rule.amount,
            currency = rule.currency,
            date = Timestamp.now(),
            paidBy = rule.defaultPayer ?: rule.createdBy,
            paidByName = getUserDisplayName(rule.defaultPayer ?: rule.createdBy),
            splitBetween = rule.defaultSplits,
            category = rule.category,
            isRecurring = true,
            recurrenceId = rule.id
        )
    }
}
```

## 2. EDGE CASES & ERROR SCENARIOS

### **Financial Edge Cases**

#### **Edge Case 1: Precision & Rounding Issues**
```kotlin
// PROBLEM: Floating point precision in split calculations
// Example: $10.00 split 3 ways = $3.33, $3.33, $3.34 (total: $10.00)

class PrecisionSafeSplitCalculator {
    fun calculateEqualSplit(totalAmount: Double, memberCount: Int): List<Double> {
        // Use BigDecimal for precise financial calculations
        val total = BigDecimal.valueOf(totalAmount)
        val count = BigDecimal.valueOf(memberCount.toLong())
        val baseAmount = total.divide(count, 2, RoundingMode.DOWN)
        val remainder = total.remainder(count.multiply(baseAmount))
        
        val splits = mutableListOf<Double>()
        
        // Distribute base amount to all members
        repeat(memberCount) { splits.add(baseAmount.toDouble()) }
        
        // Distribute remainder cents to first members
        val remainderCents = remainder.multiply(BigDecimal(100)).toInt()
        for (i in 0 until remainderCents) {
            splits[i] = (BigDecimal.valueOf(splits[i]).add(BigDecimal.valueOf(0.01))).toDouble()
        }
        
        return splits
    }
}

// TESTING: Edge case validation
@Test
fun `split calculations should maintain precision`() {
    val calculator = PrecisionSafeSplitCalculator()
    
    // Test edge case: $10.00 split 3 ways
    val splits = calculator.calculateEqualSplit(10.00, 3)
    
    assertThat(splits).containsExactly(3.34, 3.33, 3.33)
    assertThat(splits.sum()).isEqualTo(10.00)
    
    // Test edge case: $0.01 split 2 ways
    val miniSplits = calculator.calculateEqualSplit(0.01, 2)
    assertThat(miniSplits).containsExactly(0.01, 0.00)
}
```

#### **Edge Case 2: Currency Conversion & Multi-Currency Groups**
```kotlin
// SCENARIO: International trip with multiple currencies
class MultiCurrencyExpenseService {
    
    suspend fun addMultiCurrencyExpense(
        groupId: String,
        amount: Double,
        currency: String,
        description: String
    ): Result<String> {
        try {
            val group = groupService.getGroupById(groupId)
            val groupBaseCurrency = group?.currency ?: "USD"
            
            // Convert to group's base currency if different
            val convertedAmount = if (currency != groupBaseCurrency) {
                currencyService.convert(amount, currency, groupBaseCurrency)
            } else {
                amount
            }
            
            val expense = Expense(
                // ... other fields
                amount = convertedAmount,
                currency = groupBaseCurrency,
                originalAmount = amount,
                originalCurrency = currency,
                exchangeRate = if (currency != groupBaseCurrency) {
                    currencyService.getExchangeRate(currency, groupBaseCurrency)
                } else 1.0
            )
            
            return Result.success(expenseRepository.addExpense(expense))
            
        } catch (e: CurrencyConversionException) {
            return Result.failure(e)
        }
    }
}
```

### **Network & Offline Edge Cases**

#### **Edge Case 3: Offline Expense Creation**
```kotlin
// SCENARIO: User creates expenses without internet connection
class OfflineExpenseHandler {
    
    suspend fun createOfflineExpense(expense: Expense): String {
        // Generate local ID for offline expense
        val offlineId = "offline_${System.currentTimeMillis()}_${Random.nextInt()}"
        
        // Store in local database with sync flag
        val offlineExpense = expense.copy(
            id = offlineId,
            isSynced = false,
            createdAt = Timestamp.now(),
            syncStatus = SyncStatus.PENDING
        )
        
        localDatabase.insertExpense(offlineExpense)
        
        // Schedule sync when network becomes available
        syncManager.scheduleSync(offlineId)
        
        return offlineId
    }
    
    suspend fun syncPendingExpenses() {
        val pendingExpenses = localDatabase.getPendingExpenses()
        
        pendingExpenses.forEach { expense ->
            try {
                // Upload to Firestore
                val remoteId = expenseRepository.addExpense(expense.copy(id = ""))
                
                // Update local record with remote ID
                localDatabase.updateExpenseId(expense.id, remoteId)
                localDatabase.markAsSynced(expense.id)
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync expense ${expense.id}", e)
                // Retry later
                syncManager.scheduleRetry(expense.id)
            }
        }
    }
}
```

#### **Edge Case 4: Concurrent Modifications**
```kotlin
// SCENARIO: Multiple users editing the same expense simultaneously
class ConcurrentModificationHandler {
    
    suspend fun updateExpenseWithConflictResolution(
        expenseId: String,
        updates: Map<String, Any>,
        expectedVersion: Int
    ): Result<Unit> {
        return try {
            firestore.runTransaction { transaction ->
                val expenseRef = firestore.collection("expenses").document(expenseId)
                val currentExpense = transaction.get(expenseRef).toObject<Expense>()
                
                if (currentExpense == null) {
                    throw Exception("Expense not found")
                }
                
                // Check for concurrent modifications
                if (currentExpense.version != expectedVersion) {
                    throw ConcurrentModificationException(
                        "Expense was modified by another user. Please refresh and try again.",
                        currentVersion = currentExpense.version,
                        expectedVersion = expectedVersion
                    )
                }
                
                // Apply updates with version increment
                val updatedData = updates.toMutableMap().apply {
                    put("version", currentExpense.version + 1)
                    put("updatedAt", FieldValue.serverTimestamp())
                    put("modifiedBy", auth.currentUser?.uid)
                }
                
                transaction.update(expenseRef, updatedData)
            }.await()
            
            Result.success(Unit)
            
        } catch (e: ConcurrentModificationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update expense: ${e.message}"))
        }
    }
}

class ConcurrentModificationException(
    message: String,
    val currentVersion: Int,
    val expectedVersion: Int
) : Exception(message)
```

### **User Behavior Edge Cases**

#### **Edge Case 5: Group Membership Changes During Active Expenses**
```kotlin
// SCENARIO: User leaves group while having unsettled expenses
class GroupMembershipHandler {
    
    suspend fun removeUserFromGroup(groupId: String, userId: String): Result<Unit> {
        return try {
            // Check for unsettled expenses
            val unsettledExpenses = expenseRepository.getUnsettledExpensesByUser(groupId, userId)
            
            if (unsettledExpenses.isNotEmpty()) {
                // Calculate user's current balance
                val balance = settlementService.getUserBalance(groupId, userId)
                
                return if (balance != 0.0) {
                    // User has debts or credits - cannot leave
                    Result.failure(Exception(
                        "Cannot leave group with unsettled balance of ${balance}. " +
                        "Please settle your debts first."
                    ))
                } else {
                    // Balance is zero - safe to remove
                    performUserRemoval(groupId, userId)
                }
            }
            
            // No unsettled expenses - safe to remove
            performUserRemoval(groupId, userId)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun performUserRemoval(groupId: String, userId: String): Result<Unit> {
        return firestore.runTransaction { transaction ->
            val groupRef = firestore.collection("groups").document(groupId)
            val group = transaction.get(groupRef).toObject<Group>()
                ?: throw Exception("Group not found")
            
            // Remove user from members list
            val updatedMembers = group.members.filterKeys { it != userId }
            val updatedMemberIds = group.memberIds.filter { it != userId }
            
            transaction.update(groupRef, mapOf(
                "members" to updatedMembers,
                "memberIds" to updatedMemberIds,
                "memberCount" to updatedMemberIds.size,
                "lastActivity" to FieldValue.serverTimestamp()
            ))
            
            // Log activity
            val activityRef = firestore.collection("groupActivities").document()
            transaction.set(activityRef, mapOf(
                "type" to "MEMBER_LEFT",
                "groupId" to groupId,
                "userId" to userId,
                "timestamp" to FieldValue.serverTimestamp()
            ))
            
        }.await().let { Result.success(Unit) }
    }
}
```

## 3. STRESS TESTING SCENARIOS

### **High-Volume Data Scenarios**
```kotlin
// SCENARIO: Large group with many expenses (company retreat)
// Group: 50 members, 200+ expenses over 1 week

class LargeGroupPerformanceTest {
    
    @Test
    fun `app should handle large group with many expenses`() = runTest {
        // Create test group with 50 members
        val members = (1..50).map { createTestUser("user$it") }
        val group = createTestGroup(members)
        
        // Create 200 expenses with random splits
        val expenses = (1..200).map { i ->
            createRandomExpense(
                groupId = group.id,
                amount = Random.nextDouble(10.0, 500.0),
                paidBy = members.random().uid,
                splitBetween = members.shuffled().take(Random.nextInt(3, 15))
            )
        }
        
        // Measure settlement calculation performance
        val startTime = System.currentTimeMillis()
        val settlements = settlementService.calculateSettlements(group.id)
        val calculationTime = System.currentTimeMillis() - startTime
        
        // Performance assertions
        assertThat(calculationTime).isLessThan(5000) // 5 seconds max
        assertThat(settlements.size).isGreaterThan(0)
        
        // Validate settlement accuracy
        val totalDebts = settlements.sumOf { it.amount }
        val totalCredits = settlements.groupBy { it.from }.values.sumOf { it.sumOf { it.amount } }
        assertThat(totalDebts).isEqualTo(totalCredits)
    }
}
```

### **Long-Running Session Scenarios**
```kotlin
// SCENARIO: User keeps app open for extended period with continuous usage
class LongRunningSessionTest {
    
    @Test
    fun `app should maintain performance during extended usage`() = runTest {
        val initialMemory = getMemoryUsage()
        
        // Simulate 4 hours of continuous usage
        repeat(240) { minute ->
            // Simulate user actions every minute
            when (minute % 5) {
                0 -> createRandomExpense()
                1 -> loadGroupExpenses()
                2 -> updateExpenseDetails()
                3 -> calculateSettlements()
                4 -> loadGroupActivity()
            }
            
            delay(1000) // Simulate 1 minute (compressed to 1 second)
            
            // Check memory usage every 30 minutes
            if (minute % 30 == 0) {
                val currentMemory = getMemoryUsage()
                val memoryGrowth = currentMemory - initialMemory
                
                // Memory growth should be limited
                assertThat(memoryGrowth).isLessThan(100 * 1024 * 1024) // 100MB max
                
                // Trigger cleanup if needed
                if (memoryGrowth > 50 * 1024 * 1024) {
                    performanceOptimizer.cleanup()
                }
            }
        }
    }
}
```

## 4. ACCESSIBILITY & USABILITY EDGE CASES

### **Accessibility Scenarios**
```kotlin
// SCENARIO: User with visual impairment using TalkBack
class AccessibilityTestScenarios {
    
    // TalkBack content descriptions for financial data
    @Composable
    fun ExpenseItem(expense: Expense) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = "Expense: ${expense.description}, " +
                        "Amount: ${expense.amount} ${expense.currency}, " +
                        "Paid by: ${expense.paidByName}, " +
                        "Date: ${formatDate(expense.date)}"
                }
        ) {
            // UI content
        }
    }
    
    // Accessible form validation feedback
    @Composable
    fun AmountInputField(
        value: String,
        onValueChange: (String) -> Unit,
        isError: Boolean,
        errorMessage: String?
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Amount") },
            isError = isError,
            modifier = Modifier.semantics {
                if (isError && errorMessage != null) {
                    error(errorMessage)
                }
            }
        )
    }
}
```

### **Internationalization Edge Cases**
```kotlin
// SCENARIO: App usage in different locales and languages
class InternationalizationHandler {
    
    fun formatCurrency(amount: Double, currency: String, locale: Locale): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(locale)
            formatter.currency = Currency.getInstance(currency)
            formatter.format(amount)
        } catch (e: IllegalArgumentException) {
            // Fallback for unsupported currencies
            "$amount $currency"
        }
    }
    
    fun formatDate(timestamp: Timestamp, locale: Locale): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale)
        return dateFormat.format(timestamp.toDate())
    }
    
    // Handle right-to-left languages
    @Composable
    fun LocalizedLayout(content: @Composable () -> Unit) {
        val layoutDirection = when (Locale.getDefault().language) {
            "ar", "he", "fa" -> LayoutDirection.Rtl
            else -> LayoutDirection.Ltr
        }
        
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            content()
        }
    }
}
```

## 5. AI LEARNING OBJECTIVES FOR USER SCENARIOS

### **Real-World Usage Pattern Recognition**
- **User Journey Mapping**: End-to-end flow implementation with state management
- **Edge Case Handling**: Comprehensive error scenarios and recovery strategies
- **Financial Precision**: Currency calculation accuracy and rounding strategies
- **Offline Capability**: Data synchronization and conflict resolution patterns

### **Performance Under Load**
- **Scalability Testing**: Large group and high-volume expense handling
- **Memory Management**: Long-running session optimization strategies
- **Concurrent Access**: Multi-user modification conflict resolution
- **Network Resilience**: Offline operation and sync strategies

### **Accessibility & Inclusivity**
- **Screen Reader Support**: Semantic content descriptions for financial data
- **Internationalization**: Multi-language and multi-currency support
- **Error Communication**: Clear, actionable error messages for users
- **Progressive Disclosure**: Complex features with intuitive user flows

---

*AI Training Data for User Scenarios & Real-World Usage - Production User Behavior Analysis*
*Critical for understanding how real users interact with financial mobile applications* 