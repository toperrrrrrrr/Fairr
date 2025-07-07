# AI TRAINING DATA: Data Flow & State Management - Fairr Android App

## 1. FILE-TO-STATE-PATTERN MAPPING

### **StateFlow Architecture**
```
ui/viewmodels/StartupViewModel.kt → Complex multi-state management with session validation
ui/screens/home/HomeViewModel.kt → Simple StateFlow with data aggregation
ui/screens/groups/GroupDetailViewModel.kt → Advanced state combination patterns
data/auth/AuthService.kt → Service-level StateFlow for authentication state
data/preferences/UserPreferencesManager.kt → DataStore reactive flows
→ Pattern: Layered state management with reactive data streams
```

### **Data Flow Layers**
```
Firebase/Remote → Services → Repositories → ViewModels → UI State → Compose
data/auth/AuthService.kt → Firebase Auth listener → StateFlow
data/groups/GroupService.kt → Firestore real-time → CallbackFlow
data/repository/ExpenseRepository.kt → Data access → suspend functions
ui/viewmodels/*ViewModel.kt → Business logic → MutableStateFlow
ui/screens/*Screen.kt → UI consumption → collectAsState()
→ Pattern: Unidirectional data flow with reactive streams
```

## 2. STATE MANAGEMENT PATTERN TRAINING DATA

### **Complex State Management (`ui/viewmodels/StartupViewModel.kt`)**
```kotlin
@HiltViewModel
class StartupViewModel @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val authService: AuthService
) : ViewModel() {

    // Multiple state streams for different concerns
    private val _isOnboardingCompleted = MutableStateFlow(false)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _authLoading = MutableStateFlow(true)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _startupState = MutableStateFlow<StartupState>(StartupState.Loading)
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    init {
        validateSessionOnStartup()
        observeAuthState()
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authService.authState.collect { authState ->
                when (authState) {
                    is AuthState.Loading -> {
                        _authLoading.value = true
                    }
                    is AuthState.Authenticated -> {
                        _isAuthenticated.value = true
                        _authLoading.value = false
                        userPreferencesManager.saveAuthState(authState.user)
                        
                        if (_isOnboardingCompleted.value) {
                            _startupState.value = StartupState.Main
                        }
                    }
                    is AuthState.Unauthenticated -> {
                        _isAuthenticated.value = false
                        _authLoading.value = false
                        userPreferencesManager.clearAuthState()
                        
                        _startupState.value = if (_isOnboardingCompleted.value) {
                            StartupState.Authentication
                        } else {
                            StartupState.Onboarding
                        }
                    }
                    is AuthState.Error -> {
                        _authError.value = authState.message
                        _authLoading.value = false
                        _isAuthenticated.value = false
                        userPreferencesManager.clearAuthState()
                    }
                }
            }
        }
    }
}
```
**AI Learning Points:**
- Multiple StateFlow streams for different UI concerns
- Reactive state updates based on external service state changes
- Complex conditional state logic based on multiple factors
- Proper state synchronization between local and remote states
- Comprehensive error handling with state transitions

### **State Combination Patterns (`ui/screens/groups/GroupDetailViewModel.kt`)**
```kotlin
@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseRepository: ExpenseRepository,
    private val settlementService: SettlementService,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState: GroupDetailUiState by mutableStateOf(GroupDetailUiState.Loading)
        private set

    private fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = GroupDetailUiState.Loading
            try {
                groupService.getGroupById(groupId)
                    .combine(expenseRepository.getExpensesByGroupIdFlow(groupId)) { group, expenses ->
                        val uiMembers = group.members.map { convertToUiMember(it) }
                        val totalExpenses = expenses.sumOf { it.amount }
                        val activities = generateActivities(group, expenses)

                        val summary = settlementService.getSettlementSummary(groupId)
                        val currentUserId = auth.currentUser?.uid
                        val currentUserBalance = summary.firstOrNull { it.userId == currentUserId }?.netBalance ?: 0.0

                        GroupDetailUiState.Success(
                            group = group,
                            members = uiMembers,
                            currentUserBalance = currentUserBalance,
                            totalExpenses = totalExpenses,
                            expenses = expenses,
                            activities = activities
                        )
                    }
                    .catch { e ->
                        Log.e(TAG, "Error loading group details", e)
                        uiState = GroupDetailUiState.Error(e.message ?: "Failed to load group details")
                    }
                    .collect { state ->
                        uiState = state
                    }
            } catch (e: Exception) {
                uiState = GroupDetailUiState.Error(e.message ?: "Failed to load group details")
            }
        }
    }
}
```
**AI Learning Points:**
- Flow combination for multiple data sources with `.combine()`
- Sealed interface pattern for exhaustive state representation
- Real-time data aggregation and transformation
- Error handling with `.catch()` operator
- Business logic composition within state transformation

### **Service-Level State Management (`data/auth/AuthService.kt`)**
```kotlin
@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _authState.value = if (user != null) {
            Log.d(TAG, "User authenticated: ${user.email}")
            AuthState.Authenticated(user)
        } else {
            Log.d(TAG, "User unauthenticated")
            AuthState.Unauthenticated
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
        val currentUser = auth.currentUser
        _authState.value = if (currentUser != null) {
            AuthState.Authenticated(currentUser)
        } else {
            AuthState.Unauthenticated
        }
    }

    suspend fun isUserAuthenticated(): Boolean {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.getIdToken(true).await()  // Force token refresh to validate session
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Session validation failed", e)
            false
        }
    }
}
```
**AI Learning Points:**
- Service-level StateFlow for cross-cutting concerns
- Firebase listener integration with StateFlow
- Proper lifecycle management with init block setup
- Session validation with token refresh patterns
- Consistent logging for debugging state transitions

## 3. LOCAL DATA SYNCHRONIZATION PATTERNS

### **DataStore Integration (`data/preferences/UserPreferencesManager.kt`)**
```kotlin
@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val AUTH_USER_ID = stringPreferencesKey("auth_user_id")
        val AUTH_SESSION_TIMESTAMP = longPreferencesKey("auth_session_timestamp")
        val AUTH_IS_AUTHENTICATED = booleanPreferencesKey("auth_is_authenticated")
    }
    
    val authState: Flow<AuthState> = context.dataStore.data
        .map { preferences ->
            AuthState(
                userId = preferences[PreferencesKeys.AUTH_USER_ID],
                userEmail = preferences[PreferencesKeys.AUTH_USER_EMAIL],
                sessionTimestamp = preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L,
                isAuthenticated = preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] ?: false
            )
        }

    suspend fun saveAuthState(user: FirebaseUser) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_USER_ID] = user.uid
            preferences[PreferencesKeys.AUTH_USER_EMAIL] = user.email ?: ""
            preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] = System.currentTimeMillis()
            preferences[PreferencesKeys.AUTH_IS_AUTHENTICATED] = true
        }
    }
    
    suspend fun isSessionValid(sessionTimeoutDays: Long = 7): Boolean {
        val sessionTimestamp = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L
        }.first()
        
        val currentTime = System.currentTimeMillis()
        val sessionTimeoutMillis = TimeUnit.DAYS.toMillis(sessionTimeoutDays)
        
        return (currentTime - sessionTimestamp) < sessionTimeoutMillis
    }
}
```
**AI Learning Points:**
- DataStore Preferences for type-safe local storage
- Reactive Flow transformations for local data
- Atomic write operations with `edit` transactions
- Session management with timestamp validation
- Consistent key naming with PreferencesKeys object

### **Real-time Data Synchronization (`data/groups/GroupService.kt`)**
```kotlin
fun getUserGroups(): Flow<List<Group>> = callbackFlow {
    val currentUser = auth.currentUser
        ?: throw IllegalStateException("User not authenticated")

    val subscription = groupsCollection
        .whereArrayContains("memberIds", currentUser.uid)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error fetching groups", error)
                close(error)
                return@addSnapshotListener
            }

            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            val groups = snapshot.documents.mapNotNull { doc ->
                try {
                    parseGroupDocument(doc)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing group document", e)
                    null
                }
            }
            trySend(groups)
        }

    awaitClose { subscription.remove() }
}
```
**AI Learning Points:**
- CallbackFlow for Firebase real-time listeners
- Proper resource cleanup with `awaitClose`
- Error handling within listener callbacks
- Null safety patterns for Firebase data
- Authentication-aware query filtering

## 4. UI STATE CONSUMPTION PATTERNS

### **Compose State Collection (`MainActivity.kt`)**
```kotlin
@Composable
fun MainActivity() {
    val isDarkMode by settingsDataStore.darkModeEnabled.collectAsState(initial = false)
    val startupState by startupViewModel.startupState.collectAsState()
    val isAuthenticated by startupViewModel.isAuthenticated.collectAsState()
    val authLoading by startupViewModel.authLoading.collectAsState()
    val authError by startupViewModel.authError.collectAsState()

    FairrTheme(darkTheme = isDarkMode) {
        when (startupState) {
            StartupState.Loading -> SplashScreen(
                startupState = startupState,
                authLoading = authLoading,
                authError = authError,
                onRetry = { startupViewModel.handleAuthStateChange() },
                onClearError = { /* Handle error clearing */ }
            )
            StartupState.Onboarding -> OnboardingScreen(/* ... */)
            StartupState.Authentication -> WelcomeScreen(/* ... */)
            StartupState.Main -> FairrNavGraph(/* ... */)
        }
    }
}
```
**AI Learning Points:**
- Multiple state collection with `collectAsState()`
- Default values for initial state presentation
- Conditional UI rendering based on state
- State-driven navigation and screen transitions
- Proper state observation in Compose lifecycle

### **Advanced State Collection (`ui/screens/auth/WelcomeScreen.kt`)**
```kotlin
@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {} // Handle other events if needed
            }
        }
    }

    // UI implementation with state-driven behavior
    Button(
        onClick = onNavigateToLogin,
        enabled = !state.isLoading
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Text("Sign In")
        }
    }
}
```
**AI Learning Points:**
- Lifecycle-aware state collection with `collectAsStateWithLifecycle()`
- Event-driven UI updates with LaunchedEffect
- State-based UI element enabling/disabling
- Conditional UI rendering within components
- Separation of state and event streams

## 5. DATA TRANSFORMATION & AGGREGATION PATTERNS

### **State Transformation Pipeline**
```kotlin
// Data aggregation in HomeViewModel
groupService.getUserGroups()
    .catch { e ->
        _state.value = _state.value.copy(
            isLoading = false,
            error = e.message ?: "An error occurred while loading groups"
        )
    }
    .collect { groups ->
        var totalBalance = 0.0
        var totalExpenses = 0.0
        val recentExpenses = mutableListOf<Expense>()

        // Load expenses for each group
        groups.forEach { group ->
            try {
                val expenses = expenseRepository.getExpensesByGroupId(group.id)
                recentExpenses.addAll(expenses)

                // Calculate group totals
                expenses.forEach { expense ->
                    totalExpenses += expense.amount
                    if (expense.paidBy == auth.currentUser?.uid) {
                        totalBalance += expense.amount
                    }
                    totalBalance -= expense.amount / expense.splitBetween.size
                }
            } catch (e: Exception) {
                // Log error but continue processing other groups
                _state.value = _state.value.copy(
                    error = "Error loading expenses for some groups"
                )
            }
        }

        _state.value = _state.value.copy(
            isLoading = false,
            groups = groups,
            activeGroups = groups.size,
            totalBalance = totalBalance,
            totalExpenses = totalExpenses,
            recentExpenses = recentExpenses.sortedByDescending { it.date }.take(5)
        )
    }
```
**AI Learning Points:**
- Complex data aggregation within state updates
- Error handling for partial failures
- Business logic calculations within data transformation
- Data sorting and limiting for UI optimization
- State copying patterns for immutable updates

### **Activity Generation Pattern**
```kotlin
private fun generateActivities(group: Group, expenses: List<Expense>): List<GroupActivity> {
    val activities = mutableListOf<GroupActivity>()
    
    // Add group creation activity
    activities.add(
        GroupActivity(
            id = "group_created_${group.id}",
            type = ActivityType.GROUP_CREATED,
            title = "Group Created",
            description = "${group.name} was created",
            timestamp = group.createdAt,
            userId = group.createdBy,
            userName = group.members.find { it.userId == group.createdBy }?.name ?: "Unknown"
        )
    )
    
    // Add expense activities
    expenses.forEach { expense ->
        activities.add(
            GroupActivity(
                id = "expense_added_${expense.id}",
                type = ActivityType.EXPENSE_ADDED,
                title = "Expense Added",
                description = "${expense.description} - ${expense.paidByName} paid ${expense.amount}",
                timestamp = expense.date,
                userId = expense.paidBy,
                userName = expense.paidByName,
                expenseId = expense.id,
                amount = expense.amount
            )
        )
    }
    
    // Sort by timestamp (newest first) and limit to recent activities
    return activities.sortedByDescending { it.timestamp.seconds }.take(20)
}
```
**AI Learning Points:**
- Data transformation for UI-specific representations
- Activity timeline generation from multiple data sources
- Timestamp-based sorting for chronological presentation
- Data limitation for performance optimization

## 6. ERROR HANDLING & RECOVERY PATTERNS

### **Multi-layer Error Handling**
```kotlin
// Repository Level
suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses = 
    withContext(Dispatchers.IO) {
        try {
            val snapshot = query.get().await()
            val expenses = parseExpensesOptimized(snapshot.documents)
            PaginatedExpenses(expenses, hasMore, lastDocument)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting paginated expenses", e)
            PaginatedExpenses(emptyList(), false, null)  // Graceful fallback
        }
    }

// ViewModel Level
private fun loadHomeData() {
    viewModelScope.launch {
        try {
            _state.value = _state.value.copy(isLoading = true, error = null)
            // Business logic...
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = e.message ?: "An unexpected error occurred"
            )
        }
    }
}

// Flow Level
groupService.getUserGroups()
    .catch { e ->
        _state.value = _state.value.copy(
            isLoading = false,
            error = e.message ?: "An error occurred while loading groups"
        )
    }
    .collect { groups -> /* ... */ }
```
**AI Learning Points:**
- Layered error handling strategies
- Graceful fallback values for failed operations
- User-friendly error message generation
- Error state management in UI state containers
- Partial failure handling in data aggregation

## 7. PERFORMANCE OPTIMIZATION PATTERNS

### **Efficient State Updates**
```kotlin
// Immutable state updates with copy
_state.value = _state.value.copy(
    isLoading = false,
    error = null,
    groups = newGroups
)

// Conditional state updates to avoid unnecessary recomposition
if (_state.value.groups != newGroups) {
    _state.value = _state.value.copy(groups = newGroups)
}

// Debounced state updates for frequent changes
private val searchQueryState = MutableStateFlow("")
val searchResults = searchQueryState
    .debounce(300) // Wait 300ms after user stops typing
    .distinctUntilChanged()
    .flatMapLatest { query ->
        if (query.isBlank()) {
            flowOf(emptyList())
        } else {
            searchRepository.searchExpenses(query)
        }
    }
```

### **Memory-Efficient Data Loading**
```kotlin
// Pagination to limit memory usage
suspend fun loadMoreExpenses(
    groupId: String, 
    lastDocument: DocumentSnapshot,
    pageSize: Int = DEFAULT_PAGE_SIZE
): PaginatedExpenses = 
    getPaginatedExpenses(
        ExpenseQueryParams(
            groupId = groupId,
            pageSize = pageSize,
            lastDocument = lastDocument
        )
    )

// Caching for frequently accessed data
private val userNameCache = mutableMapOf<String, String>()
```

## 8. AI PATTERN LEARNING OBJECTIVES

### **State Management Mastery**
- **Reactive Streams**: How to use StateFlow and Flow for reactive state management
- **State Combination**: Combining multiple data sources with `.combine()` operator
- **Local Synchronization**: DataStore integration for persistent local state
- **Complex State Logic**: Multi-conditional state transitions and validation

### **Data Flow Architecture**
- **Unidirectional Flow**: Data flows from services through repositories to UI
- **Real-time Synchronization**: Firebase listeners integrated with StateFlow
- **Error Propagation**: Error handling strategies across architectural layers
- **State Transformation**: Data aggregation and transformation patterns

### **Performance Patterns**
- **Efficient Updates**: Immutable state updates and conditional recomposition
- **Memory Management**: Pagination and caching strategies
- **Resource Cleanup**: Proper Flow and listener lifecycle management
- **Debouncing**: Optimizing frequent state updates

## 9. IMPLEMENTATION GUIDELINES FOR AI

### **Quality Indicators**
1. **Reactive Design**: State flows through reactive streams from data sources to UI
2. **Error Resilience**: Comprehensive error handling with graceful fallbacks
3. **Performance**: Efficient state updates and memory management
4. **Consistency**: Consistent state management patterns across features

### **Common Anti-Patterns to Avoid**
- Direct state mutation instead of immutable copies
- Missing error handling in state flows
- Memory leaks from unclosed listeners
- Blocking operations on main thread in state updates
- Inconsistent state representation across similar features

---

*AI Training Data for Data Flow & State Management - Generated from Fairr Android App Pass 6*
*File references verified and state management patterns documented* 