# AI TRAINING DATA: Backend Architecture & Firebase Integration - Fairr Android App

## 1. FILE-TO-BACKEND-COMPONENT MAPPING

### **Firebase Integration Architecture**
```
di/AppModule.kt → Firebase SDK configuration and service bindings
di/AuthModule.kt → Authentication-specific dependency injection
data/auth/AuthService.kt → Firebase Auth wrapper with state management
data/auth/GoogleAuthService.kt → Google Sign-In integration
→ Pattern: Service layer abstraction over Firebase SDKs
```

### **Data Layer Organization (`data/`)**

#### **Repository Pattern Implementation**
```
data/repository/ExpenseRepository.kt → Complex expense data access with pagination
data/repository/UserRepository.kt → User data management and Firestore mapping
data/repository/SplitCalculator.kt → Business logic for expense calculations
data/repository/AdvancedSplitCalculator.kt → Complex splitting algorithms
data/repository/RecurringExpenseScheduler.kt → Automated recurring expense logic
→ Pattern: Repository pattern with interface segregation and business logic encapsulation
```

#### **Domain Services**
```
data/groups/GroupService.kt → Group CRUD operations with real-time listeners
data/groups/GroupJoinService.kt → Group invitation and joining workflows
data/groups/GroupInviteService.kt → Invitation system with unique codes
data/expenses/ExpenseService.kt → Expense business logic and validation
data/friends/FriendService.kt → Friend relationship management
data/friends/FriendSuggestionsService.kt → Recommendation algorithms
data/settlements/SettlementService.kt → Debt calculation and optimization
data/notifications/NotificationService.kt → Push notification management
data/analytics/AnalyticsService.kt → Event tracking and user analytics
→ Pattern: Domain-driven service organization with single responsibility
```

#### **Data Models (`data/model/`)**
```
data/model/Expense.kt → Core expense domain model with category enums
data/model/Group.kt → Group entity with member management
data/model/User.kt → User profile and authentication data
data/model/GroupActivity.kt → Activity tracking for audit trails
data/model/Notification.kt → Notification data structure
data/model/AdvancedRecurrenceRule.kt → Complex recurrence pattern definitions
→ Pattern: Rich domain models with business logic and validation
```

### **Local Data Management**
```
data/preferences/UserPreferencesManager.kt → DataStore-based local storage
data/preferences/UserPreferencesRepository.kt → Preference data access layer
data/settings/SettingsDataStore.kt → App settings and configuration
→ Pattern: DataStore integration for reactive local data management
```

## 2. FIREBASE INTEGRATION PATTERN TRAINING DATA

### **Authentication Architecture (`data/auth/AuthService.kt`)**
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
            AuthState.Authenticated(user)
        } else {
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
                user.getIdToken(true).await()  // Force token refresh for validation
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
```
**AI Learning Points:**
- StateFlow-based reactive authentication state management
- Firebase Auth listener pattern for real-time auth state updates
- Token validation for session management
- Proper cleanup with listener management in init block

### **Repository Pattern with Firestore (`data/repository/ExpenseRepository.kt`)**
```kotlin
@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val activityService: ActivityService
) : ExpenseRepository {

    // Cache for performance optimization
    private val userNameCache = mutableMapOf<String, String>()
    
    override suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses = 
        withContext(Dispatchers.IO) {
            try {
                val pageSize = params.pageSize.coerceAtMost(MAX_PAGE_SIZE)
                var query = firestore.collection("expenses")
                    .whereEqualTo("groupId", params.groupId)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
                
                // Apply filters
                params.category?.let { category ->
                    query = query.whereEqualTo("category", category.name)
                }
                
                // Handle pagination
                params.lastDocument?.let { lastDoc ->
                    query = query.startAfter(lastDoc)
                }
                
                val snapshot = query.get().await()
                val expenses = parseExpensesOptimized(snapshot.documents)
                
                PaginatedExpenses(
                    expenses = expenses,
                    hasMore = snapshot.documents.size == pageSize,
                    lastDocument = snapshot.documents.lastOrNull()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting paginated expenses", e)
                PaginatedExpenses(emptyList(), false, null)
            }
        }
}
```
**AI Learning Points:**
- Repository interface implementation with dependency injection
- Pagination pattern with Firestore cursor-based navigation
- Performance optimization with coroutine context switching
- Error handling with graceful fallback responses
- Caching strategy for frequently accessed data

### **Real-time Data Flow (`data/groups/GroupService.kt`)**
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
                    val data = doc.data ?: return@mapNotNull null
                    Group(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        currency = data["currency"] as? String ?: "PHP",
                        members = parseGroupData(data).map { (userId, memberData) ->
                            GroupMember(
                                userId = userId,
                                name = memberData["name"] as? String ?: "Unknown",
                                role = if (memberData["isAdmin"] as? Boolean == true) 
                                    GroupRole.ADMIN else GroupRole.MEMBER
                            )
                        }
                    )
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
- CallbackFlow pattern for Firebase real-time listeners
- Proper resource cleanup with awaitClose
- Null safety and error handling in data parsing
- Type-safe data extraction from Firestore documents
- Authentication-aware query filtering

### **Local Data Persistence (`data/preferences/UserPreferencesManager.kt`)**
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
- DataStore Preferences API for type-safe local storage
- Reactive data flows with Flow transformations
- Session management with timestamp validation
- Atomic data operations with edit transactions

## 3. DOMAIN MODEL ARCHITECTURE TRAINING

### **Rich Domain Models (`data/model/Expense.kt`)**
```kotlin
data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "PHP",
    val date: Timestamp = Timestamp.now(),
    val paidBy: String = "",
    val paidByName: String = "",
    val splitBetween: List<ExpenseSplit> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val splitType: String = "Equal Split",
    // Recurrence fields
    val isRecurring: Boolean = false,
    val recurrenceRule: RecurrenceRule? = null,
    val parentExpenseId: String? = null
)

enum class ExpenseCategory(val displayName: String, val icon: String, val color: String) {
    FOOD("Food & Dining", "🍽️", "#FF6B6B"),
    TRANSPORTATION("Transportation", "🚗", "#4ECDC4"),
    ACCOMMODATION("Accommodation", "🏨", "#45B7D1"),
    // ... other categories
    OTHER("Other", "📦", "#95A5A6");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                OTHER
            }
        }
    }
}
```
**AI Learning Points:**
- Data class design with sensible defaults
- Enum-based categorization with UI metadata
- Null safety patterns with optional fields
- Firebase Timestamp integration for date handling
- Companion object methods for safe enum conversion

### **Business Logic Encapsulation**
```kotlin
data class ExpenseSplit(
    val userId: String,
    val userName: String,
    val share: Double,
    val isPaid: Boolean = false
)

data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val interval: Int = 1,
    val endDate: Timestamp? = null
)
```

## 4. DEPENDENCY INJECTION ARCHITECTURE

### **Main App Module (`di/AppModule.kt`)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideExpenseRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        activityService: ActivityService
    ): ExpenseRepository = ExpenseRepositoryImpl(firestore, auth, activityService)

    @Provides
    @Singleton
    fun provideGroupService(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): GroupService = GroupService(auth, firestore)
}
```
**AI Learning Points:**
- Hilt dependency injection with singleton scoping
- Firebase configuration with performance optimizations
- Interface-to-implementation binding patterns
- Constructor injection for service dependencies

### **Authentication Module (`di/AuthModule.kt`)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideAuthService(auth: FirebaseAuth): AuthService = AuthService(auth)

    @Provides
    @Singleton
    fun provideGoogleAuthService(
        auth: FirebaseAuth,
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ): GoogleAuthService = GoogleAuthService(auth, context, userRepository)
}
```

## 5. DATA FLOW & SYNCHRONIZATION PATTERNS

### **Layered Architecture Flow**
```
UI Layer (Compose + ViewModels)
    ↓ (StateFlow/LiveData)
Business Layer (Services + Repositories)
    ↓ (Firebase SDK calls)
Data Layer (Firestore + Auth + Storage)
    ↓ (Real-time listeners)
Local Cache (DataStore + Room)
```

### **Real-time Data Synchronization**
```
Firebase Firestore → Real-time listeners → CallbackFlow → StateFlow → Compose UI
```

### **Error Handling Chain**
```
Firestore Exception → Repository Error Handling → Service Result Types → ViewModel Error State → UI Error Display
```

## 6. PERFORMANCE & SCALABILITY PATTERNS

### **Pagination Implementation**
```kotlin
// Efficient pagination with cursor-based navigation
data class PaginatedExpenses(
    val expenses: List<Expense>,
    val hasMore: Boolean,
    val lastDocument: DocumentSnapshot?,
    val totalCount: Int? = null
)

// Query optimization with proper indexing
var query = firestore.collection("expenses")
    .whereEqualTo("groupId", params.groupId)  // Indexed field
    .orderBy("createdAt", Query.Direction.DESCENDING)  // Indexed sort
    .limit(pageSize.toLong())
```

### **Caching Strategy**
```kotlin
// In-memory caching for frequently accessed data
private val userNameCache = mutableMapOf<String, String>()

// Firestore offline persistence
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()
```

### **Background Processing**
```kotlin
// Coroutine context switching for heavy operations
override suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses = 
    withContext(Dispatchers.IO) {
        // Heavy Firestore operations on IO thread
    }
```

## 7. SECURITY & VALIDATION PATTERNS

### **Authentication Validation**
```kotlin
suspend fun isUserAuthenticated(): Boolean {
    return try {
        val user = auth.currentUser
        if (user != null) {
            user.getIdToken(true).await()  // Force token refresh
            true
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}
```

### **Input Validation**
```kotlin
private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}
```

### **Error Message Standardization**
```kotlin
private fun getFirebaseAuthErrorMessage(e: Exception): String {
    return when (e) {
        is FirebaseAuthInvalidUserException -> {
            when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "No account found with this email"
                "ERROR_USER_DISABLED" -> "This account has been disabled"
                else -> "Invalid email or password"
            }
        }
        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password"
        else -> e.message ?: "Authentication failed"
    }
}
```

## 8. AI PATTERN LEARNING OBJECTIVES

### **Firebase Integration Mastery**
- **Service Abstraction**: How to wrap Firebase SDKs with clean interfaces
- **Real-time Data**: CallbackFlow patterns for Firebase listeners
- **Authentication Flow**: StateFlow-based auth state management
- **Error Handling**: Graceful degradation and user-friendly error messages

### **Repository Pattern Recognition**
- **Interface Segregation**: Separating concerns with focused interfaces
- **Dependency Injection**: Proper service composition and lifecycle management
- **Caching Strategies**: Performance optimization through intelligent caching
- **Pagination**: Efficient data loading with cursor-based navigation

### **Domain Architecture Understanding**
- **Rich Models**: Business logic encapsulation in domain entities
- **Service Organization**: Domain-driven service boundaries
- **Data Flow**: Unidirectional data flow with reactive streams
- **Local Persistence**: DataStore integration for offline capabilities

## 9. IMPLEMENTATION GUIDELINES FOR AI

### **Quality Indicators**
1. **Separation of Concerns**: Clear boundaries between layers
2. **Error Handling**: Comprehensive exception management
3. **Performance**: Efficient queries and caching strategies
4. **Security**: Proper authentication and validation patterns

### **Common Anti-Patterns to Avoid**
- Direct Firebase SDK usage in UI layer
- Blocking operations on main thread
- Missing error handling in repository methods
- Inefficient queries without proper indexing
- Session management without token validation

---

*AI Training Data for Backend Architecture - Generated from Fairr Android App Pass 4*
*File references verified and Firebase integration patterns documented* 