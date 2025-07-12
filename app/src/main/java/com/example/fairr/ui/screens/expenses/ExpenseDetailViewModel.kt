package com.example.fairr.ui.screens.expenses

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.model.Expense
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.repository.GroupRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "ExpenseDetailViewModel"

sealed interface ExpenseDetailUiState {
    object Loading : ExpenseDetailUiState
    data class Success(
        val expense: Expense,
        val groupName: String = "",
        val currentUserId: String = ""
    ) : ExpenseDetailUiState
    data class Error(val message: String) : ExpenseDetailUiState
}

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState: ExpenseDetailUiState by mutableStateOf(ExpenseDetailUiState.Loading)
        private set

    private val expenseId: String = checkNotNull(savedStateHandle["expenseId"])
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    init {
        loadExpenseDetails()
    }

    fun refresh() {
        loadExpenseDetails()
    }

    private fun loadExpenseDetails() {
        viewModelScope.launch {
            uiState = ExpenseDetailUiState.Loading
            try {
                val expense = expenseRepository.getExpenseById(expenseId)
                if (expense != null) {
                    // Get group name
                    val groupName = try {
                        val group = groupRepository.getGroup(expense.groupId).first()
                        group.name
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching group name", e)
                        "Unknown Group"
                    }

                    uiState = ExpenseDetailUiState.Success(
                        expense = expense,
                        groupName = groupName,
                        currentUserId = auth.currentUser?.uid ?: ""
                    )
                } else {
                    uiState = ExpenseDetailUiState.Error("Expense not found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading expense details", e)
                uiState = ExpenseDetailUiState.Error(e.message ?: "Failed to load expense details")
            }
        }
    }

    fun formatDate(timestamp: com.google.firebase.Timestamp): String {
        return dateFormat.format(Date(timestamp.seconds * 1000))
    }

    fun isCurrentUserPayer(expense: Expense): Boolean {
        return expense.paidBy == auth.currentUser?.uid
    }

    fun isCurrentUserParticipant(expense: Expense): Boolean {
        return expense.splitBetween.any { it.userId == auth.currentUser?.uid }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting deletion of expense: $expenseId")
                val expense = expenseRepository.getExpenseById(expenseId)
                if (expense != null) {
                    Log.d(TAG, "Found expense to delete: ${expense.description}")
                    expenseRepository.deleteExpense(expense)
                    Log.d(TAG, "Expense deleted successfully: $expenseId")
                    // Emit success event or update UI state
                } else {
                    Log.e(TAG, "Expense not found for deletion: $expenseId")
                    // You could emit an error state here
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting expense: $expenseId", e)
                // You could emit an error state here
            }
        }
    }
} 