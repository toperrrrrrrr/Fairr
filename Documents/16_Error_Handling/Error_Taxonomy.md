# AI TRAINING DATA: Error Handling & Taxonomy - Fairr Android App

## 1. ERROR CLASSIFICATION SYSTEM

### **Error Taxonomy Hierarchy**
```
Application Errors
├── User Input Errors (4xx category)
│   ├── Validation Errors → User can correct
│   ├── Permission Errors → User lacks access
│   └── Authentication Errors → User needs to sign in
├── System Errors (5xx category)
│   ├── Network Errors → Connectivity issues
│   ├── Server Errors → Backend problems
│   └── Device Errors → Local storage/hardware issues
├── Business Logic Errors (Custom)
│   ├── Financial Calculation Errors → Precision/rounding issues
│   ├── Group Management Errors → Member conflicts
│   └── Settlement Errors → Complex debt resolution issues
└── Integration Errors (External)
    ├── Firebase Errors → Cloud service issues
    ├── Currency API Errors → Exchange rate failures
    └── Analytics Errors → Tracking failures
```

### **Error Severity Levels**
```kotlin
enum class ErrorSeverity(val level: Int, val userActionRequired: Boolean) {
    INFO(0, false),          // Informational - no action needed
    WARNING(1, false),       // Warning - app continues normally
    ERROR(2, true),          // Error - user action may be required
    CRITICAL(3, true),       // Critical - app functionality impaired
    FATAL(4, true)           // Fatal - app cannot continue
}

// Error classification with proper severity assignment
sealed class FairrError(
    val code: String,
    val message: String,
    val severity: ErrorSeverity,
    val isRetryable: Boolean = false,
    val cause: Throwable? = null
) : Exception(message, cause)
```

## 2. COMPREHENSIVE ERROR DEFINITIONS

### **Authentication & Authorization Errors**
```kotlin
// CATEGORY: Authentication errors with specific handling
sealed class AuthError(
    code: String,
    message: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    isRetryable: Boolean = false,
    cause: Throwable? = null
) : FairrError(code, message, severity, isRetryable, cause) {
    
    object InvalidCredentials : AuthError(
        code = "AUTH_001",
        message = "Invalid email or password. Please check your credentials and try again.",
        severity = ErrorSeverity.ERROR,
        isRetryable = true
    )
    
    object UserNotVerified : AuthError(
        code = "AUTH_002", 
        message = "Please verify your email address before signing in.",
        severity = ErrorSeverity.WARNING,
        isRetryable = false
    )
    
    object AccountDisabled : AuthError(
        code = "AUTH_003",
        message = "Your account has been disabled. Please contact support.",
        severity = ErrorSeverity.CRITICAL,
        isRetryable = false
    )
    
    object SessionExpired : AuthError(
        code = "AUTH_004",
        message = "Your session has expired. Please sign in again.",
        severity = ErrorSeverity.ERROR,
        isRetryable = true
    )
    
    object NetworkAuthError : AuthError(
        code = "AUTH_005",
        message = "Unable to connect to authentication service. Check your internet connection.",
        severity = ErrorSeverity.ERROR,
        isRetryable = true
    )
}

// IMPLEMENTATION: Authentication error handling
class AuthService {
    private fun handleAuthException(exception: Exception): Result<Unit> {
        val error = when (exception) {
            is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
            is FirebaseAuthUserCollisionException -> AuthError.AccountAlreadyExists  
            is FirebaseAuthEmailException -> AuthError.InvalidEmail
            is FirebaseNetworkException -> AuthError.NetworkAuthError
            else -> AuthError.UnknownError(cause = exception)
        }
        
        _authState.value = AuthState.Error(error)
        logSecurityEvent(SecurityEvent.AUTH_FAILURE, error.code)
        
        return Result.failure(error)
    }
}
```

### **Financial & Business Logic Errors**
```kotlin
// CATEGORY: Financial calculation and business rule errors
sealed class FinancialError(
    code: String,
    message: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    isRetryable: Boolean = false,
    cause: Throwable? = null
) : FairrError(code, message, severity, isRetryable, cause) {
    
    data class InvalidAmount(val amount: Double) : FinancialError(
        code = "FIN_001",
        message = "Invalid amount: $amount. Amount must be greater than 0.",
        severity = ErrorSeverity.ERROR
    )
    
    data class AmountTooLarge(val amount: Double, val maxAmount: Double) : FinancialError(
        code = "FIN_002", 
        message = "Amount $amount exceeds maximum limit of $maxAmount.",
        severity = ErrorSeverity.ERROR
    )
    
    data class SplitCalculationError(val totalAmount: Double, val splitSum: Double) : FinancialError(
        code = "FIN_003",
        message = "Split amounts ($splitSum) don't match total amount ($totalAmount).",
        severity = ErrorSeverity.ERROR
    )
    
    data class CurrencyConversionError(val fromCurrency: String, val toCurrency: String) : FinancialError(
        code = "FIN_004",
        message = "Unable to convert from $fromCurrency to $toCurrency. Exchange rates unavailable.",
        severity = ErrorSeverity.WARNING,
        isRetryable = true
    )
    
    data class SettlementCalculationError(val groupId: String) : FinancialError(
        code = "FIN_005",
        message = "Unable to calculate settlements for group. Data may be inconsistent.",
        severity = ErrorSeverity.CRITICAL,
        isRetryable = true
    )
}

// IMPLEMENTATION: Financial error handling with recovery
class SplitCalculator {
    fun calculateSplits(
        totalAmount: Double,
        splitType: String,
        members: List<Map<String, Any>>
    ): Result<List<Map<String, Any>>> {
        return try {
            // Validate input
            if (totalAmount <= 0) {
                return Result.failure(FinancialError.InvalidAmount(totalAmount))
            }
            
            if (totalAmount > MAX_EXPENSE_AMOUNT) {
                return Result.failure(FinancialError.AmountTooLarge(totalAmount, MAX_EXPENSE_AMOUNT))
            }
            
            val splits = when (splitType) {
                "Equal Split" -> calculateEqualSplit(totalAmount, members)
                "Percentage" -> calculatePercentageSplit(totalAmount, members)
                "Custom Amount" -> calculateCustomAmountSplit(totalAmount, members)
                else -> throw IllegalArgumentException("Unknown split type: $splitType")
            }
            
            // Validate calculation result
            val splitSum = splits.sumOf { it["amount"] as Double }
            if (abs(splitSum - totalAmount) > 0.01) { // Allow 1 cent tolerance for rounding
                return Result.failure(FinancialError.SplitCalculationError(totalAmount, splitSum))
            }
            
            Result.success(splits)
            
        } catch (e: Exception) {
            Result.failure(FinancialError.CalculationError(cause = e))
        }
    }
}
```

### **Network & Connectivity Errors**
```kotlin
// CATEGORY: Network and connectivity errors with retry strategies
sealed class NetworkError(
    code: String,
    message: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    isRetryable: Boolean = true,
    cause: Throwable? = null
) : FairrError(code, message, severity, isRetryable, cause) {
    
    object NoConnection : NetworkError(
        code = "NET_001",
        message = "No internet connection. Please check your network settings.",
        severity = ErrorSeverity.ERROR
    )
    
    object Timeout : NetworkError(
        code = "NET_002",
        message = "Request timed out. Please try again.",
        severity = ErrorSeverity.WARNING
    )
    
    data class ServerError(val httpCode: Int) : NetworkError(
        code = "NET_003",
        message = "Server error ($httpCode). Please try again later.",
        severity = if (httpCode >= 500) ErrorSeverity.CRITICAL else ErrorSeverity.ERROR
    )
    
    object RateLimited : NetworkError(
        code = "NET_004", 
        message = "Too many requests. Please wait before trying again.",
        severity = ErrorSeverity.WARNING,
        isRetryable = false
    )
    
    data class FirebaseError(val firebaseCode: String) : NetworkError(
        code = "NET_005",
        message = "Firebase service error: $firebaseCode",
        severity = ErrorSeverity.CRITICAL
    )
}

// IMPLEMENTATION: Network error handling with exponential backoff
class NetworkRetryHandler {
    suspend fun <T> executeWithRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 10000,
        operation: suspend () -> T
    ): Result<T> {
        var currentDelay = initialDelay
        var lastException: Exception? = null
        
        repeat(maxAttempts) { attempt ->
            try {
                return Result.success(operation())
            } catch (e: Exception) {
                lastException = e
                
                val error = classifyNetworkError(e)
                
                // Don't retry if error is not retryable
                if (!error.isRetryable) {
                    return Result.failure(error)
                }
                
                // Don't retry on last attempt
                if (attempt == maxAttempts - 1) {
                    return Result.failure(error)
                }
                
                // Exponential backoff with jitter
                val jitter = Random.nextLong(0, currentDelay / 4)
                delay(currentDelay + jitter)
                currentDelay = (currentDelay * 2).coerceAtMost(maxDelay)
                
                Log.d(TAG, "Retrying operation after ${currentDelay}ms (attempt ${attempt + 2}/$maxAttempts)")
            }
        }
        
        return Result.failure(lastException ?: NetworkError.UnknownError())
    }
    
    private fun classifyNetworkError(exception: Exception): NetworkError {
        return when (exception) {
            is UnknownHostException, is ConnectException -> NetworkError.NoConnection
            is SocketTimeoutException -> NetworkError.Timeout  
            is HttpException -> NetworkError.ServerError(exception.code())
            is FirebaseException -> NetworkError.FirebaseError(exception.message ?: "unknown")
            else -> NetworkError.UnknownError(cause = exception)
        }
    }
}
```

### **Data Validation Errors**
```kotlin
// CATEGORY: Input validation errors with specific field information
sealed class ValidationError(
    code: String,
    message: String,
    val field: String,
    severity: ErrorSeverity = ErrorSeverity.ERROR,
    cause: Throwable? = null
) : FairrError(code, message, severity, false, cause) {
    
    data class RequiredField(val fieldName: String) : ValidationError(
        code = "VAL_001",
        message = "$fieldName is required.",
        field = fieldName
    )
    
    data class InvalidFormat(val fieldName: String, val expectedFormat: String) : ValidationError(
        code = "VAL_002", 
        message = "$fieldName has invalid format. Expected: $expectedFormat",
        field = fieldName
    )
    
    data class ValueTooShort(val fieldName: String, val minLength: Int, val actualLength: Int) : ValidationError(
        code = "VAL_003",
        message = "$fieldName must be at least $minLength characters (current: $actualLength).",
        field = fieldName
    )
    
    data class ValueTooLong(val fieldName: String, val maxLength: Int, val actualLength: Int) : ValidationError(
        code = "VAL_004",
        message = "$fieldName cannot exceed $maxLength characters (current: $actualLength).",
        field = fieldName
    )
    
    data class InvalidRange(val fieldName: String, val min: Double, val max: Double, val actual: Double) : ValidationError(
        code = "VAL_005",
        message = "$fieldName must be between $min and $max (current: $actual).",
        field = fieldName
    )
}

// IMPLEMENTATION: Comprehensive validation with detailed error reporting
class ValidationUtils {
    fun validateExpense(expense: ExpenseData): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Description validation
        if (expense.description.isBlank()) {
            errors.add(ValidationError.RequiredField("Description"))
        } else if (expense.description.length > MAX_DESCRIPTION_LENGTH) {
            errors.add(ValidationError.ValueTooLong(
                "Description", 
                MAX_DESCRIPTION_LENGTH, 
                expense.description.length
            ))
        }
        
        // Amount validation
        if (expense.amount <= 0) {
            errors.add(ValidationError.InvalidRange(
                "Amount", 
                0.01, 
                MAX_EXPENSE_AMOUNT, 
                expense.amount
            ))
        }
        
        // Email validation with specific patterns
        if (expense.paidByEmail.isNotEmpty() && !isValidEmail(expense.paidByEmail)) {
            errors.add(ValidationError.InvalidFormat(
                "Email", 
                "valid email address (example@domain.com)"
            ))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error(errors)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: List<ValidationError>) : ValidationResult()
}
```

## 3. ERROR PROPAGATION & HANDLING PATTERNS

### **ViewModel Error State Management**
```kotlin
// PATTERN: Centralized error state management in ViewModels
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: FairrError) : UiState<Nothing>
}

class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()
    
    suspend fun addExpense(expenseData: ExpenseData) {
        _uiState.value = UiState.Loading
        
        try {
            // Validate input
            val validationResult = ValidationUtils.validateExpense(expenseData)
            if (validationResult is ValidationResult.Error) {
                _uiState.value = UiState.Error(
                    ValidationError.MultipleErrors(validationResult.errors)
                )
                return
            }
            
            // Create expense
            val expenseId = expenseRepository.addExpense(expenseData.toExpense())
            _uiState.value = UiState.Success(expenseId)
            
        } catch (e: Exception) {
            val error = when (e) {
                is FairrError -> e
                is FirebaseException -> NetworkError.FirebaseError(e.message ?: "unknown")
                else -> FairrError.UnknownError(cause = e)
            }
            
            _uiState.value = UiState.Error(error)
            logError(error, "AddExpenseViewModel.addExpense")
        }
    }
}
```

### **Repository Error Transformation**
```kotlin
// PATTERN: Error transformation at repository boundary
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val errorLogger: ErrorLogger
) : ExpenseRepository {
    
    override suspend fun addExpense(expense: Expense): String {
        return try {
            val docRef = firestore.collection("expenses").add(expense.toMap()).await()
            docRef.id
            
        } catch (e: Exception) {
            val transformedError = transformFirebaseError(e, "addExpense")
            errorLogger.logError(transformedError, "ExpenseRepository")
            throw transformedError
        }
    }
    
    private fun transformFirebaseError(exception: Exception, operation: String): FairrError {
        return when (exception) {
            is FirebaseFirestoreException -> when (exception.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                    AuthError.InsufficientPermissions
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    NetworkError.ServiceUnavailable
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    NetworkError.Timeout
                else -> DataError.DatabaseError(exception.code.name)
            }
            is FirebaseNetworkException -> NetworkError.NoConnection
            is IllegalArgumentException -> ValidationError.InvalidData(exception.message ?: "")
            else -> FairrError.UnknownError(cause = exception)
        }
    }
}
```

### **UI Error Display Patterns**
```kotlin
// PATTERN: Consistent error UI presentation
@Composable
fun ErrorDisplay(
    error: FairrError,
    onRetry: (() -> Unit)? = null,
    onDismiss: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (error.severity) {
                ErrorSeverity.WARNING -> MaterialTheme.colorScheme.warningContainer
                ErrorSeverity.ERROR -> MaterialTheme.colorScheme.errorContainer
                ErrorSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Error icon based on severity
            Icon(
                imageVector = when (error.severity) {
                    ErrorSeverity.WARNING -> Icons.Default.Warning
                    ErrorSeverity.ERROR, ErrorSeverity.CRITICAL -> Icons.Default.Error
                    else -> Icons.Default.Info
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error message
            Text(
                text = error.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            // Error code for debugging
            if (BuildConfig.DEBUG) {
                Text(
                    text = "Error Code: ${error.code}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (error.isRetryable && onRetry != null) {
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Retry")
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}

// COMPOSABLE: Error-aware screen wrapper
@Composable
fun <T> ErrorAwareContent(
    uiState: UiState<T>,
    onRetry: (() -> Unit)? = null,
    loadingContent: @Composable () -> Unit = { LoadingSpinner() },
    successContent: @Composable (T) -> Unit
) {
    when (uiState) {
        is UiState.Loading -> loadingContent()
        is UiState.Success -> successContent(uiState.data)
        is UiState.Error -> ErrorDisplay(
            error = uiState.error,
            onRetry = onRetry
        )
    }
}
```

## 4. ERROR LOGGING & MONITORING

### **Comprehensive Error Logging**
```kotlin
// IMPLEMENTATION: Structured error logging for production monitoring
class ErrorLogger @Inject constructor(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics
) {
    
    fun logError(
        error: FairrError,
        context: String,
        userId: String? = null,
        additionalData: Map<String, Any> = emptyMap()
    ) {
        // Log to Crashlytics for crash reporting
        crashlytics.apply {
            setCustomKey("error_code", error.code)
            setCustomKey("error_severity", error.severity.name)
            setCustomKey("context", context)
            setCustomKey("is_retryable", error.isRetryable)
            
            userId?.let { setUserId(it) }
            
            additionalData.forEach { (key, value) ->
                setCustomKey(key, value.toString())
            }
            
            recordException(error)
        }
        
        // Log to Analytics for usage patterns
        analytics.logEvent("app_error", Bundle().apply {
            putString("error_code", error.code)
            putString("error_category", getErrorCategory(error))
            putString("context", context)
            putString("severity", error.severity.name)
            putBoolean("is_retryable", error.isRetryable)
        })
        
        // Local logging for development
        val logLevel = when (error.severity) {
            ErrorSeverity.INFO -> Log.INFO
            ErrorSeverity.WARNING -> Log.WARN
            ErrorSeverity.ERROR -> Log.ERROR
            ErrorSeverity.CRITICAL, ErrorSeverity.FATAL -> Log.ERROR
        }
        
        Log.println(logLevel, TAG, "Error [${error.code}] in $context: ${error.message}")
        error.cause?.let { Log.println(logLevel, TAG, "Caused by: ${it.message}", it) }
    }
    
    private fun getErrorCategory(error: FairrError): String {
        return when (error) {
            is AuthError -> "authentication"
            is FinancialError -> "financial"
            is NetworkError -> "network"
            is ValidationError -> "validation"
            is DataError -> "data"
            else -> "general"
        }
    }
}
```

### **Error Metrics & Analytics**
```kotlin
// MONITORING: Error rate tracking and alerting
class ErrorMetricsCollector @Inject constructor(
    private val analytics: FirebaseAnalytics
) {
    
    private val errorCounts = ConcurrentHashMap<String, AtomicLong>()
    private val errorRates = ConcurrentHashMap<String, CircularBuffer<Long>>()
    
    fun trackError(error: FairrError, context: String) {
        val errorKey = "${error.code}_$context"
        
        // Increment error count
        errorCounts.computeIfAbsent(errorKey) { AtomicLong(0) }.incrementAndGet()
        
        // Track error rate (errors per minute)
        val currentMinute = System.currentTimeMillis() / 60000
        errorRates.computeIfAbsent(errorKey) { CircularBuffer(60) }.add(currentMinute)
        
        // Check for error spikes
        checkForErrorSpikes(errorKey, error.severity)
        
        // Log error metrics
        analytics.logEvent("error_metrics", Bundle().apply {
            putString("error_code", error.code)
            putString("context", context)
            putLong("total_count", errorCounts[errorKey]?.get() ?: 0)
            putDouble("error_rate_per_minute", calculateErrorRate(errorKey))
        })
    }
    
    private fun checkForErrorSpikes(errorKey: String, severity: ErrorSeverity) {
        val errorRate = calculateErrorRate(errorKey)
        val threshold = when (severity) {
            ErrorSeverity.CRITICAL, ErrorSeverity.FATAL -> 1.0 // 1 error per minute
            ErrorSeverity.ERROR -> 5.0 // 5 errors per minute
            else -> 10.0 // 10 errors per minute
        }
        
        if (errorRate > threshold) {
            // Alert development team
            Log.e(TAG, "Error spike detected: $errorKey rate=$errorRate (threshold=$threshold)")
            
            // Send to crash reporting for immediate attention
            crashlytics.log("ERROR SPIKE: $errorKey rate=$errorRate")
        }
    }
    
    private fun calculateErrorRate(errorKey: String): Double {
        val buffer = errorRates[errorKey] ?: return 0.0
        val currentMinute = System.currentTimeMillis() / 60000
        val recentErrors = buffer.getValues().count { it > currentMinute - 5 } // Last 5 minutes
        return recentErrors / 5.0 // Errors per minute over last 5 minutes
    }
}

// Utility class for error rate calculation
class CircularBuffer<T>(private val capacity: Int) {
    private val buffer = ArrayDeque<T>(capacity)
    
    fun add(item: T) {
        if (buffer.size >= capacity) {
            buffer.removeFirst()
        }
        buffer.addLast(item)
    }
    
    fun getValues(): List<T> = buffer.toList()
}
```

## 5. AI LEARNING OBJECTIVES FOR ERROR HANDLING

### **Error Classification Mastery**
- **Taxonomic Organization**: Hierarchical error classification with severity levels
- **Business Logic Errors**: Financial calculation and domain-specific error handling
- **Recovery Strategies**: Retry logic, fallback mechanisms, and user guidance
- **Error Propagation**: Clean error flow through architectural layers

### **Production Error Management**
- **Comprehensive Logging**: Structured error reporting for monitoring and debugging
- **Error Metrics**: Rate tracking, spike detection, and performance impact analysis
- **User Experience**: Error presentation that guides users toward resolution
- **Monitoring Integration**: Firebase Crashlytics and Analytics integration patterns

### **Resilience Patterns**
- **Network Resilience**: Retry with exponential backoff and circuit breaker patterns
- **Data Validation**: Comprehensive input validation with specific error feedback
- **Graceful Degradation**: App functionality maintenance during partial service failures
- **Error Recovery**: User-guided recovery flows and state restoration strategies

---

*AI Training Data for Error Handling & Taxonomy - Production-Grade Error Management*
*Essential for building resilient, user-friendly Android applications with comprehensive error handling* 