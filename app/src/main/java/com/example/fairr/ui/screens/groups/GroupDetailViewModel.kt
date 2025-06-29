package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember as DataGroupMember
import com.example.fairr.ui.model.GroupMember as UiGroupMember
import com.example.fairr.data.settlements.SettlementService
import com.google.firebase.auth.FirebaseAuth
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
        val members: List<UiGroupMember>,
        val currentUserBalance: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val expenses: List<Expense> = emptyList()
    ) : GroupDetailUiState
    data class Error(val message: String) : GroupDetailUiState
}

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupService: GroupService,
    private val expenseRepository: ExpenseRepository,
    private val settlementService: SettlementService,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState: GroupDetailUiState by mutableStateOf(GroupDetailUiState.Loading)
        private set

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    init {
        loadGroupDetails()
    }

    private fun convertToUiMember(member: DataGroupMember): UiGroupMember {
        val currentUserId = auth.currentUser?.uid
        return UiGroupMember(
            id = member.userId,
            name = member.name,
            email = member.email,
            isAdmin = member.role == com.example.fairr.data.model.GroupRole.ADMIN,
            isCurrentUser = member.userId == currentUserId
        )
    }

    fun refresh() {
        loadGroupDetails()
    }

    private fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = GroupDetailUiState.Loading
            try {
                groupService.getGroupById(groupId)
                    .combine(expenseRepository.getExpensesByGroupIdFlow(groupId)) { group, expenses ->
                        val uiMembers = group.members.map { convertToUiMember(it) }
                        val totalExpenses = expenses.sumOf { it.amount }

                        val summary = settlementService.getSettlementSummary(groupId)
                        val currentUserId = auth.currentUser?.uid
                        val currentUserBalance = summary.firstOrNull { it.userId == currentUserId }?.netBalance ?: 0.0

                        GroupDetailUiState.Success(
                            group = group,
                            members = uiMembers,
                            currentUserBalance = currentUserBalance,
                            totalExpenses = totalExpenses,
                            expenses = expenses
                        )
                    }
                    .catch { e ->
                        Log.e(TAG, "Error loading group details", e)
                        uiState = GroupDetailUiState.Error(e.message ?: "Failed to load group details")
                    }
                    .collect { state ->
                        uiState = state
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadGroupDetails", e)
                uiState = GroupDetailUiState.Error(e.message ?: "Failed to load group details")
            }
        }
    }

    fun formatDate(timestamp: com.google.firebase.Timestamp): String {
        return dateFormat.format(Date(timestamp.seconds * 1000))
    }
} 