package com.example.fairr.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.Group
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class AnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val overallStats: OverallSpendingStats = OverallSpendingStats(),
    val groupBreakdown: List<GroupSpendingBreakdown> = emptyList(),
    val categoryBreakdown: List<CategorySpendingBreakdown> = emptyList(),
    val monthlyTrends: List<MonthlySpendingTrend> = emptyList(),
    val insights: List<String> = emptyList()
)

data class OverallSpendingStats(
    val totalSpent: Double = 0.0,
    val thisMonthSpent: Double = 0.0,
    val totalGroups: Int = 0,
    val totalExpenses: Int = 0,
    val averagePerExpense: Double = 0.0
)

data class GroupSpendingBreakdown(
    val groupId: String,
    val groupName: String,
    val totalSpent: Double,
    val expenseCount: Int
)

data class CategorySpendingBreakdown(
    val category: String,
    val totalSpent: Double,
    val expenseCount: Int,
    val percentage: Double
)

data class MonthlySpendingTrend(
    val month: String,
    val totalSpent: Double,
    val expenseCount: Int
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupService: GroupService,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Get current user
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            error = "User not authenticated"
                        ) 
                    }
                    return@launch
                }
                
                // Get all user groups
                val userGroups = groupService.getUserGroups().first()
                
                // Get all expenses across all groups
                val allExpenses = mutableListOf<Expense>()
                userGroups.forEach { group ->
                    val groupExpenses = expenseRepository.getExpensesByGroupId(group.id)
                    allExpenses.addAll(groupExpenses)
                }
                
                // Calculate overall stats
                val overallStats = calculateOverallStats(allExpenses, userGroups)
                
                // Calculate group breakdown
                val groupBreakdown = calculateGroupBreakdown(userGroups, allExpenses)
                
                // Calculate category breakdown
                val categoryBreakdown = calculateCategoryBreakdown(allExpenses)
                
                // Calculate monthly trends
                val monthlyTrends = calculateMonthlyTrends(allExpenses)
                
                // Generate insights
                val insights = generateInsights(overallStats, groupBreakdown, categoryBreakdown)
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        overallStats = overallStats,
                        groupBreakdown = groupBreakdown,
                        categoryBreakdown = categoryBreakdown,
                        monthlyTrends = monthlyTrends,
                        insights = insights
                    ) 
                }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load analytics"
                    ) 
                }
            }
        }
    }
    
    private fun calculateOverallStats(expenses: List<Expense>, groups: List<Group>): OverallSpendingStats {
        if (expenses.isEmpty()) {
            return OverallSpendingStats()
        }
        
        val totalSpent = expenses.sumOf { it.amount }
        val totalExpenses = expenses.size
        val averagePerExpense = totalSpent / totalExpenses
        
        // Calculate this month's spending
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val thisMonthExpenses = expenses.filter { expense ->
            calendar.time = expense.date.toDate()
            calendar.get(Calendar.MONTH) == currentMonth && 
            calendar.get(Calendar.YEAR) == currentYear
        }
        
        val thisMonthSpent = thisMonthExpenses.sumOf { it.amount }
        
        return OverallSpendingStats(
            totalSpent = totalSpent,
            thisMonthSpent = thisMonthSpent,
            totalGroups = groups.size,
            totalExpenses = totalExpenses,
            averagePerExpense = averagePerExpense
        )
    }
    
    private fun calculateGroupBreakdown(groups: List<Group>, allExpenses: List<Expense>): List<GroupSpendingBreakdown> {
        return groups.map { group ->
            val groupExpenses = allExpenses.filter { it.groupId == group.id }
            val totalSpent = groupExpenses.sumOf { it.amount }
            
            GroupSpendingBreakdown(
                groupId = group.id,
                groupName = group.name,
                totalSpent = totalSpent,
                expenseCount = groupExpenses.size
            )
        }.sortedByDescending { it.totalSpent }
    }
    
    private fun calculateCategoryBreakdown(expenses: List<Expense>): List<CategorySpendingBreakdown> {
        if (expenses.isEmpty()) return emptyList()
        
        val categoryMap = mutableMapOf<String, MutableList<Expense>>()
        
        expenses.forEach { expense ->
            val category = expense.category.displayName
            categoryMap.getOrPut(category) { mutableListOf() }.add(expense)
        }
        
        val totalSpent = expenses.sumOf { it.amount }
        
        return categoryMap.map { (category, expenseList) ->
            val categoryTotal = expenseList.sumOf { it.amount }
            val percentage = (categoryTotal / totalSpent) * 100
            
            CategorySpendingBreakdown(
                category = category,
                totalSpent = categoryTotal,
                expenseCount = expenseList.size,
                percentage = percentage
            )
        }.sortedByDescending { it.totalSpent }
    }
    
    private fun calculateMonthlyTrends(expenses: List<Expense>): List<MonthlySpendingTrend> {
        if (expenses.isEmpty()) return emptyList()
        
        val trends = mutableListOf<MonthlySpendingTrend>()
        val calendar = Calendar.getInstance()
        
        // Go back 6 months
        for (i in 5 downTo 0) {
            calendar.add(Calendar.MONTH, -i)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)
            
            val monthName = getMonthName(month)
            val monthKey = "$monthName $year"
            
            // Get expenses for this month
            val monthExpenses = expenses.filter { expense ->
                calendar.time = expense.date.toDate()
                calendar.get(Calendar.MONTH) == month && 
                calendar.get(Calendar.YEAR) == year
            }
            
            val totalSpent = monthExpenses.sumOf { it.amount }
            
            trends.add(MonthlySpendingTrend(
                month = monthKey,
                totalSpent = totalSpent,
                expenseCount = monthExpenses.size
            ))
            
            calendar.add(Calendar.MONTH, i) // Reset calendar
        }
        
        return trends
    }
    
    private fun getMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "Jan"
            Calendar.FEBRUARY -> "Feb"
            Calendar.MARCH -> "Mar"
            Calendar.APRIL -> "Apr"
            Calendar.MAY -> "May"
            Calendar.JUNE -> "Jun"
            Calendar.JULY -> "Jul"
            Calendar.AUGUST -> "Aug"
            Calendar.SEPTEMBER -> "Sep"
            Calendar.OCTOBER -> "Oct"
            Calendar.NOVEMBER -> "Nov"
            Calendar.DECEMBER -> "Dec"
            else -> "Unknown"
        }
    }
    
    private fun generateInsights(
        overallStats: OverallSpendingStats,
        groupBreakdown: List<GroupSpendingBreakdown>,
        categoryBreakdown: List<CategorySpendingBreakdown>
    ): List<String> {
        val insights = mutableListOf<String>()
        
        if (overallStats.totalExpenses == 0) {
            insights.add("No expenses found. Start adding expenses to see your spending patterns.")
            return insights
        }
        
        // High spending insights
        if (overallStats.averagePerExpense > 100) {
            insights.add("Your average expense is ${String.format("%.2f", overallStats.averagePerExpense)}. Consider if some expenses can be reduced.")
        }
        
        // Monthly spending insights
        if (overallStats.thisMonthSpent > overallStats.totalSpent * 0.3) {
            insights.add("This month's spending (${String.format("%.2f", overallStats.thisMonthSpent)}) is high compared to your total. Review recent expenses.")
        }
        
        // Group insights
        if (groupBreakdown.isNotEmpty()) {
            val topGroup = groupBreakdown.first()
            if (topGroup.totalSpent > overallStats.totalSpent * 0.5) {
                insights.add("Most of your spending is in '${topGroup.groupName}' (${String.format("%.1f", (topGroup.totalSpent / overallStats.totalSpent) * 100)}%).")
            }
        }
        
        // Category insights
        if (categoryBreakdown.isNotEmpty()) {
            val topCategory = categoryBreakdown.first()
            if (topCategory.percentage > 40) {
                insights.add("Your highest spending category is ${topCategory.category} (${String.format("%.1f", topCategory.percentage)}%).")
            }
        }
        
        // Savings opportunities
        if (overallStats.totalExpenses > 20) {
            insights.add("You have ${overallStats.totalExpenses} total expenses. Consider consolidating similar expenses.")
        }
        
        return insights
    }
} 