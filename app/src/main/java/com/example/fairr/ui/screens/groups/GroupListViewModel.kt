package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.GroupRepository
import com.example.fairr.data.settlements.SettlementService
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GroupListViewModel"

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val settlementService: SettlementService,
    private val auth: FirebaseAuth
) : ViewModel() {

    var uiState: GroupListUiState by mutableStateOf(GroupListUiState.Loading)
        private set

    private var groupBalances by mutableStateOf<Map<String, Double>>(emptyMap())
    
    var showingArchived: Boolean by mutableStateOf(false)
        private set

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            uiState = GroupListUiState.Loading
            try {
                val groupsFlow = if (showingArchived) {
                    groupRepository.getArchivedGroups()
                } else {
                    groupRepository.getActiveGroups()
                }
                
                groupsFlow
                    .catch { e ->
                        Log.e(TAG, "Error loading groups", e)
                        uiState = GroupListUiState.Error(e.message ?: "Unknown error occurred")
                    }
                    .collect { groups ->
                        uiState = GroupListUiState.Success(groups)

                        // Compute balances asynchronously
                        computeBalances(groups)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading groups", e)
                uiState = GroupListUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
    
    fun toggleShowArchived() {
        showingArchived = !showingArchived
        loadGroups()
    }

    private fun computeBalances(groups: List<com.example.fairr.data.model.Group>) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Launch parallel computations
        groups.forEach { group ->
            viewModelScope.launch {
                try {
                    val summary = settlementService.getSettlementSummary(group.id)
                    val balance = summary.firstOrNull { it.userId == currentUserId }?.netBalance ?: 0.0
                    groupBalances = groupBalances + (group.id to balance)
                } catch (e: Exception) {
                    Log.e(TAG, "Error calculating balance for group ${group.id}", e)
                }
            }
        }
    }

    fun getBalanceForGroup(groupId: String): Double {
        return groupBalances[groupId] ?: 0.0
    }

    fun refresh() {
        loadGroups()
    }
}