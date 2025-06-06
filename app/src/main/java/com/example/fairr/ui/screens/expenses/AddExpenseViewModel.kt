package com.example.fairr.ui.screens.expenses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class AddExpenseEvent {
    data class ShowError(val message: String) : AddExpenseEvent()
    data object ExpenseSaved : AddExpenseEvent()
}

data class AddExpenseState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    var state by mutableStateOf(AddExpenseState())
        private set
        
    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events = _events.asSharedFlow()

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getCurrencySymbol(): String {
        // TODO: Get this from group settings
        return "$"
    }

    fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                expenseRepository.addExpense(groupId, description, amount, date)
                state = state.copy(isLoading = false)
                _events.emit(AddExpenseEvent.ExpenseSaved)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _events.emit(AddExpenseEvent.ShowError(e.message ?: "Failed to save expense"))
            }
        }
    }
} 