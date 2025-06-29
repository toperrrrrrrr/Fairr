package com.example.fairr.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.comments.CommentService
import com.example.fairr.data.model.Comment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CommentEvent {
    data class ShowError(val message: String) : CommentEvent()
    data object CommentAdded : CommentEvent()
    data object CommentUpdated : CommentEvent()
    data object CommentDeleted : CommentEvent()
}

data class CommentState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val commentService: CommentService
) : ViewModel() {

    private val _state = MutableStateFlow(CommentState())
    val state: StateFlow<CommentState> = _state

    private val _events = MutableStateFlow<CommentEvent?>(null)
    val events: StateFlow<CommentEvent?> = _events

    fun loadComments(expenseId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                commentService.getCommentsForExpense(expenseId).collect { comments ->
                    _state.update { it.copy(comments = comments, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun addComment(expenseId: String, text: String) {
        if (text.trim().isBlank()) {
            _events.value = CommentEvent.ShowError("Comment cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                val result = commentService.addComment(expenseId, text.trim())
                if (result.isSuccess) {
                    _events.value = CommentEvent.CommentAdded
                } else {
                    _events.value = CommentEvent.ShowError(result.exceptionOrNull()?.message ?: "Failed to add comment")
                }
            } catch (e: Exception) {
                _events.value = CommentEvent.ShowError(e.message ?: "Failed to add comment")
            }
        }
    }

    fun updateComment(expenseId: String, commentId: String, newText: String) {
        if (newText.trim().isBlank()) {
            _events.value = CommentEvent.ShowError("Comment cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                val result = commentService.updateComment(expenseId, commentId, newText.trim())
                if (result.isSuccess) {
                    _events.value = CommentEvent.CommentUpdated
                } else {
                    _events.value = CommentEvent.ShowError(result.exceptionOrNull()?.message ?: "Failed to update comment")
                }
            } catch (e: Exception) {
                _events.value = CommentEvent.ShowError(e.message ?: "Failed to update comment")
            }
        }
    }

    fun deleteComment(expenseId: String, commentId: String) {
        viewModelScope.launch {
            try {
                val result = commentService.deleteComment(expenseId, commentId)
                if (result.isSuccess) {
                    _events.value = CommentEvent.CommentDeleted
                } else {
                    _events.value = CommentEvent.ShowError(result.exceptionOrNull()?.message ?: "Failed to delete comment")
                }
            } catch (e: Exception) {
                _events.value = CommentEvent.ShowError(e.message ?: "Failed to delete comment")
            }
        }
    }

    fun clearEvents() {
        _events.value = null
    }
} 