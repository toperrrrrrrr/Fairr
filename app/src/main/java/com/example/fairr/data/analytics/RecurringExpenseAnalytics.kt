package com.example.fairr.data.analytics

import android.util.Log
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.RecurrenceFrequency
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.repository.ExpenseQueryParams
import com.example.fairr.data.model.ExpenseCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseAnalytics @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    data class RecurringExpenseStats(
        val totalRecurringExpenses: Int,
        val totalRecurringAmount: Double,
        val averageRecurringAmount: Double,
        val recurringExpensesByCategory: Map<ExpenseCategory, Double>,
        val totalGeneratedInstances: Int = 0,
        val monthlyProjection: Double = 0.0,
        val yearlyProjection: Double = 0.0,
        val upcomingExpenses: List<Expense> = emptyList()
    )
    
    data class FrequencyBreakdown(
        val frequency: RecurrenceFrequency,
        val count: Int,
        val totalAmount: Double,
        val percentage: Double
    )
    
    data class CategoryBreakdown(
        val category: ExpenseCategory,
        val count: Int,
        val totalAmount: Double,
        val percentage: Double
    )
    
    data class MonthlyTrend(
        val month: String,
        val recurringAmount: Double,
        val generatedAmount: Double,
        val totalAmount: Double
    )
    
    /**
     * Get comprehensive statistics for recurring expenses in a group
     */
    suspend fun getRecurringExpenseStats(groupId: String): RecurringExpenseStats {
        val expenses = expenseRepository.getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                startDate = getStartOfMonth(),
                endDate = Date()
            )
        ).expenses

        return analyzeRecurringExpenses(expenses)
    }
    
    private fun analyzeRecurringExpenses(expenses: List<Expense>): RecurringExpenseStats {
        val recurringExpenses = expenses.filter { it.isRecurring }
        val totalRecurringAmount = recurringExpenses.sumOf { it.amount }
        val averageRecurringAmount = if (recurringExpenses.isNotEmpty()) {
            totalRecurringAmount / recurringExpenses.size
        } else {
            0.0
        }

        val monthlyProjection = calculateMonthlyProjection(recurringExpenses)
        val yearlyProjection = monthlyProjection * 12
        val totalGeneratedInstances = expenses.count { it.parentExpenseId != null }
        val upcomingExpenses = getUpcomingExpenses(recurringExpenses)

        return RecurringExpenseStats(
            totalRecurringExpenses = recurringExpenses.size,
            totalRecurringAmount = totalRecurringAmount,
            averageRecurringAmount = averageRecurringAmount,
            recurringExpensesByCategory = getRecurringExpensesByCategory(recurringExpenses),
            totalGeneratedInstances = totalGeneratedInstances,
            monthlyProjection = monthlyProjection,
            yearlyProjection = yearlyProjection,
            upcomingExpenses = upcomingExpenses
        )
    }
    
    private fun getRecurringExpensesByCategory(expenses: List<Expense>): Map<ExpenseCategory, Double> {
        return expenses.groupBy { it.category }
            .mapValues { (_, expenses) -> expenses.sumOf { it.amount } }
    }
    
    private fun getStartOfMonth(): Date {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }
    
    private fun getUpcomingExpenses(expenses: List<Expense>): List<Expense> {
        val now = Calendar.getInstance()
        val oneMonthFromNow = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
        }

        return expenses.filter { expense ->
            val nextOccurrence = calculateNextOccurrence(expense)
            nextOccurrence != null && nextOccurrence.time in now.timeInMillis..oneMonthFromNow.timeInMillis
        }
    }

    private fun calculateNextOccurrence(expense: Expense): Date? {
        val rule = expense.recurrenceRule ?: return null
        val lastDate = expense.date.toDate()
        val now = Calendar.getInstance().time

        val calendar = Calendar.getInstance().apply {
            time = lastDate
        }

        while (calendar.time <= now) {
            when (rule.frequency) {
                RecurrenceFrequency.DAILY -> calendar.add(Calendar.DAY_OF_MONTH, rule.interval)
                RecurrenceFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, rule.interval)
                RecurrenceFrequency.MONTHLY -> calendar.add(Calendar.MONTH, rule.interval)
                RecurrenceFrequency.YEARLY -> calendar.add(Calendar.YEAR, rule.interval)
                else -> return null
            }
        }

        return calendar.time
    }
    
    /**
     * Get frequency breakdown for recurring expenses
     */
    fun getFrequencyBreakdown(expenses: List<Expense>): List<FrequencyBreakdown> {
        val frequencyMap = mutableMapOf<RecurrenceFrequency, MutableList<Expense>>()
        
        expenses.forEach { expense ->
            expense.recurrenceRule?.frequency?.let { frequency ->
                frequencyMap.getOrPut(frequency) { mutableListOf() }.add(expense)
            }
        }
        
        val totalCount = expenses.size
        
        return frequencyMap.map { (frequency, expenseList) ->
            val count = expenseList.size
            val totalAmount = expenseList.sumOf { it.amount }
            val percentage = (count.toDouble() / totalCount) * 100
            
            FrequencyBreakdown(
                frequency = frequency,
                count = count,
                totalAmount = totalAmount,
                percentage = percentage
            )
        }.sortedByDescending { it.count }
    }
    
    /**
     * Get category breakdown for recurring expenses
     */
    fun getCategoryBreakdown(expenses: List<Expense>): List<CategoryBreakdown> {
        val categoryMap = mutableMapOf<ExpenseCategory, MutableList<Expense>>()
        
        expenses.forEach { expense ->
            val category = expense.category
            categoryMap.getOrPut(category) { mutableListOf() }.add(expense)
        }
        
        val totalCount = expenses.size
        
        return categoryMap.map { (category, expenseList) ->
            val count = expenseList.size
            val totalAmount = expenseList.sumOf { it.amount }
            val percentage = (count.toDouble() / totalCount) * 100
            
            CategoryBreakdown(
                category = category,
                count = count,
                totalAmount = totalAmount,
                percentage = percentage
            )
        }.sortedByDescending { it.count }
    }
    
    /**
     * Calculate monthly projection based on recurring expenses
     */
    private fun calculateMonthlyProjection(expenses: List<Expense>): Double {
        return expenses.sumOf { expense ->
            val rule = expense.recurrenceRule ?: return@sumOf 0.0
            
            when (rule.frequency) {
                RecurrenceFrequency.DAILY -> expense.amount * 30 / rule.interval
                RecurrenceFrequency.WEEKLY -> expense.amount * 4.33 / rule.interval // Average weeks per month
                RecurrenceFrequency.MONTHLY -> expense.amount / rule.interval
                RecurrenceFrequency.YEARLY -> expense.amount / (12 * rule.interval)
                else -> 0.0
            }
        }
    }
    
    /**
     * Get monthly trends for the last 12 months
     */
    suspend fun getMonthlyTrends(groupId: String): List<MonthlyTrend> {
        val trends = mutableListOf<MonthlyTrend>()
        val calendar = Calendar.getInstance()
        
        // Go back 12 months
        for (i in 11 downTo 0) {
            calendar.add(Calendar.MONTH, -i)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            
            val monthName = getMonthName(month)
            val monthKey = "$monthName $year"
            
            // Get expenses for this month
            val monthExpenses = getExpensesForMonth(groupId, month, year)
            
            val recurringAmount = monthExpenses.filter { it.isRecurring }.sumOf { it.amount }
            val generatedAmount = monthExpenses.filter { it.parentExpenseId != null }.sumOf { it.amount }
            val totalAmount = recurringAmount + generatedAmount
            
            trends.add(MonthlyTrend(
                month = monthKey,
                recurringAmount = recurringAmount,
                generatedAmount = generatedAmount,
                totalAmount = totalAmount
            ))
            
            calendar.add(Calendar.MONTH, i) // Reset calendar
        }
        
        return trends
    }
    
    /**
     * Get expenses for a specific month
     */
    private suspend fun getExpensesForMonth(groupId: String, month: Int, year: Int): List<Expense> {
        val allExpenses = expenseRepository.getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                pageSize = Int.MAX_VALUE
            )
        )
        val calendar = Calendar.getInstance()
        
        return allExpenses.expenses.filter { expense ->
            calendar.time = expense.date.toDate()
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }
    
    /**
     * Get insights and recommendations
     */
    fun getInsights(stats: RecurringExpenseStats, frequencyBreakdown: List<FrequencyBreakdown>): List<String> {
        val insights = mutableListOf<String>()
        
        if (stats.totalRecurringExpenses == 0) {
            insights.add("No recurring expenses found. Consider setting up recurring expenses for regular bills and subscriptions.")
            return insights
        }
        
        // High amount insights
        if (stats.totalRecurringAmount > 1000) {
            insights.add("Your total recurring expenses are high (${String.format("%.2f", stats.totalRecurringAmount)}). Consider reviewing for potential savings.")
        }
        
        // Frequency insights
        val dailyExpenses = frequencyBreakdown.find { it.frequency == RecurrenceFrequency.DAILY }
        if (dailyExpenses != null && dailyExpenses.count > 2) {
            insights.add("You have ${dailyExpenses.count} daily recurring expenses. Consider if some can be consolidated.")
        }
        
        // Category insights
        if (stats.recurringExpensesByCategory.isNotEmpty()) {
            val mostCommonCategory = stats.recurringExpensesByCategory.maxByOrNull { it.value }?.key
            if (mostCommonCategory != null) {
                insights.add("Most of your recurring expenses are in the ${mostCommonCategory} category.")
            }
        }
        
        // Savings opportunities
        if (stats.averageRecurringAmount > 100) {
            insights.add("Your average recurring expense is ${String.format("%.2f", stats.averageRecurringAmount)}. Look for bulk discounts or annual plans.")
        }
        
        return insights
    }
    
    private fun getMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "January"
            Calendar.FEBRUARY -> "February"
            Calendar.MARCH -> "March"
            Calendar.APRIL -> "April"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "June"
            Calendar.JULY -> "July"
            Calendar.AUGUST -> "August"
            Calendar.SEPTEMBER -> "September"
            Calendar.OCTOBER -> "October"
            Calendar.NOVEMBER -> "November"
            Calendar.DECEMBER -> "December"
            else -> "Unknown"
        }
    }
    
    /**
     * Clean up resources and cancel pending coroutines
     * Should be called when the app is being destroyed or user signs out
     */
    fun cleanup() {
        scope.cancel()
        Log.d("RecurringExpenseAnalytics", "RecurringExpenseAnalytics cleanup completed")
    }
} 