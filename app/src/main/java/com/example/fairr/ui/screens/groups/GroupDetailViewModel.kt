package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.expenses.ExpenseService
import com.example.fairr.data.expenses.Expense
import com.example.fairr.ui.model.Group
import com.example.fairr.ui.model.GroupMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private const val TAG = "GroupDetailViewModel"

sealed interface GroupDetailUiState {
    object Loading : GroupDetailUiState
    data class Success(
        val group: Group,
        val currentUserBalance: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val expenses: List<Expense> = emptyList()
    ) : GroupDetailUiState
    data class Error(val message: String) : GroupDetailUiState
}

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseService: ExpenseService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState: GroupDetailUiState by mutableStateOf(GroupDetailUiState.Loading)
        private set

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    init {
        loadGroupDetails()
    }

    fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = GroupDetailUiState.Loading
            try {
                combine(
                    groupService.getGroupById(groupId),
                    expenseService.getExpensesForGroup(groupId)
                ) { group, expenses ->
                    val totalExpenses = expenses.sumOf { it.amount }
                    // TODO: Calculate individual balances
                    GroupDetailUiState.Success(
                        group = group,
                        expenses = expenses,
                        totalExpenses = totalExpenses
                    )
                }.catch { e ->
                    Log.e(TAG, "Error loading group details", e)
                    uiState = GroupDetailUiState.Error(e.message ?: "Unknown error occurred")
                }.collect { state ->
                    uiState = state
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading group details", e)
                uiState = GroupDetailUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refresh() {
        loadGroupDetails()
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
} 