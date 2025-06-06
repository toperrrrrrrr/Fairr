package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupService
import com.example.fairr.ui.model.Group
import com.example.fairr.ui.model.GroupMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GroupDetailViewModel"

sealed interface GroupDetailUiState {
    object Loading : GroupDetailUiState
    data class Success(
        val group: Group,
        val currentUserBalance: Double = 0.0,
        val totalExpenses: Double = 0.0
    ) : GroupDetailUiState
    data class Error(val message: String) : GroupDetailUiState
}

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val groupService: GroupService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    var uiState: GroupDetailUiState by mutableStateOf(GroupDetailUiState.Loading)
        private set

    private val groupId: String = checkNotNull(savedStateHandle["groupId"])

    init {
        loadGroupDetails()
    }

    fun loadGroupDetails() {
        viewModelScope.launch {
            uiState = GroupDetailUiState.Loading
            try {
                groupService.getGroupById(groupId)
                    .catch { e ->
                        Log.e(TAG, "Error loading group details", e)
                        uiState = GroupDetailUiState.Error(e.message ?: "Unknown error occurred")
                    }
                    .collect { group ->
                        // TODO: Calculate balances and total expenses
                        uiState = GroupDetailUiState.Success(
                            group = group,
                            currentUserBalance = 0.0, // TODO: Implement balance calculation
                            totalExpenses = 0.0 // TODO: Implement total expenses calculation
                        )
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
} 