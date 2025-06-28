package com.example.fairr.ui.screens.expenses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.model.Group
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first

sealed class AddExpenseEvent {
    data class ShowError(val message: String) : AddExpenseEvent()
    data object ExpenseSaved : AddExpenseEvent()
}

data class MemberInfo(
    val userId: String,
    val displayName: String
)

data class AddExpenseState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val groupMembers: List<MemberInfo> = emptyList(),
    val userCurrency: String = "PHP"
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupService: GroupService,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    var state by mutableStateOf(AddExpenseState())
        private set
        
    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val userCurrency = settingsDataStore.defaultCurrency.first()
            state = state.copy(userCurrency = userCurrency)
        }
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getCurrencySymbol(): String {
        return CurrencyFormatter.getSymbol(state.userCurrency)
    }

    fun formatCurrency(amount: Double): String {
        return CurrencyFormatter.format(state.userCurrency, amount)
    }

    fun formatCurrencyWithSpacing(amount: Double): String {
        return CurrencyFormatter.formatWithSpacing(state.userCurrency, amount)
    }

    fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String,
        category: com.example.fairr.data.model.ExpenseCategory = com.example.fairr.data.model.ExpenseCategory.OTHER,
        isRecurring: Boolean = false,
        recurrenceRule: com.example.fairr.data.model.RecurrenceRule? = null
    ) {
        viewModelScope.launch {
            try {
                state = state.copy(isLoading = true)
                
                // Add the expense
                expenseRepository.addExpense(groupId, description, amount, date, paidBy, splitType, category, isRecurring, recurrenceRule)
                
                // If it's a recurring expense, generate instances
                if (isRecurring && recurrenceRule != null) {
                    // Get the expense we just created to generate instances
                    val expenses = expenseRepository.getExpensesByGroupId(groupId)
                    val createdExpense = expenses.find { 
                        it.description == description && 
                        it.amount == amount && 
                        it.isRecurring && 
                        it.recurrenceRule == recurrenceRule 
                    }
                    
                    createdExpense?.let { expense ->
                        expenseRepository.generateRecurringInstances(expense, monthsAhead = 3)
                    }
                }
                
                state = state.copy(isLoading = false)
                _events.emit(AddExpenseEvent.ExpenseSaved)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _events.emit(AddExpenseEvent.ShowError(e.message ?: "Failed to save expense"))
            }
        }
    }

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            try {
                groupService.getGroupById(groupId).collectLatest { group ->
                    val memberInfos = group.members.map { member ->
                        MemberInfo(
                            userId = member.userId,
                            displayName = if (member.userId == getCurrentUserId()) "You" else member.name
                        )
                    }
                    state = state.copy(groupMembers = memberInfos)
                }
            } catch (e: Exception) {
                _events.emit(AddExpenseEvent.ShowError("Failed to load group members"))
            }
        }
    }

    fun getMemberIdByDisplayName(displayName: String): String {
        return state.groupMembers.find { it.displayName == displayName }?.userId ?: getCurrentUserId()
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
} 