package com.example.fairr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.GroupRepository
import com.example.fairr.data.repository.GroupResult
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.groups.GroupInviteResult
import com.example.fairr.data.model.Group
import com.example.fairr.data.model.GroupMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GroupSettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val group: Group? = null,
    val members: List<GroupMember> = emptyList(),
    val showDeleteDialog: Boolean = false,
    val showLeaveDialog: Boolean = false,
    val showRemoveMemberDialog: Boolean = false,
    val showEditGroupDialog: Boolean = false,
    val showPromoteMemberDialog: Boolean = false,
    val showDemoteMemberDialog: Boolean = false,
    val memberToRemove: GroupMember? = null,
    val memberToPromote: GroupMember? = null,
    val memberToDemote: GroupMember? = null
) {
    // Helper extension property
    val Group?.isUserAdmin: Boolean
        get() = this?.isUserAdmin ?: false
}

sealed class GroupSettingsEvent {
    object GroupDeleted : GroupSettingsEvent()
    object GroupUpdated : GroupSettingsEvent()
    object MemberRemoved : GroupSettingsEvent()
    object NavigateBack : GroupSettingsEvent()
    data class ShowError(val message: String) : GroupSettingsEvent()
    data class ShowSuccess(val message: String) : GroupSettingsEvent()
}

@HiltViewModel
class GroupSettingsViewModel @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupInviteService: GroupInviteService
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupSettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<GroupSettingsEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var currentGroupId: String? = null

    fun initialize(groupId: String) {
        currentGroupId = groupId
        loadGroup(groupId)
    }

    fun loadGroup(groupId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                groupRepository.getGroup(groupId).collect { group ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            group = group,
                            members = group.members
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load group"
                    ) 
                }
            }
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun showLeaveDialog() {
        _uiState.update { it.copy(showLeaveDialog = true) }
    }

    fun hideLeaveDialog() {
        _uiState.update { it.copy(showLeaveDialog = false) }
    }

    fun deleteGroup() {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                try {
                    val result = groupRepository.deleteGroup(groupId)
                    when (result) {
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
                val result = groupRepository.leaveGroup(groupId)
                when (result) {
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

    fun showRemoveMemberDialog(member: GroupMember) {
        _uiState.update { 
            it.copy(
                showRemoveMemberDialog = true,
                memberToRemove = member
            )
        }
    }

    fun hideRemoveMemberDialog() {
        _uiState.update { 
            it.copy(
                showRemoveMemberDialog = false,
                memberToRemove = null
            )
        }
    }

    fun removeMember(member: GroupMember) {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true, showRemoveMemberDialog = false) }
                val result = groupRepository.removeMember(groupId, member.userId)
                when (result) {
                    is GroupResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, memberToRemove = null) }
                        _uiEvents.emit(GroupSettingsEvent.MemberRemoved)
                        _uiEvents.emit(GroupSettingsEvent.ShowSuccess("${member.name} has been removed from the group"))
                        // Reload group to reflect updated members
                        loadGroup(groupId)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = result.message,
                                showRemoveMemberDialog = false,
                                memberToRemove = null
                            )
                        }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }

    fun showEditGroupDialog() {
        _uiState.update { it.copy(showEditGroupDialog = true) }
    }

    fun hideEditGroupDialog() {
        _uiState.update { it.copy(showEditGroupDialog = false) }
    }

    fun updateGroup(name: String, description: String, currency: String) {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true, showEditGroupDialog = false) }
                try {
                    val result = groupRepository.updateGroup(groupId, name, description, currency)
                    when (result) {
                        is GroupResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvents.emit(GroupSettingsEvent.GroupUpdated)
                            _uiEvents.emit(GroupSettingsEvent.ShowSuccess("Group updated successfully"))
                            // Reload group to reflect changes
                            loadGroup(groupId)
                        }
                        is GroupResult.Error -> {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false, 
                                    error = result.message,
                                    showEditGroupDialog = false
                                )
                            }
                            _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = e.message,
                            showEditGroupDialog = false
                        )
                    }
                    _uiEvents.emit(GroupSettingsEvent.ShowError(e.message ?: "Failed to update group"))
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun showPromoteMemberDialog(member: GroupMember) {
        _uiState.update {
            it.copy(showPromoteMemberDialog = true, memberToPromote = member)
        }
    }

    fun hidePromoteMemberDialog() {
        _uiState.update {
            it.copy(showPromoteMemberDialog = false, memberToPromote = null)
        }
    }

    fun showDemoteMemberDialog(member: GroupMember) {
        _uiState.update {
            it.copy(showDemoteMemberDialog = true, memberToDemote = member)
        }
    }

    fun hideDemoteMemberDialog() {
        _uiState.update {
            it.copy(showDemoteMemberDialog = false, memberToDemote = null)
        }
    }

    fun promoteMember(member: GroupMember) {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true, showPromoteMemberDialog = false) }
                val result = groupRepository.promoteToAdmin(groupId, member.userId)
                when (result) {
                    is GroupResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, memberToPromote = null) }
                        _uiEvents.emit(GroupSettingsEvent.ShowSuccess("${member.name} is now an admin"))
                        loadGroup(groupId)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, showPromoteMemberDialog = false, memberToPromote = null) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }

    fun demoteMember(member: GroupMember) {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true, showDemoteMemberDialog = false) }
                val result = groupRepository.demoteFromAdmin(groupId, member.userId)
                when (result) {
                    is GroupResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, memberToDemote = null) }
                        _uiEvents.emit(GroupSettingsEvent.ShowSuccess("${member.name} is no longer an admin"))
                        loadGroup(groupId)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, showDemoteMemberDialog = false, memberToDemote = null) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }

    fun archiveGroup() {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                val result = groupRepository.archiveGroup(groupId)
                when (result) {
                    is GroupResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.ShowSuccess("Group archived successfully"))
                        loadGroup(groupId)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }

    fun unarchiveGroup() {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                val result = groupRepository.unarchiveGroup(groupId)
                when (result) {
                    is GroupResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.ShowSuccess("Group unarchived successfully"))
                        loadGroup(groupId)
                    }
                    is GroupResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }

    fun sendGroupInvitation(email: String, message: String) {
        viewModelScope.launch {
            currentGroupId?.let { groupId ->
                _uiState.update { it.copy(isLoading = true) }
                val result = groupInviteService.sendGroupInvitation(groupId, email, message)
                when (result) {
                    is GroupInviteResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.ShowSuccess(result.message))
                    }
                    is GroupInviteResult.Error -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(GroupSettingsEvent.ShowError(result.message))
                    }
                }
            }
        }
    }
} 