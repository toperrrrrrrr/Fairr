package com.example.fairr.ui.screens.expenses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseCategory
import com.example.fairr.data.model.RecurrenceRule
import com.example.fairr.data.model.RecurrenceFrequency
import com.example.fairr.data.model.ExpenseSplit
import com.example.fairr.data.model.GroupMember
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.util.CurrencyFormatter
import com.example.fairr.ui.screens.expenses.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import com.example.fairr.utils.ReceiptPhoto
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

sealed class EditExpenseEvent {
    data class ShowError(val message: String) : EditExpenseEvent()
    data object ExpenseUpdated : EditExpenseEvent()
    data object ExpenseDeleted : EditExpenseEvent()
}

data class EditExpenseState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val expense: Expense? = null,
    val groupMembers: List<GroupMember> = emptyList(),
    val userCurrency: String = "PHP"
)

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupService: GroupService,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    
    var state by mutableStateOf(EditExpenseState())
        private set
        
    private val _events = MutableSharedFlow<EditExpenseEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val userCurrency = settingsDataStore.defaultCurrency.first()
            state = state.copy(userCurrency = userCurrency)
        }
    }

    fun loadExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true, error = null)
                val expense = expenseRepository.getExpenseById(expenseId)
                state = state.copy(expense = expense, isLoading = false)
            } catch (e: Exception) {
                state = state.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load expense"
                )
                _events.emit(EditExpenseEvent.ShowError(e.message ?: "Failed to load expense"))
            }
        }
    }

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            try {
                val members = groupService.getGroupMembers(groupId).first()
                state = state.copy(groupMembers = members)
            } catch (e: Exception) {
                // Log error but don't fail the screen
                _events.emit(EditExpenseEvent.ShowError("Failed to load group members"))
            }
        }
    }

    fun updateExpense(
        expenseId: String,
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String,
        category: ExpenseCategory,
        notes: String,
        isRecurring: Boolean,
        recurrenceRule: RecurrenceRule?,
        splits: List<ExpenseSplit>
    ) {
        viewModelScope.launch {
            try {
                // Validate input first
                when (val validation = validateExpense(description, amount, paidBy, splitType)) {
                    is ValidationResult.Error -> {
                        _events.emit(EditExpenseEvent.ShowError(validation.message))
                        return@launch
                    }
                    is ValidationResult.Success -> {
                        // Continue with updating
                    }
                }

                state = state.copy(isLoading = true)
                
                val currentExpense = state.expense
                if (currentExpense == null) {
                    _events.emit(EditExpenseEvent.ShowError("No expense to update"))
                    return@launch
                }

                val updatedExpense = currentExpense.copy(
                    description = description,
                    amount = amount,
                    date = com.google.firebase.Timestamp(date),
                    paidBy = paidBy,
                    splitType = splitType,
                    category = category,
                    notes = notes,
                    splitBetween = splits,
                    isRecurring = isRecurring,
                    recurrenceRule = recurrenceRule
                )

                expenseRepository.updateExpense(currentExpense, updatedExpense)
                
                state = state.copy(isLoading = false)
                _events.emit(EditExpenseEvent.ExpenseUpdated)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _events.emit(EditExpenseEvent.ShowError(e.message ?: "Failed to update expense"))
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                
                val currentExpense = state.expense
                if (currentExpense == null) {
                    _events.emit(EditExpenseEvent.ShowError("No expense to delete"))
                    return@launch
                }

                expenseRepository.deleteExpense(currentExpense)
                
                state = state.copy(isLoading = false)
                _events.emit(EditExpenseEvent.ExpenseDeleted)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _events.emit(EditExpenseEvent.ShowError(e.message ?: "Failed to delete expense"))
            }
        }
    }

    /**
     * Validate expense data before saving
     */
    private fun validateExpense(
        description: String,
        amount: Double,
        paidBy: String,
        splitType: String
    ): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Error("Description cannot be empty")
            description.length > 100 -> ValidationResult.Error("Description is too long (max 100 characters)")
            amount <= 0 -> ValidationResult.Error("Amount must be greater than 0")
            amount > 999999.99 -> ValidationResult.Error("Amount is too large")
            paidBy.isBlank() -> ValidationResult.Error("Please select who paid")
            splitType.isBlank() -> ValidationResult.Error("Please select a split type")
            state.groupMembers.isEmpty() -> ValidationResult.Error("No group members found")
            else -> ValidationResult.Success
        }
    }

    fun formatCurrency(amount: Double): String {
        return CurrencyFormatter.format(state.userCurrency, amount)
    }

    fun getCurrencySymbol(): String {
        return CurrencyFormatter.getSymbol(state.userCurrency)
    }
} 