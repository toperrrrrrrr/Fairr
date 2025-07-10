package com.example.fairr.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.GroupRepository
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.Expense
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.util.CurrencyFormatter
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Sealed class for Home screen UI state management
 * Follows consistent pattern used across the app
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val totalBalance: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val activeGroups: Int = 0,
        val groups: List<Group> = emptyList(),
        val recentExpenses: List<Expense> = emptyList(),
        val userCurrency: String = "PHP"
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    var uiState by mutableStateOf<HomeUiState>(HomeUiState.Loading)
        private set

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                uiState = HomeUiState.Loading

                // Get user's preferred currency
                val userCurrency = settingsDataStore.defaultCurrency.first()

                groupRepository.getActiveGroups()
                    .catch { e ->
                        uiState = HomeUiState.Error(
                            e.message ?: "An error occurred while loading groups"
                        )
                    }
                    .collect { groups ->
                        var totalBalance = 0.0
                        var totalExpenses = 0.0
                        val recentExpenses = mutableListOf<Expense>()

                        // Load expenses for each group
                        groups.forEach { group ->
                            try {
                                val expenses = expenseRepository.getExpensesByGroupId(group.id)
                                recentExpenses.addAll(expenses)

                                // Calculate group totals
                                expenses.forEach { expense ->
                                    totalExpenses += expense.amount
                                    // Add to balance if user is owed money, subtract if user owes
                                    if (expense.paidBy == auth.currentUser?.uid) {
                                        totalBalance += expense.amount
                                    }
                                    // Subtract user's share
                                    totalBalance -= expense.amount / expense.splitBetween.size
                                }
                            } catch (e: Exception) {
                                // Log error but continue processing other groups
                                uiState = HomeUiState.Error("Error loading expenses for some groups")
                                return@collect
                            }
                        }

                        uiState = HomeUiState.Success(
                            groups = groups,
                            activeGroups = groups.size,
                            totalBalance = totalBalance,
                            totalExpenses = totalExpenses,
                            recentExpenses = recentExpenses.sortedByDescending { it.date }.take(5),
                            userCurrency = userCurrency
                        )
                    }
            } catch (e: Exception) {
                uiState = HomeUiState.Error(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }

    // Currency formatting methods
    fun formatCurrency(amount: Double): String {
        return when (val currentState = uiState) {
            is HomeUiState.Success -> CurrencyFormatter.format(currentState.userCurrency, amount)
            else -> CurrencyFormatter.format("PHP", amount) // fallback
        }
    }

    fun formatCurrencyWithSpacing(amount: Double): String {
        return when (val currentState = uiState) {
            is HomeUiState.Success -> CurrencyFormatter.formatWithSpacing(currentState.userCurrency, amount)
            else -> CurrencyFormatter.formatWithSpacing("PHP", amount) // fallback
        }
    }

    fun getCurrencySymbol(): String {
        return when (val currentState = uiState) {
            is HomeUiState.Success -> CurrencyFormatter.getSymbol(currentState.userCurrency)
            else -> CurrencyFormatter.getSymbol("PHP") // fallback
        }
    }
} 