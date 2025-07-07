package com.example.fairr.data.category

import android.util.Log
import com.example.fairr.data.model.ExpenseCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class CustomCategory(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val icon: String = "📦", // Emoji icon
    val color: String = "#95A5A6",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Singleton
class CategoryService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "CategoryService"
        private const val CUSTOM_CATEGORIES_COLLECTION = "custom_categories"
    }

    private val _customCategories = MutableStateFlow<List<CustomCategory>>(emptyList())
    val customCategories: StateFlow<List<CustomCategory>> = _customCategories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var customCategoriesListener: ListenerRegistration? = null

    init {
        startListeningToCustomCategories()
    }

    /**
     * Get all available categories (default + custom)
     */
    fun getAllAvailableCategories(): List<CategoryItem> {
        val defaultCategories = ExpenseCategory.getAllCategories().map { category ->
            CategoryItem(
                id = category.name,
                name = category.displayName,
                icon = category.icon,
                color = category.color,
                isDefault = true,
                isActive = true
            )
        }

        val customCategoryItems = _customCategories.value.filter { it.isActive }.map { category ->
            CategoryItem(
                id = category.id,
                name = category.name,
                icon = category.icon,
                color = category.color,
                isDefault = false,
                isActive = category.isActive
            )
        }

        return defaultCategories + customCategoryItems
    }

    /**
     * Get category by ID (works for both default and custom categories)
     */
    fun getCategoryById(categoryId: String): CategoryItem? {
        // Check if it's a default category
        ExpenseCategory.values().find { it.name == categoryId }?.let { category ->
            return CategoryItem(
                id = category.name,
                name = category.displayName,
                icon = category.icon,
                color = category.color,
                isDefault = true,
                isActive = true
            )
        }

        // Check custom categories
        return _customCategories.value.find { it.id == categoryId }?.let { category ->
            CategoryItem(
                id = category.id,
                name = category.name,
                icon = category.icon,
                color = category.color,
                isDefault = false,
                isActive = category.isActive
            )
        }
    }

    /**
     * Create a new custom category
     */
    suspend fun createCustomCategory(
        name: String,
        icon: String,
        color: String
    ): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            _isLoading.value = true
            
            val category = CustomCategory(
                id = generateCategoryId(),
                userId = userId,
                name = name.trim(),
                icon = icon,
                color = color,
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            firestore.collection(CUSTOM_CATEGORIES_COLLECTION)
                .document(category.id)
                .set(category)
                .await()

            Log.d(TAG, "Custom category created: ${category.name}")
            Result.success(category.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating custom category", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Update an existing custom category
     */
    suspend fun updateCustomCategory(
        categoryId: String,
        name: String,
        icon: String,
        color: String
    ): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            _isLoading.value = true
            
            // Verify the category belongs to the user
            val existingCategory = _customCategories.value.find { it.id == categoryId }
            if (existingCategory == null || existingCategory.userId != userId) {
                return Result.failure(Exception("Category not found or access denied"))
            }

            val updates = mapOf(
                "name" to name.trim(),
                "icon" to icon,
                "color" to color,
                "updatedAt" to System.currentTimeMillis()
            )

            firestore.collection(CUSTOM_CATEGORIES_COLLECTION)
                .document(categoryId)
                .update(updates)
                .await()

            Log.d(TAG, "Custom category updated: $categoryId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating custom category", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Delete a custom category (soft delete)
     */
    suspend fun deleteCustomCategory(categoryId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            _isLoading.value = true
            
            // Verify the category belongs to the user
            val existingCategory = _customCategories.value.find { it.id == categoryId }
            if (existingCategory == null || existingCategory.userId != userId) {
                return Result.failure(Exception("Category not found or access denied"))
            }

            // Soft delete by setting isActive to false
            firestore.collection(CUSTOM_CATEGORIES_COLLECTION)
                .document(categoryId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()

            Log.d(TAG, "Custom category deleted: $categoryId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting custom category", e)
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * Get usage statistics for a category
     */
    suspend fun getCategoryUsageStats(categoryId: String): CategoryUsageStats {
        return try {
            val userId = auth.currentUser?.uid ?: return CategoryUsageStats()
            
            // Query expenses that use this category
            val expensesQuery = firestore.collection("expenses")
                .whereEqualTo("category", categoryId)
                .whereArrayContains("splitBetween.userId", userId)
                .get()
                .await()

            val expenses = expensesQuery.documents
            val totalExpenses = expenses.size
            val totalAmount = expenses.sumOf { doc ->
                (doc.data?.get("amount") as? Number)?.toDouble() ?: 0.0
            }

            CategoryUsageStats(
                totalExpenses = totalExpenses,
                totalAmount = totalAmount,
                lastUsed = expenses.maxOfOrNull { doc ->
                    (doc.data?.get("date") as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L
                } ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category usage stats", e)
            CategoryUsageStats()
        }
    }

    /**
     * Start listening to custom categories for the current user
     */
    private fun startListeningToCustomCategories() {
        val userId = auth.currentUser?.uid ?: return

        customCategoriesListener = firestore.collection(CUSTOM_CATEGORIES_COLLECTION)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to custom categories", error)
                    return@addSnapshotListener
                }

                val categories = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        CustomCategory(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            name = data["name"] as? String ?: "",
                            icon = data["icon"] as? String ?: "📦",
                            color = data["color"] as? String ?: "#95A5A6",
                            isActive = data["isActive"] as? Boolean ?: true,
                            createdAt = data["createdAt"] as? Long ?: 0L,
                            updatedAt = data["updatedAt"] as? Long ?: 0L
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing custom category", e)
                        null
                    }
                } ?: emptyList()

                _customCategories.value = categories
            }
    }

    /**
     * Stop listening to custom categories
     */
    fun stopListening() {
        customCategoriesListener?.remove()
        customCategoriesListener = null
    }

    /**
     * Generate a unique category ID
     */
    private fun generateCategoryId(): String {
        return "custom_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Unified category item that works for both default and custom categories
 */
data class CategoryItem(
    val id: String,
    val name: String,
    val icon: String, // Emoji icon
    val color: String, // Hex color
    val isDefault: Boolean,
    val isActive: Boolean
)

/**
 * Category usage statistics
 */
data class CategoryUsageStats(
    val totalExpenses: Int = 0,
    val totalAmount: Double = 0.0,
    val lastUsed: Long = 0L
) 