package com.example.fairr.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupService
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

data class HomeScreenState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalBalance: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val activeGroups: Int = 0,
    val groups: List<Group> = emptyList(),
    val recentExpenses: List<Expense> = emptyList(),
    val userCurrency: String = "PHP"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseRepository: ExpenseRepository,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeScreenState())
    val state = _state.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)

                // Get user's preferred currency
                val userCurrency = settingsDataStore.defaultCurrency.first()

                groupService.getActiveGroups()
                    .catch { e ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred while loading groups"
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
                                _state.value = _state.value.copy(
                                    error = "Error loading expenses for some groups"
                                )
                            }
                        }

                        _state.value = _state.value.copy(
                            isLoading = false,
                            groups = groups,
                            activeGroups = groups.size,
                            totalBalance = totalBalance,
                            totalExpenses = totalExpenses,
                            recentExpenses = recentExpenses.sortedByDescending { it.date }.take(5),
                            userCurrency = userCurrency
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun refresh() {
        loadHomeData()
    }

    // Currency formatting methods
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