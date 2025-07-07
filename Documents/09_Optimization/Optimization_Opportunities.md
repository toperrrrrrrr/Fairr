# AI TRAINING DATA: Optimization Opportunities - Fairr Android App

## 1. EXISTING OPTIMIZATION INFRASTRUCTURE

### **Performance Optimizer Utility (`util/PerformanceOptimizer.kt`)**
```kotlin
/**
 * Performance optimization utility for Fairr app
 * Handles memory management, coroutine cleanup, and performance monitoring
 */
object PerformanceOptimizer {
    
    private const val TAG = "PerformanceOptimizer"
    
    // Memory tracking
    private val activeCoroutines = AtomicLong(0)
    private val activeListeners = ConcurrentHashMap<String, ListenerRegistration>()
    private val performanceMetrics = ConcurrentHashMap<String, Long>()
    
    /**
     * Creates a memory-safe coroutine scope for ViewModels
     */
    fun ViewModel.createOptimizedScope(): CoroutineScope = 
        viewModelScope + SupervisorJob() + Dispatchers.Main.immediate
    
    /**
     * Launches a coroutine with automatic cleanup and error handling
     */
    fun ViewModel.launchOptimized(
        context: CoroutineContext = Dispatchers.Main,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Job {
        activeCoroutines.incrementAndGet()
        
        return viewModelScope.launch(context, start) {
            try {
                block()
            } catch (e: CancellationException) {
                Log.d(TAG, "Coroutine cancelled gracefully")
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Error in optimized coroutine", e)
            } finally {
                activeCoroutines.decrementAndGet()
            }
        }
    }
    
    /**
     * Memory-safe cache implementation
     */
    class MemorySafeCache<K, V>(
        private val maxSize: Int = 100,
        private val evictionTimeMs: Long = 300_000 // 5 minutes
    ) {
        private val cache = ConcurrentHashMap<K, CacheEntry<V>>()
        private val accessTimes = ConcurrentHashMap<K, Long>()
        
        data class CacheEntry<V>(
            val value: V,
            val timestamp: Long = System.currentTimeMillis()
        )
        
        fun get(key: K): V? {
            val entry = cache[key] ?: return null
            
            // Check if entry has expired
            if (System.currentTimeMillis() - entry.timestamp > evictionTimeMs) {
                remove(key)
                return null
            }
            
            accessTimes[key] = System.currentTimeMillis()
            return entry.value
        }
        
        fun put(key: K, value: V) {
            // Evict old entries if cache is full
            if (cache.size >= maxSize) {
                evictOldest()
            }
            
            cache[key] = CacheEntry(value)
            accessTimes[key] = System.currentTimeMillis()
        }
        
        private fun evictOldest() {
            val oldestKey = accessTimes.minByOrNull { it.value }?.key
            oldestKey?.let { remove(it) }
        }
    }
}
```
**AI Learning Points:**
- Comprehensive performance monitoring and resource tracking
- Memory-safe caching with TTL and LRU eviction
- Coroutine lifecycle management with automatic cleanup
- Performance metrics collection for monitoring

## 2. REPOSITORY LAYER OPTIMIZATION OPPORTUNITIES

### **Current Inefficient Patterns (`data/repository/ExpenseRepository.kt`)**
```kotlin
// CURRENT: Unbounded cache without proper management
class ExpenseRepositoryImpl {
    private val userNameCache = mutableMapOf<String, String>()  // PROBLEM: No size limits
    
    private suspend fun parseExpensesOptimized(documents: List<DocumentSnapshot>): List<Expense> = 
        withContext(Dispatchers.IO) {
            // PROBLEM: Limited batch size due to Firestore constraints
            val unknownUserIds = userIds.filterNot { userNameCache.containsKey(it) }
            if (unknownUserIds.isNotEmpty()) {
                try {
                    val userDocs = firestore.collection("users")
                        .whereIn("__name__", unknownUserIds.take(10)) // PROBLEM: Only 10 at a time
                        .get()
                        .await()
                } catch (e: Exception) {
                    Log.e(TAG, "Error batch fetching user names", e)
                }
            }
        }
}
```

### **OPTIMIZATION 1: Advanced Caching Strategy**
```kotlin
// OPTIMIZED: Repository with memory-safe caching
class OptimizedExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val performanceOptimizer: PerformanceOptimizer
) : ExpenseRepository {

    // BETTER: Memory-safe cache with eviction policy
    private val userNameCache = PerformanceOptimizer.MemorySafeCache<String, String>(
        maxSize = 500,
        evictionTimeMs = TimeUnit.MINUTES.toMillis(30)
    )
    
    // BETTER: Result cache for expensive operations
    private val expenseAggregateCache = PerformanceOptimizer.MemorySafeCache<String, ExpenseAggregate>(
        maxSize = 100,
        evictionTimeMs = TimeUnit.MINUTES.toMillis(5)
    )
    
    // BETTER: Chunked batch loading with retry mechanism
    private suspend fun batchFetchUserNames(userIds: Set<String>): Map<String, String> = 
        performanceOptimizer.measurePerformance("batch_fetch_users") {
            val results = mutableMapOf<String, String>()
            val chunks = userIds.chunked(10) // Firestore limit
            
            chunks.forEachIndexed { index, chunk ->
                try {
                    val userDocs = firestore.collection("users")
                        .whereIn("__name__", chunk)
                        .get()
                        .await()
                    
                    userDocs.documents.forEach { doc ->
                        val displayName = doc.getString("displayName") ?: "Unknown User"
                        userNameCache.put(doc.id, displayName)
                        results[doc.id] = displayName
                    }
                    
                    // Rate limiting between chunks
                    if (index < chunks.size - 1) {
                        delay(50) // Prevent overwhelming Firestore
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user chunk $index", e)
                    // Continue with other chunks even if one fails
                }
            }
            
            results
        }
    
    // BETTER: Aggregated data caching
    suspend fun getGroupExpenseAggregate(groupId: String): ExpenseAggregate {
        // Check cache first
        expenseAggregateCache.get(groupId)?.let { return it }
        
        return performanceOptimizer.measurePerformance("expense_aggregate_$groupId") {
            val expenses = getExpensesByGroupId(groupId)
            val aggregate = ExpenseAggregate(
                totalAmount = expenses.sumOf { it.amount },
                expenseCount = expenses.size,
                uniquePayees = expenses.map { it.paidBy }.toSet().size,
                lastExpenseDate = expenses.maxByOrNull { it.date }?.date,
                categories = expenses.groupBy { it.category }.mapValues { it.value.size }
            )
            
            // Cache the result
            expenseAggregateCache.put(groupId, aggregate)
            aggregate
        }
    }
}

data class ExpenseAggregate(
    val totalAmount: Double,
    val expenseCount: Int,
    val uniquePayees: Int,
    val lastExpenseDate: Timestamp?,
    val categories: Map<ExpenseCategory, Int>
)
```

### **OPTIMIZATION 2: Reactive Data with Smart Invalidation**
```kotlin
// BETTER: Smart cache invalidation with reactive updates
class ReactiveExpenseRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExpenseRepository {

    private val cacheInvalidationTrigger = MutableSharedFlow<String>()
    
    // BETTER: Self-invalidating reactive aggregates
    fun getGroupExpenseAggregateFlow(groupId: String): Flow<ExpenseAggregate> = 
        combine(
            getExpensesByGroupIdFlow(groupId),
            cacheInvalidationTrigger.startWith(groupId)
        ) { expenses, _ ->
            ExpenseAggregate(
                totalAmount = expenses.sumOf { it.amount },
                expenseCount = expenses.size,
                uniquePayees = expenses.map { it.paidBy }.toSet().size,
                lastExpenseDate = expenses.maxByOrNull { it.date }?.date,
                categories = expenses.groupBy { it.category }.mapValues { it.value.size }
            )
        }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
    
    // BETTER: Optimized pagination with prefetching
    suspend fun getPaginatedExpensesOptimized(
        params: ExpenseQueryParams
    ): PaginatedExpenses = withContext(Dispatchers.IO) {
        val pageSize = params.pageSize.coerceAtMost(MAX_PAGE_SIZE)
        
        // Prefetch next page in background if we're getting close to the end
        val prefetchSize = if (params.prefetchNext) pageSize + 10 else pageSize
        
        var query = firestore.collection("expenses")
            .whereEqualTo("groupId", params.groupId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(prefetchSize.toLong())
        
        // Apply filters efficiently
        params.category?.let { category ->
            query = query.whereEqualTo("category", category.name)
        }
        
        params.dateRange?.let { (start, end) ->
            query = query.whereGreaterThanOrEqualTo("date", start)
                        .whereLessThanOrEqualTo("date", end)
        }
        
        params.lastDocument?.let { lastDoc ->
            query = query.startAfter(lastDoc)
        }
        
        val snapshot = query.get().await()
        val expenses = parseExpensesOptimized(snapshot.documents.take(pageSize))
        
        PaginatedExpenses(
            expenses = expenses,
            hasMore = snapshot.documents.size > pageSize,
            lastDocument = snapshot.documents.getOrNull(pageSize - 1),
            prefetchedNext = if (snapshot.documents.size > pageSize) {
                parseExpensesOptimized(snapshot.documents.drop(pageSize))
            } else emptyList()
        )
    }
}
```

## 3. UI LAYER OPTIMIZATION OPPORTUNITIES

### **Current Performance Issues**
```kotlin
// CURRENT: Heavy recomposition in UI (MainActivity.kt)
@Composable
fun MainActivity() {
    val isDarkMode by settingsDataStore.darkModeEnabled.collectAsState(initial = false)
    val startupState by startupViewModel.startupState.collectAsState()
    val isAuthenticated by startupViewModel.isAuthenticated.collectAsState()
    val authLoading by startupViewModel.authLoading.collectAsState()
    val authError by startupViewModel.authError.collectAsState()
    // PROBLEM: Multiple state collections cause frequent recomposition
}
```

### **OPTIMIZATION 3: UI State Combination and Memoization**
```kotlin
// BETTER: Combined UI state to reduce recomposition
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val startupViewModel: StartupViewModel
) : ViewModel() {

    // BETTER: Combined state to reduce recomposition
    val uiState: StateFlow<MainUiState> = combine(
        settingsDataStore.darkModeEnabled,
        startupViewModel.startupState,
        startupViewModel.isAuthenticated,
        startupViewModel.authLoading,
        startupViewModel.authError
    ) { darkMode, startupState, isAuthenticated, authLoading, authError ->
        MainUiState(
            isDarkMode = darkMode,
            startupState = startupState,
            isAuthenticated = isAuthenticated,
            authLoading = authLoading,
            authError = authError
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState()
    )
}

data class MainUiState(
    val isDarkMode: Boolean = false,
    val startupState: StartupState = StartupState.Loading,
    val isAuthenticated: Boolean = false,
    val authLoading: Boolean = true,
    val authError: String? = null
)

// BETTER: Single state collection in UI
@Composable
fun MainActivity(
    viewModel: MainActivityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    FairrTheme(darkTheme = uiState.isDarkMode) {
        when (uiState.startupState) {
            StartupState.Loading -> SplashScreen(
                authLoading = uiState.authLoading,
                authError = uiState.authError,
                onRetry = { /* Handle retry */ }
            )
            StartupState.Onboarding -> OnboardingScreen()
            StartupState.Authentication -> WelcomeScreen()
            StartupState.Main -> FairrNavGraph()
        }
    }
}
```

### **OPTIMIZATION 4: Lazy Loading and Pagination in UI**
```kotlin
// BETTER: Optimized list handling with lazy loading
@Composable
fun GroupDetailScreen(
    groupId: String,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    // BETTER: Pagination trigger based on scroll position
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .map { visibleItems ->
                val lastVisibleItemIndex = visibleItems.lastOrNull()?.index ?: 0
                val totalItems = listState.layoutInfo.totalItemsCount
                lastVisibleItemIndex >= totalItems - 5 // Load more when 5 items from end
            }
            .distinctUntilChanged()
            .collect { shouldLoadMore ->
                if (shouldLoadMore && uiState is GroupDetailUiState.Success && uiState.hasMoreExpenses) {
                    viewModel.loadMoreExpenses()
                }
            }
    }
    
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when (uiState) {
            is GroupDetailUiState.Success -> {
                items(
                    items = uiState.expenses,
                    key = { expense -> expense.id }
                ) { expense ->
                    // BETTER: Key-based recomposition optimization
                    ExpenseItem(
                        expense = expense,
                        modifier = Modifier.animateItemPlacement()
                    )
                }
                
                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
            // Other states...
        }
    }
}

// BETTER: Memoized expensive UI calculations
@Composable
fun ExpenseItem(
    expense: Expense,
    modifier: Modifier = Modifier
) {
    // BETTER: Expensive calculations memoized
    val formattedAmount by remember(expense.amount, expense.currency) {
        derivedStateOf {
            CurrencyFormatter.formatAmount(expense.amount, expense.currency)
        }
    }
    
    val splitSummary by remember(expense.splitBetween) {
        derivedStateOf {
            "Split between ${expense.splitBetween.size} people"
        }
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // UI implementation using memoized values
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = expense.description,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = splitSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
```

## 4. STATE MANAGEMENT OPTIMIZATION OPPORTUNITIES

### **OPTIMIZATION 5: Smart State Invalidation**
```kotlin
// BETTER: Smart state management with selective updates
@HiltViewModel
class OptimizedGroupDetailViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseRepository: ExpenseRepository,
    private val settlementService: SettlementService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    
    // BETTER: Granular state management
    private val _groupData = MutableStateFlow<Group?>(null)
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    private val _settlements = MutableStateFlow<List<Settlement>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    
    // BETTER: Combine only when necessary
    val uiState: StateFlow<GroupDetailUiState> = combine(
        _groupData,
        _expenses,
        _members,
        _settlements,
        _isLoading,
        _error
    ) { group, expenses, members, settlements, isLoading, error ->
        when {
            error != null -> GroupDetailUiState.Error(error)
            isLoading && group == null -> GroupDetailUiState.Loading
            group != null -> {
                val totalExpenses = expenses.sumOf { it.amount }
                val currentUserBalance = settlements.find { 
                    it.userId == auth.currentUser?.uid 
                }?.netBalance ?: 0.0
                
                GroupDetailUiState.Success(
                    group = group,
                    expenses = expenses,
                    members = members,
                    totalExpenses = totalExpenses,
                    currentUserBalance = currentUserBalance,
                    isLoadingMore = isLoading,
                    hasMoreExpenses = expenses.size >= DEFAULT_PAGE_SIZE
                )
            }
            else -> GroupDetailUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupDetailUiState.Loading
    )
    
    // BETTER: Independent data loading with targeted updates
    init {
        loadGroupData()
        loadExpenses()
        loadSettlements()
    }
    
    private fun loadGroupData() {
        viewModelScope.launch {
            groupService.getGroupById(groupId)
                .catch { e -> _error.value = "Failed to load group: ${e.message}" }
                .collect { group ->
                    _groupData.value = group
                    _members.value = group.members.map { convertToUiMember(it) }
                }
        }
    }
    
    private fun loadExpenses() {
        viewModelScope.launch {
            expenseRepository.getExpensesByGroupIdFlow(groupId)
                .catch { e -> _error.value = "Failed to load expenses: ${e.message}" }
                .collect { expenses ->
                    _expenses.value = expenses
                }
        }
    }
    
    fun loadMoreExpenses() {
        if (_isLoading.value) return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentExpenses = _expenses.value
                val lastDocument = currentExpenses.lastOrNull()?.let { 
                    // Get Firestore document for pagination cursor
                    firestore.collection("expenses").document(it.id).get().await()
                }
                
                val paginatedResult = expenseRepository.getPaginatedExpenses(
                    ExpenseQueryParams(
                        groupId = groupId,
                        lastDocument = lastDocument,
                        pageSize = DEFAULT_PAGE_SIZE
                    )
                )
                
                _expenses.value = currentExpenses + paginatedResult.expenses
            } catch (e: Exception) {
                _error.value = "Failed to load more expenses: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

## 5. MEMORY OPTIMIZATION OPPORTUNITIES

### **OPTIMIZATION 6: Resource Lifecycle Management**
```kotlin
// BETTER: Comprehensive resource management
@HiltViewModel
class ResourceOptimizedViewModel @Inject constructor(
    private val performanceOptimizer: PerformanceOptimizer,
    private val repository: Repository
) : ViewModel() {

    private val resources = mutableListOf<Closeable>()
    
    // BETTER: Automatic resource registration and cleanup
    fun <T : Closeable> T.registerResource(): T {
        resources.add(this)
        return this
    }
    
    // BETTER: Optimized data loading with cleanup
    fun loadData() {
        performanceOptimizer.launchOptimized {
            val listener = repository.observeChanges { data ->
                // Handle data updates
            }.registerResource()
            
            // Automatic cleanup when ViewModel is cleared
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Cleanup all registered resources
        resources.forEach { resource ->
            try {
                resource.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error closing resource", e)
            }
        }
        resources.clear()
        
        // Use performance optimizer cleanup
        performanceOptimizer.cleanup()
    }
}
```

### **OPTIMIZATION 7: Image Loading and Memory Management**
```kotlin
// BETTER: Optimized image loading with memory management
@Composable
fun ProfileImage(
    imageUrl: String?,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // BETTER: Memory-conscious image loading
    LaunchedEffect(imageUrl) {
        if (imageUrl != null) {
            isLoading = true
            error = null
            
            try {
                PerformanceOptimizer.optimizeImageLoading {
                    // Load image with proper memory management
                    loadImage(imageUrl)
                }
            } catch (e: Exception) {
                error = "Failed to load image"
            } finally {
                isLoading = false
            }
        }
    }
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> CircularProgressIndicator(
                modifier = Modifier.size(size / 2),
                strokeWidth = 2.dp
            )
            error != null -> Icon(
                Icons.Default.Error,
                contentDescription = "Error loading image",
                tint = MaterialTheme.colorScheme.error
            )
            else -> AsyncImage(
                model = imageUrl,
                contentDescription = "Profile image",
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.default_avatar),
                error = painterResource(R.drawable.default_avatar)
            )
        }
    }
}
```

## 6. FIREBASE INTEGRATION OPTIMIZATION

### **OPTIMIZATION 8: Batch Operations and Connection Pooling**
```kotlin
// BETTER: Optimized Firebase operations
class OptimizedFirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val performanceOptimizer: PerformanceOptimizer
) {

    // BETTER: Connection pooling for Firebase operations
    private val operationScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.IO + 
        CoroutineName("FirebaseOperations")
    )
    
    // BETTER: Batch operations with optimal size
    suspend fun batchUpdateExpenses(updates: List<ExpenseUpdate>) = 
        performanceOptimizer.optimizeBatchOperation(
            items = updates,
            batchSize = 500 // Firestore batch limit
        ) { batch ->
            val firestoreBatch = firestore.batch()
            
            batch.forEach { update ->
                val docRef = firestore.collection("expenses").document(update.expenseId)
                firestoreBatch.update(docRef, update.changes)
            }
            
            firestoreBatch.commit().await()
        }
    
    // BETTER: Optimized listener management
    fun createOptimizedListener(
        collection: String,
        query: (Query) -> Query,
        onData: (List<DocumentSnapshot>) -> Unit
    ): ListenerRegistration {
        val baseQuery = firestore.collection(collection)
        val finalQuery = query(baseQuery)
        
        return finalQuery.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Listener error for $collection", error)
                return@addSnapshotListener
            }
            
            snapshot?.documents?.let { documents ->
                // Process in background to avoid blocking main thread
                operationScope.launch {
                    onData(documents)
                }
            }
        }
    }
    
    // BETTER: Query optimization with indexes
    suspend fun getOptimizedExpenses(
        groupId: String,
        filters: ExpenseFilters
    ): List<Expense> = withContext(Dispatchers.IO) {
        var query: Query = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
        
        // Use composite indexes for complex queries
        filters.category?.let { category ->
            query = query.whereEqualTo("category", category.name)
        }
        
        filters.dateRange?.let { (start, end) ->
            query = query.whereGreaterThanOrEqualTo("date", start)
                        .whereLessThanOrEqualTo("date", end)
        }
        
        // Add ordering last for index optimization
        query = query.orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(50)
        
        val snapshot = query.get().await()
        parseExpenses(snapshot.documents)
    }
}
```

## 7. ANALYTICS AND MONITORING OPTIMIZATION

### **OPTIMIZATION 9: Performance Monitoring Integration**
```kotlin
// BETTER: Comprehensive performance monitoring
class PerformanceMonitoringService @Inject constructor(
    private val firebasePerformance: FirebasePerformance,
    private val analytics: FirebaseAnalytics
) {

    // BETTER: Automatic performance tracing
    suspend fun <T> traceOperation(
        operationName: String,
        attributes: Map<String, String> = emptyMap(),
        operation: suspend () -> T
    ): T {
        val trace = firebasePerformance.newTrace(operationName)
        attributes.forEach { (key, value) ->
            trace.putAttribute(key, value)
        }
        
        trace.start()
        return try {
            val result = operation()
            trace.putAttribute("success", "true")
            result
        } catch (e: Exception) {
            trace.putAttribute("success", "false")
            trace.putAttribute("error", e.javaClass.simpleName)
            throw e
        } finally {
            trace.stop()
        }
    }
    
    // BETTER: Memory usage monitoring
    fun trackMemoryUsage(context: String) {
        val runtime = Runtime.getRuntime()
        val memoryInfo = mapOf(
            "context" to context,
            "used_memory" to (runtime.totalMemory() - runtime.freeMemory()),
            "max_memory" to runtime.maxMemory(),
            "available_memory" to runtime.freeMemory()
        )
        
        analytics.logEvent("memory_usage", Bundle().apply {
            memoryInfo.forEach { (key, value) ->
                putLong(key, value as Long)
            }
        })
    }
    
    // BETTER: Network performance monitoring
    fun trackNetworkOperation(
        operation: String,
        duration: Long,
        success: Boolean,
        responseSize: Long = 0
    ) {
        analytics.logEvent("network_operation", Bundle().apply {
            putString("operation", operation)
            putLong("duration_ms", duration)
            putBoolean("success", success)
            putLong("response_size_bytes", responseSize)
        })
    }
}
```

## 8. AI PATTERN LEARNING OBJECTIVES

### **Performance Optimization Strategies**
- **Memory Management**: Bounded caches with TTL and LRU eviction policies
- **Batch Operations**: Chunked processing to handle API limitations
- **Reactive Updates**: Smart cache invalidation and selective state updates
- **Resource Cleanup**: Comprehensive lifecycle management for listeners and coroutines

### **UI Performance Patterns**
- **State Combination**: Reducing recomposition through combined state flows
- **Lazy Loading**: Pagination and virtualization for large datasets
- **Memoization**: Expensive calculations cached with remember/derivedStateOf
- **Key-based Optimization**: Stable keys for efficient list recomposition

### **Firebase Optimization**
- **Query Optimization**: Index-aware query construction and batching
- **Connection Management**: Efficient listener lifecycle and connection pooling
- **Data Fetching**: Prefetching and pagination strategies
- **Performance Monitoring**: Comprehensive tracing and analytics integration

## 9. IMPLEMENTATION PRIORITY MATRIX

### **High Impact, Low Effort**
1. **State Combination**: Combine multiple StateFlows to reduce recomposition
2. **Memory-Safe Caching**: Replace unbounded maps with TTL-based caches
3. **Query Batching**: Implement chunked operations for Firestore limitations
4. **Resource Cleanup**: Add proper listener and coroutine cleanup

### **High Impact, High Effort**
1. **Advanced Pagination**: Implement bidirectional scrolling and prefetching
2. **Offline Optimization**: Smart caching with sync conflict resolution
3. **Image Optimization**: Advanced memory management and compression
4. **Performance Dashboard**: Real-time monitoring and alerting system

### **Low Impact, Low Effort**
1. **Logging Optimization**: Structured logging with performance context
2. **Error Analytics**: Enhanced error tracking and categorization
3. **Memory Monitoring**: Regular memory usage reporting
4. **Cache Statistics**: Cache hit/miss ratio monitoring

## 10. IMPLEMENTATION GUIDELINES FOR AI

### **Optimization Decision Framework**
1. **Profile First**: Measure current performance before optimizing
2. **User Impact**: Prioritize optimizations that improve user experience
3. **Maintainability**: Ensure optimizations don't compromise code clarity
4. **Scalability**: Consider future growth and usage patterns

### **Quality Gates for Optimizations**
- **Memory Usage**: No unbounded caches or resource leaks
- **Response Times**: 95th percentile under acceptable thresholds
- **Battery Efficiency**: Minimal background processing and network usage
- **Code Complexity**: Optimizations maintain or improve readability

---

*AI Training Data for Optimization Opportunities - Generated from Fairr Android App Pass 9*
*File references verified and optimization strategies prioritized by impact* 