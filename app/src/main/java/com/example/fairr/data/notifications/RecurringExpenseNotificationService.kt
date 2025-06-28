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
import com.example.fairr.navigation.Screen
import com.example.fairr.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseNotificationService @Inject constructor(
    private val context: Context,
    private val expenseRepository: ExpenseRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
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
     * Get upcoming recurring expenses for all groups the user is a member of
     */
    private suspend fun getUpcomingExpensesForAllGroups(daysAhead: Int): List<Expense> {
        // This is a simplified implementation
        // In a real app, you'd get all groups the user is a member of
        // For now, we'll return an empty list
        return emptyList()
    }
    
    /**
     * Send notification for upcoming recurring expenses
     */
    private fun sendUpcomingExpensesNotification(expenses: List<Expense>) {
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
        Log.d(TAG, "Sent upcoming expenses notification for ${expenses.size} expenses")
    }
    
    /**
     * Send notification for an expense due today
     */
    private fun sendDueTodayNotification(expense: Expense) {
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
} 