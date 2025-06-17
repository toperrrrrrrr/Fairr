package com.example.fairr.ui.screens.groups

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupResult
import com.example.fairr.data.groups.GroupService
import com.example.fairr.data.groups.GroupInviteService
import com.example.fairr.data.groups.InviteResult
import com.example.fairr.ui.model.CreateGroupData
import com.example.fairr.ui.model.GroupMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineExceptionHandler
import javax.inject.Inject

private const val TAG = "CreateGroupViewModel"

sealed class CreateGroupUiState {
    object Initial : CreateGroupUiState()
    object Loading : CreateGroupUiState()
    data class Error(val message: String) : CreateGroupUiState()
    data class Success(val groupId: String, val invitesSent: Int = 0) : CreateGroupUiState()
}

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupService: GroupService,
    private val groupInviteService: GroupInviteService
) : ViewModel() {
    var uiState by mutableStateOf<CreateGroupUiState>(CreateGroupUiState.Initial)
        private set

    var groupName by mutableStateOf("")
        private set

    var groupDescription by mutableStateOf("")
        private set

    var groupCurrency by mutableStateOf("PHP")
        private set

    private val _members = mutableStateOf<List<GroupMember>>(emptyList())
    val members: List<GroupMember> get() = _members.value

    private val _navigationEvents = MutableSharedFlow<String>()
    val navigationEvents = _navigationEvents.asSharedFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Coroutine exception", throwable)
        uiState = CreateGroupUiState.Error(throwable.message ?: "An unexpected error occurred")
    }

    fun onGroupNameChange(name: String) {
        groupName = name
    }

    fun onGroupDescriptionChange(description: String) {
        groupDescription = description
    }

    fun onGroupCurrencyChange(currency: String) {
        groupCurrency = currency
    }

    fun addMember(member: GroupMember) {
        _members.value = _members.value + member
    }

    fun removeMember(member: GroupMember) {
        _members.value = _members.value - member
    }

    fun createGroup() {
        if (groupName.isBlank()) {
            uiState = CreateGroupUiState.Error("Group name cannot be empty")
            return
        }

        viewModelScope.launch(exceptionHandler) {
            try {
                uiState = CreateGroupUiState.Loading

                val groupData = CreateGroupData(
                    name = groupName,
                    description = groupDescription,
                    currency = groupCurrency,
                    members = members
                )

                when (val result = groupService.createGroup(groupData)) {
                    is GroupResult.Success -> {
                        // Send invites to all members
                        var successfulInvites = 0
                        val failedInvites = mutableListOf<String>()
                        
                        if (members.isNotEmpty()) {
                            members.forEach { member ->
                                try {
                                    when (val inviteResult = groupInviteService.sendGroupInvite(result.groupId, member.email)) {
                                        is InviteResult.Success -> {
                                            successfulInvites++
                                        }
                                        is InviteResult.Error -> {
                                            failedInvites.add("${member.email}: ${inviteResult.message}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error sending invite to ${member.email}", e)
                                    failedInvites.add("${member.email}: ${e.message ?: "Unknown error"}")
                                }
                            }
                        }
                        
                        if (failedInvites.isNotEmpty()) {
                            uiState = CreateGroupUiState.Error(
                                "Group created but some invites failed:\n${failedInvites.joinToString("\n")}"
                            )
                        } else {
                            uiState = CreateGroupUiState.Success(result.groupId, successfulInvites)
                            _navigationEvents.emit(result.groupId)
                        }
                    }
                    is GroupResult.Error -> {
                        uiState = CreateGroupUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating group", e)
                uiState = CreateGroupUiState.Error(e.message ?: "Failed to create group")
            }
        }
    }

    fun resetState() {
        uiState = CreateGroupUiState.Initial
        groupName = ""
        groupDescription = ""
        groupCurrency = "PHP"
        _members.value = emptyList()
    }
} 