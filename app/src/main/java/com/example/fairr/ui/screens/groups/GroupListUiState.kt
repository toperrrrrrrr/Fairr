package com.example.fairr.ui.screens.groups

import com.example.fairr.ui.model.Group

sealed interface GroupListUiState {
    object Loading : GroupListUiState
    data class Success(val groups: List<Group>) : GroupListUiState
    data class Error(val message: String) : GroupListUiState
} 