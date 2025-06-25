# Fairr Coding Standards

This document outlines the coding standards and best practices for the Fairr Android application.

## Kotlin Standards

### Naming Conventions
- **Classes**: PascalCase (`ExpenseRepository`)
- **Functions/Variables**: camelCase (`getUserExpenses()`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_GROUP_SIZE`)
- **Packages**: lowercase (`com.example.fairr.ui.screens`)

### Code Organization
```kotlin
// 1. Package declaration
// 2. Imports (organized by type)
// 3. Data classes and models
// 4. Sealed classes and enums
// 5. Main composable functions
// 6. Helper functions and extensions
```

### Code Organization

#### File Structure
```kotlin
// 1. Package declaration
package com.example.fairr.ui.screens.expenses

// 2. Imports (organized by type)
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// 3. Data classes and models
data class ExpenseState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false
)

// 4. Sealed classes and enums
sealed class ExpenseEvent {
    object LoadExpenses : ExpenseEvent()
    data class AddExpense(val expense: Expense) : ExpenseEvent()
}

// 5. Main composable functions
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    // Implementation
}

// 6. Helper functions and extensions
private fun formatCurrency(amount: Double): String {
    return "$%.2f".format(amount)
}
```

#### Function Length
- Keep functions under 50 lines when possible
- Break down complex functions into smaller, focused functions
- Use descriptive function names that explain their purpose

### Error Handling

#### Exception Handling
```kotlin
// ✅ Correct
try {
    val result = repository.getData()
    // Handle success
} catch (e: NetworkException) {
    // Handle network errors
    Log.e(TAG, "Network error", e)
    _state.value = _state.value.copy(error = "Network error")
} catch (e: Exception) {
    // Handle other errors
    Log.e(TAG, "Unexpected error", e)
    _state.value = _state.value.copy(error = "Something went wrong")
}

// ❌ Incorrect
try {
    val result = repository.getData()
} catch (e: Exception) {
    // Generic error handling
}
```

#### Result Types
```kotlin
// ✅ Correct
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}

fun getExpenses(): Result<List<Expense>> {
    return try {
        val expenses = repository.getExpenses()
        Result.Success(expenses)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

## Jetpack Compose Standards

### Composable Function Structure

#### Basic Structure
```kotlin
@Composable
fun ExpenseCard(
    expense: Expense,
    onExpenseClick: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onExpenseClick(expense) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formatCurrency(expense.amount),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

#### State Management
```kotlin
// ✅ Correct - Use remember and derivedStateOf
@Composable
fun ExpenseList(
    expenses: List<Expense>,
    searchQuery: String
) {
    val filteredExpenses by remember(expenses, searchQuery) {
        derivedStateOf {
            expenses.filter { it.description.contains(searchQuery, ignoreCase = true) }
        }
    }
    
    LazyColumn {
        items(filteredExpenses) { expense ->
            ExpenseCard(expense = expense)
        }
    }
}

// ❌ Incorrect - Don't use mutableStateOf unnecessarily
@Composable
fun ExpenseList(
    expenses: List<Expense>,
    searchQuery: String
) {
    var filteredExpenses by remember { mutableStateOf(emptyList<Expense>()) }
    
    LaunchedEffect(expenses, searchQuery) {
        filteredExpenses = expenses.filter { 
            it.description.contains(searchQuery, ignoreCase = true) 
        }
    }
}
```

### Modifier Usage

#### Modifier Order
```kotlin
// ✅ Correct order
Modifier
    .fillMaxWidth()
    .padding(16.dp)
    .background(Color.Gray)
    .clickable { /* action */ }

// ❌ Incorrect order
Modifier
    .clickable { /* action */ }
    .background(Color.Gray)
    .padding(16.dp)
    .fillMaxWidth()
```

#### Reusable Modifiers
```kotlin
// ✅ Correct - Create reusable modifiers
val cardModifier = Modifier
    .fillMaxWidth()
    .padding(8.dp)
    .background(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp)
    )

@Composable
fun ExpenseCard(expense: Expense) {
    Card(
        modifier = cardModifier
    ) {
        // Content
    }
}
```

## Architecture Standards

### MVVM Pattern

#### ViewModel Structure
```kotlin
@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    // State
    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    // Events
    private val _events = MutableSharedFlow<ExpenseEvent>()
    val events: SharedFlow<ExpenseEvent> = _events.asSharedFlow()

    // Actions
    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val expenses = expenseRepository.getExpenses()
                _uiState.value = _uiState.value.copy(
                    expenses = expenses,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}
```

#### State Management
```kotlin
// ✅ Correct - Use sealed classes for state
sealed class ExpenseUiState {
    object Loading : ExpenseUiState()
    data class Success(
        val expenses: List<Expense>,
        val totalAmount: Double
    ) : ExpenseUiState()
    data class Error(val message: String) : ExpenseUiState()
}

// ❌ Incorrect - Don't use nullable state
data class ExpenseUiState(
    val expenses: List<Expense>? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Repository Pattern

#### Repository Interface
```kotlin
interface ExpenseRepository {
    suspend fun getExpenses(): List<Expense>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun updateExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
}
```

#### Repository Implementation
```kotlin
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExpenseRepository {

    override suspend fun getExpenses(): List<Expense> {
        return try {
            val snapshot = firestore.collection("expenses")
                .whereEqualTo("userId", auth.currentUser?.uid)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Expense::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expenses", e)
            emptyList()
        }
    }
}
```

## Testing Standards

### Unit Tests

#### ViewModel Testing
```kotlin
@RunWith(MockitoJUnitRunner::class)
class ExpenseViewModelTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var expenseRepository: ExpenseRepository
    
    private lateinit var viewModel: ExpenseViewModel
    
    @Before
    fun setup() {
        viewModel = ExpenseViewModel(expenseRepository)
    }
    
    @Test
    fun `loadExpenses should update state with expenses`() = runTest {
        // Given
        val expenses = listOf(Expense(id = "1", description = "Test"))
        whenever(expenseRepository.getExpenses()).thenReturn(expenses)
        
        // When
        viewModel.loadExpenses()
        
        // Then
        val state = viewModel.uiState.value
        assertThat(state.expenses).isEqualTo(expenses)
        assertThat(state.isLoading).isFalse()
    }
}
```

#### Repository Testing
```kotlin
@RunWith(MockitoJUnitRunner::class)
class ExpenseRepositoryTest {
    
    @Mock
    private lateinit var firestore: FirebaseFirestore
    
    @Mock
    private lateinit var auth: FirebaseAuth
    
    @Mock
    private lateinit var currentUser: FirebaseUser
    
    private lateinit var repository: ExpenseRepositoryImpl
    
    @Before
    fun setup() {
        whenever(auth.currentUser).thenReturn(currentUser)
        whenever(currentUser.uid).thenReturn("test-user-id")
        repository = ExpenseRepositoryImpl(firestore, auth)
    }
}
```

### UI Tests

#### Composable Testing
```kotlin
@RunWith(AndroidJUnit4::class)
class ExpenseScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun expenseScreen_displaysExpenses() {
        // Given
        val expenses = listOf(
            Expense(id = "1", description = "Lunch", amount = 25.0)
        )
        
        // When
        composeTestRule.setContent {
            ExpenseScreen(expenses = expenses)
        }
        
        // Then
        composeTestRule.onNodeWithText("Lunch").assertIsDisplayed()
        composeTestRule.onNodeWithText("$25.00").assertIsDisplayed()
    }
}
```

## Performance Standards

### Memory Management

#### Image Loading
```kotlin
// ✅ Correct - Use Coil for image loading
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(expense.receiptUrl)
        .crossfade(true)
        .build(),
    contentDescription = "Receipt",
    modifier = Modifier.size(100.dp),
    contentScale = ContentScale.Crop
)

// ❌ Incorrect - Don't load images without optimization
Image(
    painter = painterResource(id = R.drawable.receipt),
    contentDescription = "Receipt",
    modifier = Modifier.size(100.dp)
)
```

#### Lazy Loading
```kotlin
// ✅ Correct - Use LazyColumn for large lists
LazyColumn {
    items(expenses) { expense ->
        ExpenseCard(expense = expense)
    }
}

// ❌ Incorrect - Don't use Column for large lists
Column {
    expenses.forEach { expense ->
        ExpenseCard(expense = expense)
    }
}
```

### Network Optimization

#### Caching
```kotlin
// ✅ Correct - Implement caching
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val expenseCache: ExpenseCache
) : ExpenseRepository {

    override suspend fun getExpenses(): List<Expense> {
        // Try cache first
        val cachedExpenses = expenseCache.getExpenses()
        if (cachedExpenses.isNotEmpty()) {
            return cachedExpenses
        }
        
        // Fetch from network
        val networkExpenses = fetchFromFirestore()
        expenseCache.saveExpenses(networkExpenses)
        return networkExpenses
    }
}
```

## Security Standards

### Input Validation
```kotlin
// ✅ Correct - Validate all inputs
fun validateExpense(expense: Expense): ValidationResult {
    return when {
        expense.description.isBlank() -> 
            ValidationResult.Error("Description cannot be empty")
        expense.amount <= 0 -> 
            ValidationResult.Error("Amount must be positive")
        expense.amount > MAX_EXPENSE_AMOUNT -> 
            ValidationResult.Error("Amount exceeds maximum")
        else -> ValidationResult.Success
    }
}

// ❌ Incorrect - Don't trust user input
fun saveExpense(expense: Expense) {
    // Directly save without validation
    repository.saveExpense(expense)
}
```

### Data Sanitization
```kotlin
// ✅ Correct - Sanitize data before storage
fun sanitizeExpenseDescription(description: String): String {
    return description
        .trim()
        .replace(Regex("[<>\"']"), "") // Remove potential HTML/script tags
        .take(MAX_DESCRIPTION_LENGTH)
}
```

## Documentation Standards

### Code Comments
```kotlin
/**
 * Calculates the optimal settlement plan for a group of expenses.
 * 
 * This function uses a greedy algorithm to minimize the number of transactions
 * needed to settle all debts in the group.
 * 
 * @param expenses List of expenses to settle
 * @param members List of group members
 * @return List of settlement transactions
 * 
 * @throws IllegalArgumentException if expenses or members list is empty
 */
fun calculateSettlements(
    expenses: List<Expense>,
    members: List<GroupMember>
): List<Settlement> {
    require(expenses.isNotEmpty()) { "Expenses list cannot be empty" }
    require(members.isNotEmpty()) { "Members list cannot be empty" }
    
    // Implementation...
}
```

### README Files
Each major component should have a README file explaining:
- Purpose and responsibility
- Usage examples
- Dependencies
- Testing approach

---

*Last updated: December 2024*
*Maintained by: Development Team* 