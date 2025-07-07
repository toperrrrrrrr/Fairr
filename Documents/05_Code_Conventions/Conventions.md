# AI TRAINING DATA: Code Conventions & Standards - Fairr Android App

## 1. FILE-TO-CONVENTION MAPPING

### **Naming Convention Patterns**

#### **Class Naming Standards**
```
ViewModels: [FeatureName]ViewModel.kt
- HomeViewModel.kt → Home screen state management
- GroupDetailViewModel.kt → Group detail screen state
- AddExpenseViewModel.kt → Expense creation state
→ Pattern: Feature-specific ViewModel naming with descriptive prefixes

Services: [Domain]Service.kt
- AuthService.kt → Authentication business logic
- GroupService.kt → Group operations
- ExpenseService.kt → Expense management
→ Pattern: Domain-driven service naming

Repositories: [Entity]Repository.kt
- ExpenseRepository.kt → Expense data access layer
- UserRepository.kt → User data operations
→ Pattern: Entity-based repository naming

Components: [Type]Components.kt or [Prefix][ComponentName].kt
- CommonComponents.kt → Shared UI components
- FairrFilterChip → Branded component with prefix
- ModernCard → Style-variant component naming
→ Pattern: Categorized or branded component organization
```

#### **Package Organization Standards**
```
ui/screens/[feature]/ → Feature-based screen organization
ui/components/ → Reusable UI component library
data/[domain]/ → Domain-driven service organization
data/repository/ → Data access layer
data/model/ → Domain entity definitions
util/ → Utility classes and helpers
di/ → Dependency injection modules
→ Pattern: Clean Architecture layering with feature organization
```

### **File Structure Conventions**
```
MainActivity.kt → Single activity entry point
FairrApplication.kt → Application class with initialization
[Feature]Screen.kt → Jetpack Compose screen implementations
[Feature]ViewModel.kt → MVVM state management
[Domain]Service.kt → Business logic services
[Entity]Repository.kt → Data access interfaces and implementations
→ Pattern: Consistent suffix-based file identification
```

## 2. KOTLIN CODING CONVENTION TRAINING DATA

### **Class Definition Patterns**

#### **ViewModel Convention (`ui/screens/home/HomeViewModel.kt`)**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                // Implementation...
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}
```
**AI Learning Points:**
- Hilt dependency injection with constructor injection
- StateFlow pattern for reactive state management
- Private mutable state with public read-only exposure
- Proper coroutine usage in viewModelScope
- Comprehensive error handling with user-friendly messages

#### **Data Class Convention**
```kotlin
data class HomeScreenState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalBalance: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val activeGroups: Int = 0,
    val groups: List<Group> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val userCurrency: String = "PHP"
)
```
**AI Learning Points:**
- Data classes for state containers with default values
- Nullable types for optional/error states
- Descriptive property names with clear intent
- Sensible defaults for initialization

### **Service Class Patterns**

#### **Singleton Service Pattern (`data/auth/AuthService.kt`)**
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
        // Set initial state safely...
    }
}
```
**AI Learning Points:**
- Singleton scope for shared services
- StateFlow for reactive state management
- Proper lifecycle management with init blocks
- Functional programming style with expression bodies

### **Repository Interface Patterns**

#### **Interface Definition (`data/repository/ExpenseRepository.kt`)**
```kotlin
interface ExpenseRepository {
    suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        currency: String = "PHP",
        date: Date,
        paidBy: String,
        splitType: String,
        category: ExpenseCategory = ExpenseCategory.OTHER,
        isRecurring: Boolean = false,
        recurrenceRule: RecurrenceRule? = null
    )
    
    @Deprecated("Use getPaginatedExpenses instead for better performance")
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>
    
    suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses
    
    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>>
}
```
**AI Learning Points:**
- Interface-implementation separation for testability
- Suspend functions for asynchronous operations
- Default parameters for optional values
- Deprecation annotations with migration guidance
- Flow types for reactive data streams

### **Utility Class Conventions (`util/ValidationUtils.kt`)**
```kotlin
object ValidationUtils {
    
    // Constants for validation limits
    private const val MAX_EMAIL_LENGTH = 254
    private const val MIN_PASSWORD_LENGTH = 6
    private const val MAX_PASSWORD_LENGTH = 128
    private const val MIN_NAME_LENGTH = 1
    
    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim()
        
        return when {
            trimmedEmail.isBlank() -> ValidationResult.Error("Email address cannot be empty")
            trimmedEmail.length > MAX_EMAIL_LENGTH -> ValidationResult.Error("Email address is too long (max $MAX_EMAIL_LENGTH characters)")
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() -> ValidationResult.Error("Please enter a valid email address")
            else -> ValidationResult.Success
        }
    }
    
    fun sanitizeText(input: String): String {
        return input.trim()
            .replace(Regex("[<>\"'&]"), "") // Remove potentially dangerous characters
            .replace(Regex("\\s+"), " ") // Replace multiple spaces with single space
            .take(MAX_DESCRIPTION_LENGTH) // Limit length
    }
}
```
**AI Learning Points:**
- Object declaration for stateless utility classes
- Consistent naming with validation/utility purpose
- Comprehensive input validation with security considerations
- Clear error messages for user feedback
- Method chaining for data transformation

## 3. LOGGING & DEBUGGING CONVENTIONS

### **Logging Tag Pattern**
```kotlin
// Consistent across all classes with logging
private const val TAG = "GroupDetailViewModel"    // ViewModels
private const val TAG = "ExpenseRepository"       // Repositories  
private const val TAG = "AuthService"            // Services
private const val TAG = "PerformanceOptimizer"   // Utilities
→ Pattern: TAG matches class name for easy log filtering
```

### **Error Handling Patterns**
```kotlin
// Repository level error handling
try {
    val snapshot = query.get().await()
    val expenses = parseExpensesOptimized(snapshot.documents)
    // Success path...
} catch (e: Exception) {
    Log.e(TAG, "Error getting paginated expenses", e)
    PaginatedExpenses(
        expenses = emptyList(),
        hasMore = false,
        lastDocument = null
    )
}

// ViewModel level error handling
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
```
**AI Learning Points:**
- Consistent error logging with TAG and exception details
- Graceful fallback values for failed operations
- User-friendly error messages in UI state
- Exception type preservation for debugging

## 4. DEPENDENCY INJECTION CONVENTIONS

### **Constructor Injection Pattern**
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel()

@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth
)

class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val activityService: ActivityService
) : ExpenseRepository
```
**AI Learning Points:**
- Constructor injection for all dependencies
- Private val for injected dependencies
- Appropriate scope annotations (@HiltViewModel, @Singleton)
- Interface-to-implementation binding

### **Module Organization (`di/AppModule.kt`, `di/AuthModule.kt`)**
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
}
```
**AI Learning Points:**
- Feature-based module organization
- Singleton scope for expensive resources
- Configuration within provider methods
- Clear provider method naming

## 5. DOCUMENTATION & COMMENT CONVENTIONS

### **Class Documentation**
```kotlin
/**
 * Comprehensive input validation utility for the Fairr app
 * Provides consistent validation across all screens and components
 */
object ValidationUtils

/**
 * Pagination result wrapper
 */
data class PaginatedExpenses(
    val expenses: List<Expense>,
    val hasMore: Boolean,
    val lastDocument: DocumentSnapshot?,
    val totalCount: Int? = null
)
```

### **Method Documentation**
```kotlin
/**
 * Get paginated expenses with optimized performance
 */
suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses

/**
 * Validate email address format and length
 */
fun validateEmail(email: String): ValidationResult

/**
 * Sanitize text input to prevent injection attacks
 */
fun sanitizeText(input: String): String
```

### **Inline Comments**
```kotlin
// Cache for performance optimization
private val userNameCache = mutableMapOf<String, String>()

// Apply filters
params.category?.let { category ->
    query = query.whereEqualTo("category", category.name)
}

// Force token refresh to validate session
user.getIdToken(true).await()
```
**AI Learning Points:**
- Purpose-driven documentation comments
- Implementation detail comments for complex logic
- Context-providing comments for business decisions

## 6. ENUM & SEALED CLASS CONVENTIONS

### **Enum with Metadata Pattern (`data/model/Expense.kt`)**
```kotlin
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

### **Sealed Class for State Management**
```kotlin
sealed class AuthResult {
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class AuthState {
    object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
```
**AI Learning Points:**
- Rich enums with UI metadata
- Safe enum conversion with fallbacks
- Sealed classes for exhaustive state representation
- Descriptive state names with clear intent

## 7. PERFORMANCE & MEMORY CONVENTIONS

### **Coroutine Usage Patterns**
```kotlin
// Repository operations on IO thread
override suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses = 
    withContext(Dispatchers.IO) {
        // Heavy Firestore operations
    }

// ViewModel scope for UI-related coroutines
private fun loadHomeData() {
    viewModelScope.launch {
        // UI state management
    }
}
```

### **Caching Strategies**
```kotlin
// In-memory caching for frequently accessed data
private val userNameCache = mutableMapOf<String, String>()

// Firestore offline persistence configuration
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()
```

### **Resource Management**
```kotlin
// Proper Flow cleanup with awaitClose
fun getUserGroups(): Flow<List<Group>> = callbackFlow {
    val subscription = groupsCollection
        .addSnapshotListener { snapshot, error -> /* ... */ }

    awaitClose { subscription.remove() }
}
```

## 8. VALIDATION & SECURITY CONVENTIONS

### **Input Validation Patterns**
```kotlin
private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}

fun sanitizeText(input: String): String {
    return input.trim()
        .replace(Regex("[<>\"'&]"), "")  // Security: Remove dangerous characters
        .replace(Regex("\\s+"), " ")      // Normalize whitespace
        .take(MAX_DESCRIPTION_LENGTH)     // Prevent abuse
}
```

### **Authentication Checks**
```kotlin
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
```

## 9. AI PATTERN LEARNING OBJECTIVES

### **Code Organization Recognition**
- **Feature-Based Structure**: How to organize code by business features
- **Layer Separation**: Clear boundaries between UI, business, and data layers
- **Naming Consistency**: Systematic naming conventions across file types
- **Package Strategy**: Domain-driven package organization patterns

### **Kotlin Best Practices**
- **Null Safety**: Proper handling of nullable types and safe calls
- **Coroutine Usage**: Appropriate context switching and scope management
- **Data Classes**: Immutable state containers with sensible defaults
- **Sealed Classes**: Exhaustive state representation for type safety

### **Architecture Compliance**
- **Dependency Injection**: Constructor injection with appropriate scoping
- **Clean Architecture**: Proper layer dependencies and abstractions
- **MVVM Pattern**: ViewModel state management with reactive streams
- **Repository Pattern**: Data access abstraction with interface segregation

## 10. IMPLEMENTATION GUIDELINES FOR AI

### **Quality Indicators**
1. **Consistent Naming**: All files follow established naming conventions
2. **Proper Scope**: Dependencies injected with appropriate lifecycle scopes
3. **Error Handling**: Comprehensive exception management with logging
4. **Documentation**: Clear purpose documentation for public APIs

### **Common Anti-Patterns to Avoid**
- Inconsistent naming conventions across similar components
- Missing error handling in async operations
- Hard-coded strings instead of constants
- Poor separation of concerns between layers
- Missing documentation for public interfaces

### **Code Review Checklist**
```
✓ File names follow [Feature/Entity][Type].kt pattern
✓ Classes have appropriate scope annotations
✓ Error handling with user-friendly messages
✓ Consistent logging with TAG constants
✓ Input validation for security
✓ Proper coroutine context switching
✓ StateFlow/Flow for reactive data
✓ Documentation for public APIs
```

---

*AI Training Data for Code Conventions - Generated from Fairr Android App Pass 5*
*File references verified and coding standards documented* 