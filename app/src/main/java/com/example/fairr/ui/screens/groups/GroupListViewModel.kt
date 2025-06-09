package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GroupListViewModel"

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupService: GroupService
) : ViewModel() {

    var uiState: GroupListUiState by mutableStateOf(GroupListUiState.Loading)
        private set

    init {
        loadGroups()
    }

    fun loadGroups() {
        viewModelScope.launch {
            uiState = GroupListUiState.Loading
            try {
                groupService.getUserGroups()
                    .catch { e ->
                        Log.e(TAG, "Error loading groups", e)
                        uiState = GroupListUiState.Error(e.message ?: "Unknown error occurred")
                    }
                    .collect { groups ->
                        uiState = GroupListUiState.Success(groups)
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading groups", e)
                uiState = GroupListUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun refresh() {
        loadGroups()
    }
}