package com.example.fairr.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.activity.ActivityService
import com.example.fairr.data.model.GroupActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ActivityEvent {
    data class ShowError(val message: String) : ActivityEvent()
    data object ActivitiesLoaded : ActivityEvent()
}

data class ActivityState(
    val activities: List<GroupActivity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityService: ActivityService
) : ViewModel() {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state

    private val _events = MutableStateFlow<ActivityEvent?>(null)
    val events: StateFlow<ActivityEvent?> = _events

    fun loadActivities(groupId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                activityService.getActivitiesForGroup(groupId).collect { activities ->
                    _state.update { it.copy(activities = activities, isLoading = false) }
                    _events.value = ActivityEvent.ActivitiesLoaded
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _events.value = ActivityEvent.ShowError(e.message ?: "Failed to load activities")
            }
        }
    }

    fun clearEvents() {
        _events.value = null
    }
} 