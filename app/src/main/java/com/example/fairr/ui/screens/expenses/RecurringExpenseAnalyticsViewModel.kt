package com.example.fairr.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.analytics.RecurringExpenseAnalytics
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseCategory
import com.example.fairr.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringExpenseAnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userCurrency: String = "PHP",
    val stats: RecurringExpenseAnalytics.RecurringExpenseStats = RecurringExpenseAnalytics.RecurringExpenseStats(
        totalRecurringExpenses = 0,
        totalRecurringAmount = 0.0,
        averageRecurringAmount = 0.0,
        recurringExpensesByCategory = emptyMap(),
        totalGeneratedInstances = 0,
        monthlyProjection = 0.0,
        yearlyProjection = 0.0,
        upcomingExpenses = emptyList()
    ),
    val frequencyBreakdown: List<RecurringExpenseAnalytics.FrequencyBreakdown> = emptyList(),
    val categoryBreakdown: List<RecurringExpenseAnalytics.CategoryBreakdown> = emptyList(),
    val monthlyTrends: List<RecurringExpenseAnalytics.MonthlyTrend> = emptyList(),
    val insights: List<String> = emptyList()
)

@HiltViewModel
class RecurringExpenseAnalyticsViewModel @Inject constructor(
    private val analytics: RecurringExpenseAnalytics,
    private val expenseRepository: ExpenseRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringExpenseAnalyticsState())
    val state: StateFlow<RecurringExpenseAnalyticsState> = _state

    fun loadAnalytics(groupId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                
                // Get user's preferred currency
                val userCurrency = settingsDataStore.defaultCurrency.first()
                
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
                        userCurrency = userCurrency,
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

    // Currency formatting methods using user's preferred currency
    fun formatCurrency(amount: Double): String {
        return CurrencyFormatter.format(state.value.userCurrency, amount)
    }

    fun formatCurrencyWithSpacing(amount: Double): String {
        return CurrencyFormatter.formatWithSpacing(state.value.userCurrency, amount)
    }

    fun getCurrencySymbol(): String {
        return CurrencyFormatter.getSymbol(state.value.userCurrency)
    }
} 