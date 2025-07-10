package com.example.fairr.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.repository.GroupRepository
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.Group
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import com.example.fairr.ui.screens.search.SearchResult
import com.example.fairr.ui.screens.search.SearchFilter
import com.example.fairr.ui.screens.search.SortOption
import kotlinx.coroutines.delay

data class SearchUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchResults: List<SearchResult> = emptyList(),
    val userCurrency: String = "PHP"
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth
) : ViewModel() {
    
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
                
                val results = mutableListOf<SearchResult>()
                val searchQuery = query.lowercase().trim()
                
                when (filter) {
                    SearchFilter.ALL -> {
                        // Search both expenses and groups
                        results.addAll(searchExpenses(searchQuery, category, dateRange, sortBy))
                        results.addAll(searchGroups(searchQuery))
                    }
                    SearchFilter.EXPENSES -> {
                        results.addAll(searchExpenses(searchQuery, category, dateRange, sortBy))
                    }
                    SearchFilter.GROUPS -> {
                        results.addAll(searchGroups(searchQuery))
                    }
                }
                
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

    private suspend fun searchExpenses(
        query: String,
        category: String,
        dateRange: String,
        sortBy: SortOption
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            // Get user's groups first
            val userGroups = groupRepository.getUserGroups().first()
            
            // Search expenses in each group
            userGroups.forEach { group ->
                val expenses = expenseRepository.getExpensesByGroupId(group.id)
                
                expenses.forEach { expense ->
                    // Apply search filters
                    if (matchesExpenseSearch(expense, query, category, dateRange)) {
                        results.add(SearchResult.ExpenseResult(
                            id = expense.id,
                            description = expense.description,
                            amount = expense.amount,
                            date = formatDate(expense.date.toDate()),
                            category = expense.category.name,
                            groupName = group.name
                        ))
                    }
                }
            }
            
            // return results.sortedBy { it.name }
            return results
        } catch (e: Exception) {
            // Return empty list if search fails
            return emptyList()
        }
    }

    private suspend fun searchGroups(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        
        try {
            val userGroups = groupRepository.getUserGroups().first()
            
            userGroups.forEach { group ->
                if (group.name.lowercase().contains(query) || 
                    group.description.lowercase().contains(query)) {
                    
                    // Calculate actual expense count and balance for the group
                    val groupExpenses = expenseRepository.getExpensesByGroupId(group.id)
                    val expenseCount = groupExpenses.size
                    
                    // Calculate user's balance in this group
                    val currentUserId = auth.currentUser?.uid ?: ""
                    val userBalance = calculateUserBalanceInGroup(groupExpenses, currentUserId)
                    
                    results.add(SearchResult.GroupResult(
                        id = group.id,
                        name = group.name,
                        memberCount = group.members.size,
                        expenseCount = expenseCount,
                        balance = userBalance
                    ))
                }
            }
            
            return results
        } catch (e: Exception) {
            return emptyList()
        }
    }

    private fun calculateUserBalanceInGroup(expenses: List<Expense>, userId: String): Double {
        var balance = 0.0
        
        expenses.forEach { expense ->
            // Amount the user paid
            val amountPaid = if (expense.paidBy == userId) expense.amount else 0.0
            
            // Amount the user owes
            val userSplit = expense.splitBetween.find { it.userId == userId }
            val amountOwed = userSplit?.share ?: 0.0
            
            // User's balance = amount paid - amount owed
            balance += amountPaid - amountOwed
        }
        
        return balance
    }

    private fun matchesExpenseSearch(
        expense: Expense,
        query: String,
        category: String,
        dateRange: String
    ): Boolean {
        // Text search
        val matchesText = expense.description.lowercase().contains(query) ||
                         expense.notes.lowercase().contains(query)
        
        if (!matchesText) return false
        
        // Category filter
        if (category != "All Categories") {
            val expenseCategory = expense.category.name.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            if (expenseCategory != category) return false
        }
        
        // Date range filter
        if (dateRange != "All Time") {
            val expenseDate = expense.date.toDate()
            val currentDate = Date()
            val calendar = Calendar.getInstance()
            
            when (dateRange) {
                "Last 7 Days" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    if (expenseDate.before(calendar.time)) return false
                }
                "Last 30 Days" -> {
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    if (expenseDate.before(calendar.time)) return false
                }
                "Last 3 Months" -> {
                    calendar.add(Calendar.MONTH, -3)
                    if (expenseDate.before(calendar.time)) return false
                }
                "This Year" -> {
                    calendar.set(Calendar.DAY_OF_YEAR, 1)
                    if (expenseDate.before(calendar.time)) return false
                }
            }
        }
        
        return true
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }

    fun clearSearch() {
        uiState = uiState.copy(searchResults = emptyList())
    }

    private fun formatSearchResult(text: String): String {
        return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
} 