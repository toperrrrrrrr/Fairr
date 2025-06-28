package com.example.fairr.data.analytics

import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.RecurrenceFrequency
import com.example.fairr.data.repository.ExpenseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecurringExpenseAnalytics @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    data class RecurringExpenseStats(
        val totalRecurringExpenses: Int,
        val totalAmount: Double,
        val averageAmount: Double,
        val mostCommonFrequency: RecurrenceFrequency?,
        val mostCommonCategory: String?,
        val upcomingExpenses: Int,
        val totalGeneratedInstances: Int,
        val monthlyProjection: Double,
        val yearlyProjection: Double
    )
    
    data class FrequencyBreakdown(
        val frequency: RecurrenceFrequency,
        val count: Int,
        val totalAmount: Double,
        val percentage: Double
    )
    
    data class CategoryBreakdown(
        val category: String,
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
        val recurringExpenses = expenseRepository.getRecurringExpenses(groupId)
        
        if (recurringExpenses.isEmpty()) {
            return RecurringExpenseStats(
                totalRecurringExpenses = 0,
                totalAmount = 0.0,
                averageAmount = 0.0,
                mostCommonFrequency = null,
                mostCommonCategory = null,
                upcomingExpenses = 0,
                totalGeneratedInstances = 0,
                monthlyProjection = 0.0,
                yearlyProjection = 0.0
            )
        }
        
        val totalAmount = recurringExpenses.sumOf { it.amount }
        val averageAmount = totalAmount / recurringExpenses.size
        
        // Calculate frequency breakdown
        val frequencyBreakdown = getFrequencyBreakdown(recurringExpenses)
        val mostCommonFrequency = frequencyBreakdown.maxByOrNull { it.count }?.frequency
        
        // Calculate category breakdown
        val categoryBreakdown = getCategoryBreakdown(recurringExpenses)
        val mostCommonCategory = categoryBreakdown.maxByOrNull { it.count }?.category
        
        // Get upcoming expenses (next 30 days)
        val upcomingExpenses = expenseRepository.getUpcomingRecurringExpenses(groupId, 30).size
        
        // Calculate projections
        val monthlyProjection = calculateMonthlyProjection(recurringExpenses)
        val yearlyProjection = monthlyProjection * 12
        
        // Count generated instances (expenses with parentExpenseId)
        val allExpenses = expenseRepository.getExpensesByGroupId(groupId)
        val totalGeneratedInstances = allExpenses.count { it.parentExpenseId != null }
        
        return RecurringExpenseStats(
            totalRecurringExpenses = recurringExpenses.size,
            totalAmount = totalAmount,
            averageAmount = averageAmount,
            mostCommonFrequency = mostCommonFrequency,
            mostCommonCategory = mostCommonCategory,
            upcomingExpenses = upcomingExpenses,
            totalGeneratedInstances = totalGeneratedInstances,
            monthlyProjection = monthlyProjection,
            yearlyProjection = yearlyProjection
        )
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
        val categoryMap = mutableMapOf<String, MutableList<Expense>>()
        
        expenses.forEach { expense ->
            val category = expense.category.displayName
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
        val allExpenses = expenseRepository.getExpensesByGroupId(groupId)
        val calendar = Calendar.getInstance()
        
        return allExpenses.filter { expense ->
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
        if (stats.monthlyProjection > 1000) {
            insights.add("Your monthly recurring expenses are high (${String.format("%.2f", stats.monthlyProjection)}). Consider reviewing for potential savings.")
        }
        
        // Frequency insights
        val dailyExpenses = frequencyBreakdown.find { it.frequency == RecurrenceFrequency.DAILY }
        if (dailyExpenses != null && dailyExpenses.count > 2) {
            insights.add("You have ${dailyExpenses.count} daily recurring expenses. Consider if some can be consolidated.")
        }
        
        // Category insights
        if (stats.mostCommonCategory != null) {
            insights.add("Most of your recurring expenses are in the ${stats.mostCommonCategory} category.")
        }
        
        // Upcoming expenses
        if (stats.upcomingExpenses > 0) {
            insights.add("You have ${stats.upcomingExpenses} recurring expenses coming up in the next 30 days.")
        }
        
        // Savings opportunities
        if (stats.averageAmount > 100) {
            insights.add("Your average recurring expense is ${String.format("%.2f", stats.averageAmount)}. Look for bulk discounts or annual plans.")
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
} 