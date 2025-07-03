package com.example.fairr.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.Group
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.*
import kotlin.system.measureTimeMillis

/**
 * Comprehensive Analytics Service
 * Handles user behavior tracking, performance monitoring, business intelligence, and crash reporting
 */
@Singleton
class AnalyticsService @Inject constructor(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics,
    private val performance: FirebasePerformance,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "AnalyticsService"
        
        // Custom Events
        const val EVENT_EXPENSE_CREATED = "expense_created"
        const val EVENT_EXPENSE_UPDATED = "expense_updated"
        const val EVENT_EXPENSE_DELETED = "expense_deleted"
        const val EVENT_GROUP_CREATED = "group_created"
        const val EVENT_GROUP_JOINED = "group_joined"
        const val EVENT_GROUP_LEFT = "group_left"
        const val EVENT_SETTLEMENT_RECORDED = "settlement_recorded"
        const val EVENT_RECEIPT_SCANNED = "receipt_scanned"
        const val EVENT_INVITE_SENT = "invite_sent"
        const val EVENT_EXPORT_DATA = "export_data"
        const val EVENT_SCREEN_VIEW = "screen_view"
        const val EVENT_FEATURE_USED = "feature_used"
        const val EVENT_ERROR_OCCURRED = "error_occurred"
        const val EVENT_PERFORMANCE_ISSUE = "performance_issue"
        
        // User Properties
        const val PROPERTY_USER_TYPE = "user_type"
        const val PROPERTY_TOTAL_GROUPS = "total_groups"
        const val PROPERTY_TOTAL_EXPENSES = "total_expenses"
        const val PROPERTY_PRIMARY_CURRENCY = "primary_currency"
        const val PROPERTY_APP_VERSION = "app_version"
        const val PROPERTY_DEVICE_TYPE = "device_type"
        
        // Performance Traces
        const val TRACE_APP_STARTUP = "app_startup"
        const val TRACE_EXPENSE_LOAD = "expense_load"
        const val TRACE_GROUP_LOAD = "group_load"
        const val TRACE_SETTLEMENT_CALC = "settlement_calculation"
        const val TRACE_IMAGE_PROCESSING = "image_processing"
        const val TRACE_DATA_SYNC = "data_sync"
    }

    init {
        setupAnalytics()
    }

    private fun setupAnalytics() {
        // Set user properties
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firebaseAnalytics.setUserId(userId)
            crashlytics.setUserId(userId)
        }
    }

    // ==================== User Behavior Tracking ====================

    /**
     * Track screen views for user navigation analysis
     */
    fun trackScreenView(screenName: String, screenClass: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
            }
            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            
            Log.d(TAG, "Screen view tracked: $screenName")
        } catch (e: Exception) {
            logError("Failed to track screen view", e)
        }
    }

    /**
     * Track expense-related events for business intelligence
     */
    fun trackExpenseEvent(event: String, expense: Expense) {
        try {
            val bundle = Bundle().apply {
                putString("expense_id", expense.id)
                putString("group_id", expense.groupId)
                putDouble("amount", expense.amount)
                putString("currency", expense.currency)
                putString("category", expense.category.name)
                putBoolean("has_receipt", expense.attachments.isNotEmpty())
                putInt("split_count", expense.splitBetween.size)
            }
            firebaseAnalytics.logEvent(event, bundle)
            
            // Track monetary value for revenue analysis
            if (event == EVENT_EXPENSE_CREATED) {
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.PURCHASE, Bundle().apply {
                    putString(FirebaseAnalytics.Param.CURRENCY, expense.currency)
                    putDouble(FirebaseAnalytics.Param.VALUE, expense.amount)
                    putString(FirebaseAnalytics.Param.ITEM_CATEGORY, expense.category.name)
                })
            }
            
            Log.d(TAG, "Expense event tracked: $event for amount ${expense.amount}")
        } catch (e: Exception) {
            logError("Failed to track expense event", e)
        }
    }

    /**
     * Track group-related events for social features analysis
     */
    fun trackGroupEvent(event: String, group: Group, additionalData: Map<String, Any> = emptyMap()) {
        try {
            val bundle = Bundle().apply {
                putString("group_id", group.id)
                putString("group_name", group.name)
                putInt("member_count", group.members.size)
                putString("currency", group.currency)
                
                additionalData.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                    }
                }
            }
            firebaseAnalytics.logEvent(event, bundle)
            
            Log.d(TAG, "Group event tracked: $event for group ${group.name}")
        } catch (e: Exception) {
            logError("Failed to track group event", e)
        }
    }

    /**
     * Track feature usage for product insights
     */
    fun trackFeatureUsage(featureName: String, parameters: Map<String, Any> = emptyMap()) {
        try {
            val bundle = Bundle().apply {
                putString("feature_name", featureName)
                putLong("timestamp", System.currentTimeMillis())
                
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                    }
                }
            }
            firebaseAnalytics.logEvent(EVENT_FEATURE_USED, bundle)
            
            Log.d(TAG, "Feature usage tracked: $featureName")
        } catch (e: Exception) {
            logError("Failed to track feature usage", e)
        }
    }

    /**
     * Track settlement events for financial insights
     */
    fun trackSettlementEvent(
        groupId: String,
        amount: Double,
        currency: String,
        paymentMethod: String,
        participantCount: Int
    ) {
        try {
            val bundle = Bundle().apply {
                putString("group_id", groupId)
                putDouble("amount", amount)
                putString("currency", currency)
                putString("payment_method", paymentMethod)
                putInt("participant_count", participantCount)
            }
            firebaseAnalytics.logEvent(EVENT_SETTLEMENT_RECORDED, bundle)
            
            Log.d(TAG, "Settlement event tracked: $amount $currency")
        } catch (e: Exception) {
            logError("Failed to track settlement event", e)
        }
    }

    // ==================== Performance Monitoring ====================

    /**
     * Start a performance trace
     */
    fun startTrace(traceName: String): Trace? {
        return try {
            val trace = performance.newTrace(traceName)
            trace.start()
            Log.d(TAG, "Performance trace started: $traceName")
            trace
        } catch (e: Exception) {
            logError("Failed to start trace", e)
            null
        }
    }

    /**
     * Stop a performance trace
     */
    fun stopTrace(trace: Trace?, attributes: Map<String, String> = emptyMap()) {
        try {
            trace?.let {
                attributes.forEach { (key, value) ->
                    it.putAttribute(key, value)
                }
                it.stop()
                Log.d(TAG, "Performance trace stopped")
            }
        } catch (e: Exception) {
            logError("Failed to stop trace", e)
        }
    }

    /**
     * Measure and track method execution time
     */
    suspend fun <T> measurePerformance(
        operationName: String,
        attributes: Map<String, String> = emptyMap(),
        operation: suspend () -> T
    ): T {
        val trace = startTrace(operationName)
        return try {
            val result: T
            val executionTime = measureTimeMillis {
                result = operation()
            }
            
            // Add execution time as attribute
            val allAttributes = attributes.toMutableMap()
            allAttributes["execution_time_ms"] = executionTime.toString()
            
            // Track performance issues if operation is slow
            if (executionTime > 5000) { // 5 seconds threshold
                trackPerformanceIssue(operationName, executionTime, "slow_operation")
            }
            
            stopTrace(trace, allAttributes)
            result
        } catch (e: Exception) {
            val errorAttributes = attributes.toMutableMap()
            errorAttributes["error"] = e.message ?: "unknown"
            stopTrace(trace, errorAttributes)
            throw e
        }
    }

    /**
     * Track performance issues
     */
    private fun trackPerformanceIssue(
        operation: String,
        duration: Long,
        issueType: String
    ) {
        try {
            val bundle = Bundle().apply {
                putString("operation", operation)
                putLong("duration_ms", duration)
                putString("issue_type", issueType)
                putLong("timestamp", System.currentTimeMillis())
            }
            firebaseAnalytics.logEvent(EVENT_PERFORMANCE_ISSUE, bundle)
            
            // Also log to Crashlytics for performance monitoring
            crashlytics.log("Performance issue: $operation took ${duration}ms")
            
            Log.w(TAG, "Performance issue tracked: $operation took ${duration}ms")
        } catch (e: Exception) {
            logError("Failed to track performance issue", e)
        }
    }

    // ==================== Business Intelligence ====================

    /**
     * Update user properties for segmentation
     */
    suspend fun updateUserProperties() = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext
            
            // Get user statistics
            val userStats = getUserStatistics(userId)
            
            // Set user properties
            firebaseAnalytics.setUserProperty(PROPERTY_TOTAL_GROUPS, userStats.totalGroups.toString())
            firebaseAnalytics.setUserProperty(PROPERTY_TOTAL_EXPENSES, userStats.totalExpenses.toString())
            firebaseAnalytics.setUserProperty(PROPERTY_PRIMARY_CURRENCY, userStats.primaryCurrency)
            firebaseAnalytics.setUserProperty(PROPERTY_USER_TYPE, userStats.userType)
            
            Log.d(TAG, "User properties updated: ${userStats.totalGroups} groups, ${userStats.totalExpenses} expenses")
        } catch (e: Exception) {
            logError("Failed to update user properties", e)
        }
    }

    /**
     * Get user statistics for business intelligence
     */
    private suspend fun getUserStatistics(userId: String): UserStatistics {
        return try {
            // Get user's groups - need to query based on how groups store member data
            val groupsSnapshot = firestore.collection("groups")
                .get() // We'll filter client-side since members structure might vary
                .await()
            
            val userGroups = groupsSnapshot.documents.filter { doc ->
                val members = doc.get("members") as? List<*>
                members?.any { member ->
                    when (member) {
                        is Map<*, *> -> member["userId"] == userId
                        else -> false
                    }
                } == true
            }
            
            // Get user's expenses
            val expensesSnapshot = firestore.collection("expenses")
                .whereEqualTo("paidBy", userId)
                .get()
                .await()
            
            // Analyze currencies
            val currencies = expensesSnapshot.documents.mapNotNull { 
                it.getString("currency") 
            }
            val primaryCurrency = currencies.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: "USD"
            
            // Determine user type based on usage
            val totalExpenses = expensesSnapshot.size()
            val totalGroups = userGroups.size
            val userType = when {
                totalExpenses > 100 -> "power_user"
                totalExpenses > 20 -> "active_user"
                totalExpenses > 5 -> "regular_user"
                else -> "new_user"
            }
            
            UserStatistics(
                totalGroups = totalGroups,
                totalExpenses = totalExpenses,
                primaryCurrency = primaryCurrency,
                userType = userType
            )
        } catch (e: Exception) {
            logError("Failed to get user statistics", e)
            UserStatistics()
        }
    }

    /**
     * Track conversion events for business metrics
     */
    fun trackConversion(event: String, value: Double = 0.0, currency: String = "USD") {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.CURRENCY, currency)
                putDouble(FirebaseAnalytics.Param.VALUE, value)
                putLong("timestamp", System.currentTimeMillis())
            }
            firebaseAnalytics.logEvent(event, bundle)
            
            Log.d(TAG, "Conversion tracked: $event with value $value $currency")
        } catch (e: Exception) {
            logError("Failed to track conversion", e)
        }
    }

    // ==================== Error and Crash Reporting ====================

    /**
     * Log errors for monitoring and debugging
     */
    fun logError(message: String, throwable: Throwable? = null) {
        try {
            Log.e(TAG, message, throwable)
            
            // Log to Crashlytics
            crashlytics.log(message)
            throwable?.let { crashlytics.recordException(it) }
            
            // Track as analytics event
            val bundle = Bundle().apply {
                putString("error_message", message)
                putString("error_type", throwable?.javaClass?.simpleName ?: "unknown")
                putLong("timestamp", System.currentTimeMillis())
            }
            firebaseAnalytics.logEvent(EVENT_ERROR_OCCURRED, bundle)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log error", e)
        }
    }

    /**
     * Set custom keys for crash reporting
     */
    fun setCrashKey(key: String, value: String) {
        try {
            crashlytics.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set crash key", e)
        }
    }

    /**
     * Record handled exceptions
     */
    fun recordException(throwable: Throwable, additionalInfo: Map<String, String> = emptyMap()) {
        try {
            // Add additional context
            additionalInfo.forEach { (key, value) ->
                crashlytics.setCustomKey(key, value)
            }
            
            crashlytics.recordException(throwable)
            
            Log.d(TAG, "Exception recorded: ${throwable.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record exception", e)
        }
    }

    // ==================== Analytics Dashboard Data ====================

    /**
     * Get analytics summary for dashboard
     */
    suspend fun getAnalyticsSummary(): AnalyticsSummary = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext AnalyticsSummary()
            
            val userStats = getUserStatistics(userId)
            
            // Get recent activity stats
            val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
            
            val recentExpensesSnapshot = firestore.collection("expenses")
                .whereEqualTo("paidBy", userId)
                .whereGreaterThan("date", com.google.firebase.Timestamp(Date(thirtyDaysAgo)))
                .get()
                .await()
            
            AnalyticsSummary(
                totalGroups = userStats.totalGroups,
                totalExpenses = userStats.totalExpenses,
                recentExpenses = recentExpensesSnapshot.size(),
                primaryCurrency = userStats.primaryCurrency,
                userType = userStats.userType,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            logError("Failed to get analytics summary", e)
            AnalyticsSummary()
        }
    }
}

/**
 * User statistics data class
 */
data class UserStatistics(
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val primaryCurrency: String = "USD",
    val userType: String = "new_user"
)

/**
 * Analytics summary data class
 */
data class AnalyticsSummary(
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val recentExpenses: Int = 0,
    val primaryCurrency: String = "USD",
    val userType: String = "new_user",
    val lastUpdated: Long = 0L
)