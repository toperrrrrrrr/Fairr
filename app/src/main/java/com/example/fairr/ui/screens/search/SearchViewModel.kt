package com.example.fairr.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<SearchResult> = emptyList(),
    val userCurrency: String = "PHP"
)

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {
    
    var uiState by mutableStateOf(SearchUiState())
        private set

    fun search(
        query: String,
        filter: SearchFilter,
        category: String,
        dateRange: String,
        sortBy: SortOption
    ) {
        if (query.isBlank()) {
            uiState = uiState.copy(searchResults = emptyList())
            return
        }

        viewModelScope.launch {
            try {
                uiState = uiState.copy(isLoading = true, error = null)
                
                // For now, return empty results. This will be implemented with real data later
                val results = emptyList<SearchResult>()
                
                uiState = uiState.copy(
                    isLoading = false,
                    searchResults = results
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun clearSearch() {
        uiState = uiState.copy(searchResults = emptyList())
    }
} 