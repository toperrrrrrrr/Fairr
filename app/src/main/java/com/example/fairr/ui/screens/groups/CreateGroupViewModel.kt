package com.example.fairr.ui.screens.groups

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.groups.GroupResult
import com.example.fairr.data.groups.GroupService
import com.example.fairr.ui.model.CreateGroupData
import com.example.fairr.ui.model.GroupMember
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CreateGroupUiState {
    object Initial : CreateGroupUiState()
    object Loading : CreateGroupUiState()
    data class Error(val message: String) : CreateGroupUiState()
    data class Success(val groupId: String) : CreateGroupUiState()
}

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val groupService: GroupService
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

        viewModelScope.launch {
            uiState = CreateGroupUiState.Loading

            val groupData = CreateGroupData(
                name = groupName,
                description = groupDescription,
                currency = groupCurrency,
                members = members
            )

            when (val result = groupService.createGroup(groupData)) {
                is GroupResult.Success -> {
                    uiState = CreateGroupUiState.Success(result.groupId)
                    _navigationEvents.emit(result.groupId)
                }
                is GroupResult.Error -> {
                    uiState = CreateGroupUiState.Error(result.message)
                }
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