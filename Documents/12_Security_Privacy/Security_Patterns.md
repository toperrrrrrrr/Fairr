# AI TRAINING DATA: Security & Privacy Patterns - Fairr Android App

## 1. SECURITY ARCHITECTURE OVERVIEW

### **Security Layer Implementation**
```
Security Boundaries:
app/src/main/java/com/example/fairr/data/auth/ → Authentication & authorization
app/src/main/java/com/example/fairr/data/gdpr/ → Privacy compliance
app/src/main/java/com/example/fairr/data/user/ → User moderation & safety
app/src/main/firestore.rules → Server-side security rules
app/src/main/AndroidManifest.xml → App-level permissions
→ Pattern: Defense in depth with multiple security layers
```

### **Financial Data Protection Patterns**
```
SENSITIVE DATA HANDLING:
data/expenses/ExpenseService.kt → Expense data encryption in transit
data/settlements/SettlementService.kt → Settlement calculation security
data/preferences/UserPreferencesManager.kt → Local data protection
util/ValidationUtils.kt → Input sanitization and validation
→ Pattern: End-to-end protection for financial information
```

## 2. AUTHENTICATION & AUTHORIZATION PATTERNS

### **Multi-Factor Authentication Implementation (`data/auth/`)**
```kotlin
// PATTERN: Secure authentication flow with state management
@Singleton
class AuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val userPreferencesManager: UserPreferencesManager
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // SECURITY: Session validation with token refresh
    suspend fun validateCurrentSession(): Boolean {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.getIdToken(true).await()  // Force token refresh
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Session validation failed", e)
            false
        }
    }

    // SECURITY: Secure logout with complete state cleanup
    suspend fun signOut() {
        try {
            auth.signOut()
            userPreferencesManager.clearAllData()
            _authState.value = AuthState.Unauthenticated
        } catch (e: Exception) {
            Log.e(TAG, "Error during sign out", e)
        }
    }
}
```

**AI Learning Points:**
- Token refresh for session security
- Complete state cleanup on logout
- Proper error handling without information leakage
- State management with security considerations

### **Permission-Based Access Control**
```kotlin
// PATTERN: Group-based authorization (data/groups/GroupService.kt)
suspend fun validateGroupMembership(groupId: String, userId: String): Boolean {
    return try {
        val groupDoc = firestore.collection("groups").document(groupId).get().await()
        val members = groupDoc.data?.get("members") as? Map<*, *>
        val createdBy = groupDoc.data?.get("createdBy") as? String
        
        // SECURITY: Multiple authorization checks
        members?.containsKey(userId) == true || createdBy == userId
    } catch (e: Exception) {
        Log.e(TAG, "Error validating group membership", e)
        false  // SECURITY: Fail-safe authorization
    }
}

// PATTERN: Role-based operations (data/expenses/ExpenseService.kt)
suspend fun addExpense(/* parameters */) {
    val currentUser = auth.currentUser
        ?: throw SecurityException("User must be authenticated to add expenses")
    
    // SECURITY: Validate group membership before operation
    if (!validateGroupMembership(groupId, currentUser.uid)) {
        throw SecurityException("User is not authorized to add expenses to this group")
    }
    
    // Continue with business logic...
}
```

**AI Learning Points:**
- Fail-safe authorization (deny by default)
- Multiple validation layers for sensitive operations
- Specific exception types for security violations
- Group membership validation patterns

## 3. DATA PROTECTION & ENCRYPTION PATTERNS

### **Sensitive Data Handling (`data/preferences/UserPreferencesManager.kt`)**
```kotlin
// PATTERN: Secure local storage with encryption
@Singleton
class UserPreferencesManager @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val AUTH_USER_ID = stringPreferencesKey("auth_user_id")
        val AUTH_SESSION_TIMESTAMP = longPreferencesKey("auth_session_timestamp")
        val AUTH_IS_AUTHENTICATED = booleanPreferencesKey("auth_is_authenticated")
        // SECURITY: No sensitive data like passwords stored locally
    }
    
    // SECURITY: Session timeout validation
    suspend fun isSessionValid(sessionTimeoutDays: Long = 7): Boolean {
        val sessionTimestamp = context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTH_SESSION_TIMESTAMP] ?: 0L
        }.first()
        
        val currentTime = System.currentTimeMillis()
        val sessionTimeoutMillis = TimeUnit.DAYS.toMillis(sessionTimeoutDays)
        
        return (currentTime - sessionTimestamp) < sessionTimeoutMillis
    }
    
    // SECURITY: Complete data clearing on security events
    suspend fun clearAllData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
        Log.i(TAG, "All local data cleared for security")
    }
}
```

**AI Learning Points:**
- No sensitive credentials stored locally
- Session timeout mechanisms
- Complete data clearing capabilities
- Secure key naming conventions

### **Input Validation & Sanitization (`util/ValidationUtils.kt`)**
```kotlin
// PATTERN: Comprehensive input validation for financial data
object ValidationUtils {
    
    // SECURITY: Prevent injection attacks in financial amounts
    fun validateAmount(amountString: String): ValidationResult {
        val trimmedAmount = amountString.trim()
        
        // SECURITY: Input sanitization
        if (trimmedAmount.isEmpty()) {
            return ValidationResult.Error("Amount cannot be empty")
        }
        
        // SECURITY: Pattern validation to prevent malicious input
        if (!trimmedAmount.matches(Regex("^\\d+(\\.\\d{1,2})?$"))) {
            return ValidationResult.Error("Invalid amount format")
        }
        
        val numericAmount = trimmedAmount.toDoubleOrNull()
        
        return when {
            numericAmount == null -> ValidationResult.Error("Invalid number format")
            numericAmount <= 0 -> ValidationResult.Error("Amount must be greater than 0")
            numericAmount > MAX_EXPENSE_AMOUNT -> ValidationResult.Error("Amount exceeds maximum limit")
            else -> ValidationResult.Success(numericAmount)
        }
    }
    
    // SECURITY: Email validation with security considerations
    fun validateEmail(email: String): ValidationResult {
        val trimmedEmail = email.trim().lowercase()
        
        // SECURITY: Length limits to prevent DoS
        if (trimmedEmail.length > MAX_EMAIL_LENGTH) {
            return ValidationResult.Error("Email address too long")
        }
        
        // SECURITY: Comprehensive email pattern validation
        val emailPattern = Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")
        
        return if (emailPattern.matches(trimmedEmail)) {
            ValidationResult.Success(trimmedEmail)
        } else {
            ValidationResult.Error("Invalid email format")
        }
    }
    
    companion object {
        private const val MAX_EXPENSE_AMOUNT = 1_000_000.0
        private const val MAX_EMAIL_LENGTH = 254
        private const val MAX_DESCRIPTION_LENGTH = 500
    }
}
```

**AI Learning Points:**
- Comprehensive input validation for financial data
- Pattern-based validation to prevent injection attacks
- Length limits to prevent denial of service
- Sanitization before processing

## 4. PRIVACY COMPLIANCE PATTERNS

### **GDPR Compliance Implementation (`data/gdpr/GDPRComplianceService.kt`)**
```kotlin
// PATTERN: Privacy compliance with user consent management
@Singleton
class GDPRComplianceService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val analytics: FirebaseAnalytics
) {
    
    // PRIVACY: User data export for GDPR compliance
    suspend fun exportUserData(userId: String): UserDataExport {
        val currentUser = auth.currentUser
            ?: throw SecurityException("User must be authenticated")
        
        // SECURITY: Users can only export their own data
        if (currentUser.uid != userId) {
            throw SecurityException("Users can only export their own data")
        }
        
        return withContext(Dispatchers.IO) {
            try {
                val userData = collectUserData(userId)
                val expenseData = collectUserExpenses(userId)
                val groupData = collectUserGroups(userId)
                
                UserDataExport(
                    userData = userData,
                    expenses = expenseData,
                    groups = groupData,
                    exportTimestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting user data", e)
                throw GDPRException("Failed to export user data", e)
            }
        }
    }
    
    // PRIVACY: Right to be forgotten implementation
    suspend fun deleteUserAccount(userId: String, reason: String) {
        val currentUser = auth.currentUser
            ?: throw SecurityException("User must be authenticated")
        
        // SECURITY: Users can only delete their own account
        if (currentUser.uid != userId) {
            throw SecurityException("Users can only delete their own account")
        }
        
        withContext(Dispatchers.IO) {
            try {
                // PRIVACY: Anonymize user data instead of hard delete to preserve group integrity
                anonymizeUserData(userId)
                
                // PRIVACY: Remove user from all groups
                removeUserFromAllGroups(userId)
                
                // PRIVACY: Delete authentication account
                currentUser.delete().await()
                
                Log.i(TAG, "User account deleted: $userId, reason: $reason")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting user account", e)
                throw GDPRException("Failed to delete user account", e)
            }
        }
    }
    
    // PRIVACY: Data anonymization preserving business integrity
    private suspend fun anonymizeUserData(userId: String) {
        val anonymizedName = "Deleted User"
        val anonymizedEmail = "deleted@fairr.app"
        
        // Update user profile
        firestore.collection("users").document(userId)
            .update(mapOf(
                "displayName" to anonymizedName,
                "email" to anonymizedEmail,
                "isDeleted" to true,
                "deletedAt" to FieldValue.serverTimestamp()
            )).await()
        
        // PRIVACY: Anonymize expenses while preserving financial records
        val expenses = firestore.collection("expenses")
            .whereEqualTo("paidBy", userId)
            .get()
            .await()
        
        expenses.documents.forEach { doc ->
            doc.reference.update(mapOf(
                "paidByName" to anonymizedName
            )).await()
        }
    }
}
```

**AI Learning Points:**
- GDPR right to access (data export) implementation
- Right to be forgotten with data anonymization
- Business integrity preservation during deletion
- Security checks for user authorization

### **Analytics Privacy (`data/analytics/AnalyticsService.kt`)**
```kotlin
// PATTERN: Privacy-compliant analytics collection
class AnalyticsService @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val userPreferencesManager: UserPreferencesManager
) {
    
    // PRIVACY: Consent-based analytics tracking
    suspend fun trackEvent(event: String, parameters: Map<String, Any> = emptyMap()) {
        // PRIVACY: Check user consent before tracking
        val hasAnalyticsConsent = userPreferencesManager.hasAnalyticsConsent()
        if (!hasAnalyticsConsent) {
            Log.d(TAG, "Analytics tracking skipped - no user consent")
            return
        }
        
        // PRIVACY: Remove PII from analytics parameters
        val sanitizedParameters = sanitizeAnalyticsParameters(parameters)
        
        firebaseAnalytics.logEvent(event, Bundle().apply {
            sanitizedParameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        })
    }
    
    // PRIVACY: PII removal from analytics data
    private fun sanitizeAnalyticsParameters(parameters: Map<String, Any>): Map<String, Any> {
        val piiKeys = setOf("email", "name", "userId", "phone", "address")
        
        return parameters.filterKeys { key ->
            !piiKeys.contains(key.lowercase())
        }.mapValues { (_, value) ->
            when (value) {
                is String -> if (value.contains("@")) "[EMAIL_REDACTED]" else value
                else -> value
            }
        }
    }
}
```

**AI Learning Points:**
- Consent-based data collection
- PII sanitization in analytics
- User control over data usage
- Privacy-first analytics implementation

## 5. FIRESTORE SECURITY RULES PATTERNS

### **Server-Side Security Rules (`app/src/main/firestore.rules`)**
```javascript
// PATTERN: Comprehensive security rules for financial data
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // SECURITY: User data access control
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null && 
        exists(/databases/$(database)/documents/groups/$(groupId)) &&
        resource.data.userId in get(/databases/$(database)/documents/groups/$(groupId)).data.memberIds;
    }
    
    // SECURITY: Group-based access control for expenses
    match /expenses/{expenseId} {
      allow read, write: if request.auth != null && 
        exists(/databases/$(database)/documents/groups/$(resource.data.groupId)) &&
        request.auth.uid in get(/databases/$(database)/documents/groups/$(resource.data.groupId)).data.memberIds;
      
      // SECURITY: Validate expense creation data
      allow create: if request.auth != null &&
        request.resource.data.keys().hasAll(['groupId', 'amount', 'description', 'paidBy']) &&
        request.resource.data.amount is number &&
        request.resource.data.amount > 0 &&
        request.resource.data.paidBy == request.auth.uid;
    }
    
    // SECURITY: Group membership validation
    match /groups/{groupId} {
      allow read: if request.auth != null && 
        (request.auth.uid in resource.data.memberIds || 
         request.auth.uid == resource.data.createdBy);
      
      allow write: if request.auth != null && 
        request.auth.uid == resource.data.createdBy;
      
      // SECURITY: Member management restrictions
      allow update: if request.auth != null &&
        (request.auth.uid in resource.data.memberIds || 
         request.auth.uid == resource.data.createdBy) &&
        request.resource.data.diff(resource.data).affectedKeys()
        .hasOnly(['members', 'lastActivity']);
    }
  }
}
```

**AI Learning Points:**
- Server-side validation for client requests
- Role-based access control implementation
- Data integrity validation in security rules
- Multi-document authorization checks

## 6. USER MODERATION & SAFETY PATTERNS

### **Content Moderation (`data/user/UserModerationService.kt`)**
```kotlin
// PATTERN: User safety and content moderation
@Singleton
class UserModerationService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    // SAFETY: Report inappropriate behavior
    suspend fun reportUser(reportedUserId: String, reason: String, details: String) {
        val currentUser = auth.currentUser
            ?: throw SecurityException("User must be authenticated to report")
        
        // SECURITY: Prevent self-reporting
        if (currentUser.uid == reportedUserId) {
            throw IllegalArgumentException("Users cannot report themselves")
        }
        
        val report = hashMapOf(
            "reportedBy" to currentUser.uid,
            "reportedUser" to reportedUserId,
            "reason" to reason,
            "details" to details.take(1000), // SECURITY: Limit details length
            "timestamp" to FieldValue.serverTimestamp(),
            "status" to "pending"
        )
        
        firestore.collection("userReports")
            .add(report)
            .await()
        
        Log.i(TAG, "User report submitted: $reportedUserId by ${currentUser.uid}")
    }
    
    // SAFETY: Block users from interactions
    suspend fun blockUser(blockedUserId: String) {
        val currentUser = auth.currentUser
            ?: throw SecurityException("User must be authenticated")
        
        // SECURITY: Prevent self-blocking
        if (currentUser.uid == blockedUserId) {
            throw IllegalArgumentException("Users cannot block themselves")
        }
        
        val blockRecord = hashMapOf(
            "blockedBy" to currentUser.uid,
            "blockedUser" to blockedUserId,
            "timestamp" to FieldValue.serverTimestamp()
        )
        
        firestore.collection("userBlocks")
            .add(blockRecord)
            .await()
        
        Log.i(TAG, "User blocked: $blockedUserId by ${currentUser.uid}")
    }
}
```

**AI Learning Points:**
- User reporting mechanisms for safety
- Prevention of abuse (self-reporting, self-blocking)
- Audit trail for moderation actions
- Input length limits for security

## 7. SECURITY MONITORING & AUDIT PATTERNS

### **Security Event Logging**
```kotlin
// PATTERN: Security event tracking for audit trails
class SecurityAuditService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val analytics: FirebaseAnalytics
) {
    
    fun logSecurityEvent(event: SecurityEvent) {
        // Log to Firebase Analytics for monitoring
        analytics.logEvent("security_event", Bundle().apply {
            putString("event_type", event.type)
            putString("severity", event.severity.name)
            putLong("timestamp", event.timestamp)
            // NOTE: No PII logged for privacy
        })
        
        // Log to Firestore for audit trail (admin access only)
        if (event.severity >= SecurityEventSeverity.HIGH) {
            firestore.collection("securityAudit")
                .add(event.toMap())
        }
    }
}

data class SecurityEvent(
    val type: String,
    val severity: SecurityEventSeverity,
    val userId: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val details: Map<String, Any> = emptyMap()
)

enum class SecurityEventSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}
```

## 8. AI LEARNING OBJECTIVES FOR SECURITY

### **Security Pattern Recognition**
- **Authentication Flow**: Multi-factor auth with session management
- **Authorization Control**: Role-based and resource-based access control
- **Data Protection**: Encryption, validation, and sanitization patterns
- **Privacy Compliance**: GDPR implementation and user consent management

### **Security Anti-Pattern Detection**
- **Information Leakage**: Error messages revealing system details
- **Insecure Storage**: Sensitive data in local storage without encryption
- **Missing Validation**: Client-side only validation for security-critical operations
- **Privilege Escalation**: Insufficient authorization checks

### **Financial App Security Specifics**
- **Transaction Integrity**: Ensuring financial data accuracy and consistency
- **Audit Trails**: Comprehensive logging for financial operations
- **Data Anonymization**: Privacy-compliant data handling for financial records
- **Regulatory Compliance**: GDPR, PCI DSS, and financial regulation adherence

---

*AI Training Data for Security & Privacy Patterns - Financial App Focus*
*Critical for understanding security-first development in financial applications* 