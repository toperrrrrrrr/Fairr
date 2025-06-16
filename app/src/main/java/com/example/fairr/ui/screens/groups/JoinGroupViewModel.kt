package com.example.fairr.ui.screens.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupJoinService
import com.example.fairr.data.groups.JoinRequestResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class JoinGroupUiState {
    object Initial : JoinGroupUiState()
    object Loading : JoinGroupUiState()
    data class Success(val message: String) : JoinGroupUiState()
    data class Error(val message: String) : JoinGroupUiState()
}

@HiltViewModel
class JoinGroupViewModel @Inject constructor(
    private val groupJoinService: GroupJoinService
) : ViewModel() {

    var uiState by mutableStateOf<JoinGroupUiState>(JoinGroupUiState.Initial)
        private set

    var inviteCode by mutableStateOf("")
        private set

    fun onInviteCodeChange(code: String) {
        inviteCode = code.uppercase()
        if (uiState is JoinGroupUiState.Error) {
            uiState = JoinGroupUiState.Initial
        }
    }

    fun requestToJoinGroup() {
        if (inviteCode.isBlank()) {
            uiState = JoinGroupUiState.Error("Please enter an invite code")
            return
        }

        viewModelScope.launch {
            uiState = JoinGroupUiState.Loading

            when (val result = groupJoinService.requestToJoinGroup(inviteCode)) {
                is JoinRequestResult.Success -> {
                    uiState = JoinGroupUiState.Success("Join request sent! The group creator will be notified.")
                }
                is JoinRequestResult.Error -> {
                    uiState = JoinGroupUiState.Error(result.message)
                }
            }
        }
    }

    fun resetState() {
        uiState = JoinGroupUiState.Initial
        inviteCode = ""
    }
} 