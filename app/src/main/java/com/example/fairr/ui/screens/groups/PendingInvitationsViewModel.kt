package com.example.fairr.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.groups.GroupInviteResult
import com.example.fairr.data.model.GroupInvite
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingInvitationsUiState(
    val isLoading: Boolean = false,
    val invitations: List<GroupInvite> = emptyList(),
    val processingInviteId: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class PendingInvitationsViewModel @Inject constructor(
    private val groupInviteService: GroupInviteService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PendingInvitationsUiState())
    val uiState: StateFlow<PendingInvitationsUiState> = _uiState.asStateFlow()
    
    fun loadPendingInvitations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            groupInviteService.getPendingInvitations()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Failed to load invitations: ${exception.message}"
                    )
                }
                .collect { invitations ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        invitations = invitations
                    )
                }
        }
    }
    
    fun acceptInvitation(inviteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                processingInviteId = inviteId,
                errorMessage = null
            )
            
            when (val result = groupInviteService.acceptInvitation(inviteId)) {
                is GroupInviteResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        processingInviteId = null,
                        successMessage = result.message,
                        invitations = _uiState.value.invitations.filter { it.id != inviteId }
                    )
                }
                is GroupInviteResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        processingInviteId = null,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    fun rejectInvitation(inviteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                processingInviteId = inviteId,
                errorMessage = null
            )
            
            when (val result = groupInviteService.rejectInvitation(inviteId)) {
                is GroupInviteResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        processingInviteId = null,
                        successMessage = result.message,
                        invitations = _uiState.value.invitations.filter { it.id != inviteId }
                    )
                }
                is GroupInviteResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        processingInviteId = null,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
