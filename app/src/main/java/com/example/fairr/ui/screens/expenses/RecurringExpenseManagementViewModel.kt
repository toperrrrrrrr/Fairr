package com.example.fairr.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.model.Expense
import com.example.fairr.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RecurringExpenseManagementEvent {
    data class ShowError(val message: String) : RecurringExpenseManagementEvent()
    data class InstancesGenerated(val count: Int) : RecurringExpenseManagementEvent()
}

data class RecurringExpenseManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val recurringExpenses: List<Expense> = emptyList()
)

@HiltViewModel
class RecurringExpenseManagementViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringExpenseManagementState())
    val state: StateFlow<RecurringExpenseManagementState> = _state

    private val _events = MutableSharedFlow<RecurringExpenseManagementEvent>()
    val events = _events.asSharedFlow()

    fun loadRecurringExpenses(groupId: String) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true, error = null) }
                val expenses = expenseRepository.getRecurringExpenses(groupId)
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        recurringExpenses = expenses
                    ) 
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load recurring expenses"
                    ) 
                }
                _events.emit(RecurringExpenseManagementEvent.ShowError(e.message ?: "Failed to load recurring expenses"))
            }
        }
    }

    fun generateInstances(expense: Expense) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Generate instances for the next 3 months
                expenseRepository.generateRecurringInstances(expense, monthsAhead = 3)
                
                // Reload the recurring expenses to show updated data
                val expenses = expenseRepository.getRecurringExpenses(expense.groupId)
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        recurringExpenses = expenses
                    ) 
                }
                
                // Estimate the number of instances generated (simplified)
                val estimatedCount = when (expense.recurrenceRule?.frequency) {
                    com.example.fairr.data.model.RecurrenceFrequency.DAILY -> 90
                    com.example.fairr.data.model.RecurrenceFrequency.WEEKLY -> 12
                    com.example.fairr.data.model.RecurrenceFrequency.MONTHLY -> 3
                    com.example.fairr.data.model.RecurrenceFrequency.YEARLY -> 0
                    else -> 0
                }
                
                _events.emit(RecurringExpenseManagementEvent.InstancesGenerated(estimatedCount))
                
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _events.emit(RecurringExpenseManagementEvent.ShowError(e.message ?: "Failed to generate instances"))
            }
        }
    }

    fun deleteRecurringExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }
                
                // Delete the recurring expense
                expenseRepository.deleteExpense(expense)
                
                // Reload the list
                val expenses = expenseRepository.getRecurringExpenses(expense.groupId)
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        recurringExpenses = expenses
                    ) 
                }
                
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _events.emit(RecurringExpenseManagementEvent.ShowError(e.message ?: "Failed to delete recurring expense"))
            }
        }
    }
} 