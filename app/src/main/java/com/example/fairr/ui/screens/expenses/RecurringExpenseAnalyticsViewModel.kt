package com.example.fairr.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.analytics.RecurringExpenseAnalytics
import com.example.fairr.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringExpenseAnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val stats: RecurringExpenseAnalytics.RecurringExpenseStats = RecurringExpenseAnalytics.RecurringExpenseStats(
        totalRecurringExpenses = 0,
        totalAmount = 0.0,
        averageAmount = 0.0,
        mostCommonFrequency = null,
        mostCommonCategory = null,
        upcomingExpenses = 0,
        totalGeneratedInstances = 0,
        monthlyProjection = 0.0,
        yearlyProjection = 0.0
    ),
    val frequencyBreakdown: List<RecurringExpenseAnalytics.FrequencyBreakdown> = emptyList(),
    val categoryBreakdown: List<RecurringExpenseAnalytics.CategoryBreakdown> = emptyList(),
    val monthlyTrends: List<RecurringExpenseAnalytics.MonthlyTrend> = emptyList(),
    val insights: List<String> = emptyList()
)

@HiltViewModel
class RecurringExpenseAnalyticsViewModel @Inject constructor(
    private val analytics: RecurringExpenseAnalytics,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringExpenseAnalyticsState())
    val state: StateFlow<RecurringExpenseAnalyticsState> = _state

    fun loadAnalytics(groupId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Load all analytics data
                val stats = analytics.getRecurringExpenseStats(groupId)
                val recurringExpenses = expenseRepository.getRecurringExpenses(groupId)
                val frequencyBreakdown = analytics.getFrequencyBreakdown(recurringExpenses)
                val categoryBreakdown = analytics.getCategoryBreakdown(recurringExpenses)
                val monthlyTrends = analytics.getMonthlyTrends(groupId)
                val insights = analytics.getInsights(stats, frequencyBreakdown)
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        stats = stats,
                        frequencyBreakdown = frequencyBreakdown,
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
} 