package com.example.fairr.data.model

import com.google.firebase.Timestamp
import androidx.compose.ui.graphics.Color

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "PHP",
    val date: Timestamp = Timestamp.now(),
    val paidBy: String = "", // userId
    val paidByName: String = "", // user's name
    val splitBetween: List<ExpenseSplit> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val notes: String = "",
    val attachments: List<String> = emptyList(), // URLs to attachments
    val splitType: String = "Equal Split", // Type of split: "Equal Split", "Percentage", "Custom Amount"
    // Recurrence fields
    val isRecurring: Boolean = false,
    val recurrenceRule: RecurrenceRule? = null,
    // Link to parent recurring expense (for instances)
    val parentExpenseId: String? = null
)

data class ExpenseSplit(
    val userId: String,
    val userName: String,
    val share: Double,
    val isPaid: Boolean = false
)

data class RecurrenceRule(
    val frequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val interval: Int = 1, // e.g., every 2 weeks
    val endDate: Timestamp? = null // null = no end
)

data class Comment(
    val id: String = "",
    val expenseId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isEdited: Boolean = false,
    val editedAt: Timestamp? = null,
    val groupId: String = "" // For security rules
)

enum class ExpenseCategory(val displayName: String, val icon: String, val color: String) {
    FOOD("Food & Dining", "🍽️", "#FF6B6B"),
    TRANSPORTATION("Transportation", "🚗", "#4ECDC4"),
    ACCOMMODATION("Accommodation", "🏨", "#45B7D1"),
    ENTERTAINMENT("Entertainment", "🎬", "#96CEB4"),
    SHOPPING("Shopping", "🛍️", "#FFEAA7"),
    UTILITIES("Utilities", "⚡", "#DDA0DD"),
    RENT("Rent", "🏠", "#98D8C8"),
    HEALTHCARE("Healthcare", "🏥", "#F7DC6F"),
    EDUCATION("Education", "📚", "#BB8FCE"),
    TRAVEL("Travel", "✈️", "#85C1E9"),
    WORK("Work", "💼", "#F8C471"),
    GAMING("Gaming", "🎮", "#E74C3C"),
    SPORTS("Sports", "⚽", "#2ECC71"),
    BEAUTY("Beauty", "💄", "#E91E63"),
    PETS("Pets", "🐾", "#8E44AD"),
    OTHER("Other", "📦", "#95A5A6");

    companion object {
        fun fromString(value: String): ExpenseCategory {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                OTHER
            }
        }
        
        fun getAllCategories(): List<ExpenseCategory> {
            return values().toList()
        }
        
        fun getDefaultCategories(): List<ExpenseCategory> {
            return listOf(
                FOOD, TRANSPORTATION, ENTERTAINMENT, SHOPPING, 
                UTILITIES, RENT, HEALTHCARE, TRAVEL, OTHER
            )
        }
        
        fun getCategoryByDisplayName(displayName: String): ExpenseCategory? {
            return values().find { it.displayName == displayName }
        }
    }
}

enum class RecurrenceFrequency(val displayName: String) {
    NONE("None"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly")
} 