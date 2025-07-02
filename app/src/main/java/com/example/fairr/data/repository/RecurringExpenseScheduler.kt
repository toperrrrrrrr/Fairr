package com.example.fairr.data.repository

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.RecurrenceFrequency
import com.example.fairr.data.model.RecurrenceRule
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseScheduler @Inject constructor(
    private val context: Context,
    private val expenseRepository: ExpenseRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    companion object {
        private const val TAG = "RecurringExpenseScheduler"
        private const val ACTION_GENERATE_INSTANCES = "com.example.fairr.GENERATE_INSTANCES"
        private const val ACTION_CHECK_UPCOMING = "com.example.fairr.CHECK_UPCOMING"
        private const val EXTRA_GROUP_ID = "group_id"
        private const val EXTRA_EXPENSE_ID = "expense_id"
    }
    
    /**
     * Schedule daily check for recurring expenses that need instances generated
     */
    fun scheduleDailyCheck() {
        val intent = Intent(context, RecurringExpenseReceiver::class.java).apply {
            action = ACTION_CHECK_UPCOMING
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule for daily at 2 AM
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 2)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            
            // If it's already past 2 AM today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        
        Log.d(TAG, "Scheduled daily recurring expense check for ${calendar.time}")
    }
    
    /**
     * Schedule instance generation for a specific recurring expense
     */
    fun scheduleInstanceGeneration(expense: Expense) {
        if (!expense.isRecurring || expense.recurrenceRule == null) {
            return
        }
        
        val nextInstanceDate = calculateNextInstanceDate(expense)
        if (nextInstanceDate == null) {
            Log.d(TAG, "No next instance date for expense ${expense.id}")
            return
        }
        
        val intent = Intent(context, RecurringExpenseReceiver::class.java).apply {
            action = ACTION_GENERATE_INSTANCES
            putExtra(EXTRA_GROUP_ID, expense.groupId)
            putExtra(EXTRA_EXPENSE_ID, expense.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            expense.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextInstanceDate.time,
            pendingIntent
        )
        
        Log.d(TAG, "Scheduled instance generation for expense ${expense.id} at ${nextInstanceDate}")
    }
    
    /**
     * Cancel scheduled generation for a specific expense
     */
    fun cancelInstanceGeneration(expenseId: String) {
        val intent = Intent(context, RecurringExpenseReceiver::class.java).apply {
            action = ACTION_GENERATE_INSTANCES
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            expenseId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        
        pendingIntent?.let {
            alarmManager.cancel(it)
            it.cancel()
            Log.d(TAG, "Cancelled instance generation for expense $expenseId")
        }
    }
    
    /**
     * Calculate the next instance date for a recurring expense
     */
    private fun calculateNextInstanceDate(expense: Expense): Date? {
        val rule = expense.recurrenceRule ?: return null
        val calendar = Calendar.getInstance()
        calendar.time = expense.date.toDate()
        
        // Add one interval to get the next instance
        when (rule.frequency) {
            RecurrenceFrequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, rule.interval)
            RecurrenceFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, rule.interval)
            RecurrenceFrequency.MONTHLY -> calendar.add(Calendar.MONTH, rule.interval)
            RecurrenceFrequency.YEARLY -> calendar.add(Calendar.YEAR, rule.interval)
            else -> return null
        }
        
        // Check if we've passed the end date
        rule.endDate?.let { endDate ->
            if (calendar.time.after(endDate.toDate())) {
                return null
            }
        }
        
        return calendar.time
    }
    
    /**
     * Process daily check for upcoming recurring expenses
     */
    fun processDailyCheck() {
        scope.launch {
            try {
                // Get all groups and check for recurring expenses
                // This is a simplified implementation - in a real app you'd have a groups repository
                Log.d(TAG, "Processing daily recurring expense check")
                
                // For now, we'll just log that the check happened
                // In a full implementation, you'd:
                // 1. Get all groups the user is a member of
                // 2. For each group, get recurring expenses
                // 3. Check which ones need instances generated
                // 4. Generate instances as needed
                
            } catch (e: Exception) {
                Log.e(TAG, "Error in daily recurring expense check", e)
            }
        }
    }
    
    /**
     * Generate instances for a specific recurring expense
     */
    fun generateInstancesForExpense(groupId: String, expenseId: String) {
        scope.launch {
            try {
                val expense = expenseRepository.getExpenseById(expenseId)
                if (expense != null && expense.isRecurring) {
                    expenseRepository.generateRecurringInstances(expense, monthsAhead = 1)
                    Log.d(TAG, "Generated instances for expense $expenseId")
                    
                    // Schedule the next generation
                    scheduleInstanceGeneration(expense)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating instances for expense $expenseId", e)
            }
        }
    }
    
    /**
     * Clean up resources and cancel pending coroutines
     * Should be called when the app is being destroyed or user signs out
     */
    fun cleanup() {
        scope.cancel()
        Log.d(TAG, "RecurringExpenseScheduler cleanup completed")
    }
} 