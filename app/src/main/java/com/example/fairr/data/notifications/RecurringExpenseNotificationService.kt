package com.example.fairr.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fairr.MainActivity
import com.example.fairr.R
import com.example.fairr.data.model.Expense
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.groups.GroupService
import com.example.fairr.navigation.Screen
import com.example.fairr.util.CurrencyFormatter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Singleton
class RecurringExpenseNotificationService @Inject constructor(
    private val context: Context,
    private val expenseRepository: ExpenseRepository,
    private val groupService: GroupService,
    private val auth: FirebaseAuth
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        private const val TAG = "RecurringExpenseNotification"
        private const val CHANNEL_ID = "recurring_expenses"
        private const val CHANNEL_NAME = "Recurring Expenses"
        private const val CHANNEL_DESCRIPTION = "Notifications for upcoming recurring expenses"
        private const val NOTIFICATION_ID_BASE = 1000
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Check for upcoming recurring expenses and send notifications
     */
    fun checkUpcomingExpenses() {
        scope.launch {
            try {
                // Get upcoming recurring expenses for the next 7 days
                val upcomingExpenses = getUpcomingExpensesForAllGroups(7)
                
                if (upcomingExpenses.isNotEmpty()) {
                    sendUpcomingExpensesNotification(upcomingExpenses)
                }
                
                // Check for expenses due today
                val todayExpenses = getUpcomingExpensesForAllGroups(1)
                todayExpenses.forEach { expense ->
                    sendDueTodayNotification(expense)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error checking upcoming expenses", e)
            }
        }
    }
    
    /**
     * Manually trigger notification check (called from MainActivity or other parts of the app)
     */
    fun triggerNotificationCheck() {
        Log.d(TAG, "Triggering notification check")
        checkUpcomingExpenses()
    }
    
    /**
     * Check if we should show notifications (avoid spam by checking last notification time)
     */
    private fun shouldShowNotification(): Boolean {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val lastNotificationTime = prefs.getLong("last_notification_time", 0)
        val currentTime = System.currentTimeMillis()
        
        // Only show notifications if it's been more than 1 hour since the last one
        val oneHour = 60 * 60 * 1000L
        return (currentTime - lastNotificationTime) > oneHour
    }
    
    /**
     * Update the last notification time
     */
    private fun updateLastNotificationTime() {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        prefs.edit().putLong("last_notification_time", System.currentTimeMillis()).apply()
    }
    
    /**
     * Get upcoming recurring expenses for all groups the user is a member of
     */
    private suspend fun getUpcomingExpensesForAllGroups(daysAhead: Int): List<Expense> {
        val currentUserId = auth.currentUser?.uid ?: return emptyList()
        
        try {
            // Get all groups the user is a member of
            val userGroups = groupService.getUserGroups().first()
            val upcomingExpenses = mutableListOf<Expense>()
            
            // Calculate the target date range
            val calendar = Calendar.getInstance()
            val startDate = calendar.time
            calendar.add(Calendar.DAY_OF_YEAR, daysAhead)
            val endDate = calendar.time
            
            // Get recurring expenses from each group
            userGroups.forEach { group ->
                try {
                    val groupExpenses = expenseRepository.getExpensesByGroupId(group.id)
                    
                    // Filter for recurring expenses that fall within the date range
                    val recurringExpenses = groupExpenses.filter { expense ->
                        expense.isRecurring && 
                        expense.recurrenceRule != null &&
                        expense.date.toDate().after(startDate) && 
                        expense.date.toDate().before(endDate)
                    }
                    
                    upcomingExpenses.addAll(recurringExpenses)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching expenses for group ${group.id}", e)
                }
            }
            
            // Sort by date (earliest first)
            return upcomingExpenses.sortedBy { it.date }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting upcoming expenses for all groups", e)
            return emptyList()
        }
    }
    
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
    
    /**
     * Send notification for upcoming recurring expenses
     */
    private fun sendUpcomingExpensesNotification(expenses: List<Expense>) {
        if (!shouldShowNotification()) {
            Log.d(TAG, "Skipping notification due to spam prevention")
            return
        }
        if (!hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "recurring_expenses")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Upcoming Recurring Expenses")
            .setContentText("You have ${expenses.size} recurring expenses coming up")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                buildUpcomingExpensesText(expenses)
            ))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE, notification)
        updateLastNotificationTime()
        Log.d(TAG, "Sent upcoming expenses notification for ${expenses.size} expenses")
    }
    
    /**
     * Send notification for an expense due today
     */
    private fun sendDueTodayNotification(expense: Expense) {
        if (!shouldShowNotification()) {
            Log.d(TAG, "Skipping due today notification due to spam prevention")
            return
        }
        if (!hasNotificationPermission()) {
            Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping notification.")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "group_detail")
            putExtra("group_id", expense.groupId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            expense.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recurring Expense Due Today")
            .setContentText("${expense.description} - ${CurrencyFormatter.format(expense.currency, expense.amount)}")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "${expense.description} is due today in ${expense.groupId}\n" +
                "Amount: ${CurrencyFormatter.format(expense.currency, expense.amount)}"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_BASE + expense.id.hashCode(), notification)
        updateLastNotificationTime()
        Log.d(TAG, "Sent due today notification for expense ${expense.id}")
    }
    
    /**
     * Build text for upcoming expenses notification
     */
    private fun buildUpcomingExpensesText(expenses: List<Expense>): String {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        return expenses.take(3).joinToString("\n") { expense ->
            "${expense.description} - ${CurrencyFormatter.format(expense.currency, expense.amount)} " +
            "(${dateFormat.format(expense.date.toDate())})"
        }.let { text ->
            if (expenses.size > 3) {
                "$text\n... and ${expenses.size - 3} more"
            } else {
                text
            }
        }
    }
    
    /**
     * Cancel all recurring expense notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
        Log.d(TAG, "Cancelled all recurring expense notifications")
    }
    
    /**
     * Cancel notification for a specific expense
     */
    fun cancelExpenseNotification(expenseId: String) {
        notificationManager.cancel(NOTIFICATION_ID_BASE + expenseId.hashCode())
        Log.d(TAG, "Cancelled notification for expense $expenseId")
    }
    
    /**
     * Clean up resources and cancel pending coroutines
     * Should be called when the app is being destroyed or user signs out
     */
    fun cleanup() {
        scope.cancel()
        Log.d(TAG, "RecurringExpenseNotificationService cleanup completed")
    }
} 