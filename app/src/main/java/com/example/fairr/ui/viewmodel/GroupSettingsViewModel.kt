package com.example.fairr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.groups.GroupResult
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupSettingsUiState(
    val group: Group = Group(),
    val members: List<GroupMember> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class GroupSettingsEvent {
    object NavigateBack : GroupSettingsEvent()
    object GroupDeleted : GroupSettingsEvent()
    data class ShowError(val message: String) : GroupSettingsEvent()
}

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val groupService: GroupService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState: StateFlow<GroupSettingsUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GroupSettingsEvent>()
    val uiEvents: SharedFlow<GroupSettingsEvent> = _uiEvents.asSharedFlow()

    private var currentGroupId: String? = null

    fun loadGroup(groupId: String) {
        currentGroupId = groupId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                groupService.getGroup(groupId).collect { group ->
                    _uiState.update { 
                        it.copy(
                            group = group,
                            members = group.members,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                _uiEvents.emit(GroupSettingsEvent.ShowError(e.message ?: "Failed to load group"))
            }
        }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                try {
                    when (val result = groupService.deleteGroup(groupId)) {
                        is GroupResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvents.emit(GroupSettingsEvent.GroupDeleted)
                        }
                        is GroupResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = result.message) }
                            _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                    _uiEvents.emit(GroupSettingsEvent.ShowError(e.message ?: "Failed to delete group"))
                }
            }
        }
    }

    fun leaveGroup() {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                when (val result = groupService.leaveGroup(groupId)) {
                    is GroupResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.NavigateBack)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }

    fun removeMember(member: GroupMember) {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                when (val result = groupService.removeMember(groupId, member.userId)) {
                    is GroupResult.Success -> {
                        // Reload group to reflect updated members
                        loadGroup(groupId)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }
} 