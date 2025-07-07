package com.example.fairr.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.category.CategoryItem
import com.example.fairr.data.category.CategoryService
import com.example.fairr.data.category.CategoryUsageStats
import com.example.fairr.data.model.ExpenseCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val categoryService: CategoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _categoryUsageStats = MutableStateFlow<Map<String, CategoryUsageStats>>(emptyMap())
    val categoryUsageStats: StateFlow<Map<String, CategoryUsageStats>> = _categoryUsageStats.asStateFlow()

    init {
        loadCategories()
        observeCategories()
    }

    /**
     * Load all available categories
     */
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val allCategories = categoryService.getAllAvailableCategories()
                _categories.value = allCategories
                
                // Load usage stats for each category
                loadCategoryUsageStats(allCategories)
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load categories: ${e.message}"
                )
            }
        }
    }

    /**
     * Observe changes in custom categories
     */
    private fun observeCategories() {
        viewModelScope.launch {
            combine(
                categoryService.customCategories,
                categoryService.isLoading
            ) { customCategories, isLoading ->
                val allCategories = categoryService.getAllAvailableCategories()
                _categories.value = allCategories
                
                _uiState.value = _uiState.value.copy(
                    isLoading = isLoading
                )
            }
        }
    }

    /**
     * Load usage statistics for all categories
     */
    private fun loadCategoryUsageStats(categories: List<CategoryItem>) {
        viewModelScope.launch {
            val statsMap = mutableMapOf<String, CategoryUsageStats>()
            
            categories.forEach { category ->
                try {
                    val stats = categoryService.getCategoryUsageStats(category.id)
                    statsMap[category.id] = stats
                } catch (e: Exception) {
                    // Continue loading other stats even if one fails
                }
            }
            
            _categoryUsageStats.value = statsMap
        }
    }

    /**
     * Create a new custom category
     */
    fun createCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = categoryService.createCustomCategory(name, icon, color)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        successMessage = "Category '$name' created successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to create category: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create category: ${e.message}"
                )
            }
        }
    }

    /**
     * Update an existing custom category
     */
    fun updateCategory(categoryId: String, name: String, icon: String, color: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val result = categoryService.updateCustomCategory(categoryId, name, icon, color)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        successMessage = "Category updated successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to update category: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update category: ${e.message}"
                )
            }
        }
    }

    /**
     * Delete a custom category
     */
    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val category = _categories.value.find { it.id == categoryId }
                val result = categoryService.deleteCustomCategory(categoryId)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = null,
                        successMessage = "Category '${category?.name}' deleted successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to delete category: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete category: ${e.message}"
                )
            }
        }
    }

    /**
     * Get default category options for quick setup
     */
    fun getDefaultCategoryOptions(): List<CategoryOption> {
        return listOf(
            CategoryOption("Food & Dining", "🍽️", "#FF6B6B"),
            CategoryOption("Transportation", "🚗", "#4ECDC4"),
            CategoryOption("Entertainment", "🎬", "#96CEB4"),
            CategoryOption("Shopping", "🛍️", "#FFEAA7"),
            CategoryOption("Utilities", "⚡", "#DDA0DD"),
            CategoryOption("Healthcare", "🏥", "#F7DC6F"),
            CategoryOption("Education", "📚", "#BB8FCE"),
            CategoryOption("Travel", "✈️", "#85C1E9"),
            CategoryOption("Work", "💼", "#F8C471"),
            CategoryOption("Gaming", "🎮", "#E74C3C"),
            CategoryOption("Sports", "⚽", "#2ECC71"),
            CategoryOption("Beauty", "💄", "#E91E63"),
            CategoryOption("Pets", "🐾", "#8E44AD"),
            CategoryOption("Home", "🏠", "#98D8C8"),
            CategoryOption("Coffee", "☕", "#8B4513"),
            CategoryOption("Books", "📖", "#4169E1"),
            CategoryOption("Movies", "🎭", "#FF1493"),
            CategoryOption("Music", "🎵", "#9370DB"),
            CategoryOption("Gym", "💪", "#32CD32"),
            CategoryOption("Other", "📦", "#95A5A6")
        )
    }

    /**
     * Get available icon options
     */
    fun getIconOptions(): List<String> {
        return listOf(
            "🍽️", "🚗", "🎬", "🛍️", "⚡", "🏥", "📚", "✈️", "💼", "🎮",
            "⚽", "💄", "🐾", "🏠", "☕", "📖", "🎭", "🎵", "💪", "📦",
            "🎯", "🎪", "🎨", "🎸", "🎹", "🎺", "🎻", "🏆", "🏅", "🏀",
            "🏈", "🏐", "🏓", "🏸", "🥎", "🥏", "🥊", "🥋", "🥅", "🥇",
            "🍕", "🍔", "🍟", "🌭", "🍿", "🧂", "🥓", "🥚", "🧀", "🥞",
            "🛒", "🛍️", "🎁", "🎀", "🎊", "🎉", "🎈", "🎂", "🍰", "🧁"
        )
    }

    /**
     * Get available color options
     */
    fun getColorOptions(): List<String> {
        return listOf(
            "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4", "#FFEAA7", "#DDA0DD",
            "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E9", "#F8C471", "#E74C3C",
            "#2ECC71", "#E91E63", "#8E44AD", "#95A5A6", "#3498DB", "#E67E22",
            "#9B59B6", "#1ABC9C", "#F39C12", "#D35400", "#C0392B", "#8E44AD",
            "#2980B9", "#27AE60", "#F1C40F", "#E74C3C", "#9B59B6", "#34495E"
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Clear success message
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Refresh categories
     */
    fun refresh() {
        loadCategories()
    }

    override fun onCleared() {
        super.onCleared()
        categoryService.stopListening()
    }
}

/**
 * UI state for category management
 */
data class CategoryManagementUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * Category option for quick setup
 */
data class CategoryOption(
    val name: String,
    val icon: String,
    val color: String
) 