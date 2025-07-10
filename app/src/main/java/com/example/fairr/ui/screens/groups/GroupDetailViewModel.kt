package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.fairr.data.repository.GroupRepository

private const val TAG = "GroupDetailViewModel"

// Activity types for group feed
enum class ActivityType {
    EXPENSE_ADDED,
    MEMBER_JOINED,
    MEMBER_LEFT,
    EXPENSE_SETTLED,
    GROUP_CREATED
}

data class GroupActivity(
    val id: String,
    val type: ActivityType,
    val title: String,
    val description: String,
    val timestamp: com.google.firebase.Timestamp,
    val userId: String? = null,
    val userName: String? = null,
    val expenseId: String? = null,
    val amount: Double? = null
)

sealed interface GroupDetailUiState {
    object Loading : GroupDetailUiState
    data class Success(
        val group: Group,
        val members: List<UiGroupMember>,
        val currentUserBalance: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val expenses: List<Expense> = emptyList(),
        val activities: List<GroupActivity> = emptyList()
    ) : GroupDetailUiState
    data class Error(val message: String) : GroupDetailUiState
}

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
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

    private fun generateActivities(group: Group, expenses: List<Expense>): List<GroupActivity> {
        val activities = mutableListOf<GroupActivity>()
        
        // Add group creation activity
        activities.add(
            GroupActivity(
                id = "group_created_${group.id}",
                type = ActivityType.GROUP_CREATED,
                title = "Group Created",
                description = "${group.name} was created",
                timestamp = group.createdAt,
                userId = group.createdBy,
                userName = group.members.find { it.userId == group.createdBy }?.name ?: "Unknown"
            )
        )
        
        // Add member join activities (based on joinedAt timestamps)
        group.members.forEach { member ->
            if (member.joinedAt != group.createdAt) { // Don't duplicate creation activity
                activities.add(
                    GroupActivity(
                        id = "member_joined_${member.userId}_${member.joinedAt.seconds}",
                        type = ActivityType.MEMBER_JOINED,
                        title = "Member Joined",
                        description = "${member.name} joined the group",
                        timestamp = member.joinedAt,
                        userId = member.userId,
                        userName = member.name
                    )
                )
            }
        }
        
        // Add expense activities
        expenses.forEach { expense ->
            activities.add(
                GroupActivity(
                    id = "expense_added_${expense.id}",
                    type = ActivityType.EXPENSE_ADDED,
                    title = "Expense Added",
                    description = "${expense.description} - ${expense.paidByName} paid ${expense.amount}",
                    timestamp = expense.date,
                    userId = expense.paidBy,
                    userName = expense.paidByName,
                    expenseId = expense.id,
                    amount = expense.amount
                )
            )
        }
        
        // Sort by timestamp (newest first) and limit to recent activities
        return activities.sortedByDescending { it.timestamp.seconds }.take(20)
    }

    fun refresh() {
        loadGroupDetails()
    }

    private fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = GroupDetailUiState.Loading
            try {
                groupRepository.getGroup(groupId)
                    .combine(expenseRepository.getExpensesByGroupIdFlow(groupId)) { group, expenses ->
                        val uiMembers = group.members.map { convertToUiMember(it) }
                        val totalExpenses = expenses.sumOf { it.amount }
                        val activities = generateActivities(group, expenses)

                        val summary = settlementService.getSettlementSummary(groupId)
                        val currentUserId = auth.currentUser?.uid
                        val currentUserBalance = summary.firstOrNull { it.userId == currentUserId }?.netBalance ?: 0.0

                        GroupDetailUiState.Success(
                            group = group,
                            members = uiMembers,
                            currentUserBalance = currentUserBalance,
                            totalExpenses = totalExpenses,
                            expenses = expenses,
                            activities = activities
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
    
    fun formatActivityDate(timestamp: com.google.firebase.Timestamp): String {
        val now = System.currentTimeMillis() / 1000
        val diff = now - timestamp.seconds
        
        return when {
            diff < 60 -> "Just now"
            diff < 3600 -> "${diff / 60}m ago"
            diff < 86400 -> "${diff / 3600}h ago"
            diff < 604800 -> "${diff / 86400}d ago"
            else -> dateFormat.format(Date(timestamp.seconds * 1000))
        }
    }
} 