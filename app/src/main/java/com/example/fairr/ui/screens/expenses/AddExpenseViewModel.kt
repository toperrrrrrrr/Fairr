package com.example.fairr.ui.screens.expenses

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairr.data.repository.ExpenseRepository
import com.example.fairr.data.model.Group
import com.example.fairr.data.settings.SettingsDataStore
import com.example.fairr.util.CurrencyFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.collectLatest
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import com.example.fairr.utils.ReceiptPhoto
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import com.example.fairr.ui.screens.expenses.ValidationResult
import com.example.fairr.data.repository.GroupRepository
import androidx.lifecycle.SavedStateHandle

sealed class AddExpenseEvent {
    data class ShowError(val message: String) : AddExpenseEvent()
    data object ExpenseSaved : AddExpenseEvent()
}

data class MemberInfo(
    val userId: String,
    val displayName: String
)

data class AddExpenseState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val groupMembers: List<MemberInfo> = emptyList(),
    val userCurrency: String = "PHP",
    val expenseCurrency: String = "PHP",
    val groupCurrency: String = "PHP"
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    var state by mutableStateOf(AddExpenseState())
        private set
        
    private val _events = MutableSharedFlow<AddExpenseEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            val userCurrency = settingsDataStore.defaultCurrency.first()
            state = state.copy(userCurrency = userCurrency)
        }
    }

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getCurrencySymbol(): String {
        return CurrencyFormatter.getSymbol(state.expenseCurrency)
    }

    fun formatCurrency(amount: Double): String {
        return CurrencyFormatter.format(state.expenseCurrency, amount)
    }

    fun formatCurrencyWithSpacing(amount: Double): String {
        return CurrencyFormatter.formatWithSpacing(state.expenseCurrency, amount)
    }

    fun getGroupCurrency(): String {
        return state.expenseCurrency
    }

    fun updateExpenseCurrency(currency: String) {
        state = state.copy(expenseCurrency = currency)
    }

    fun loadGroupCurrency(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.getGroup(groupId).collectLatest { group ->
                    state = state.copy(
                        groupCurrency = group.currency,
                        expenseCurrency = group.currency // Default to group currency
                    )
                }
            } catch (e: Exception) {
                // Use default currency if group loading fails
            }
        }
    }

    /**
     * Validate expense data before saving
     */
    private fun validateExpense(
        description: String,
        amount: Double,
        paidBy: String,
        splitType: String
    ): ValidationResult {
        return when {
            description.isBlank() -> ValidationResult.Error("Description cannot be empty")
            description.length > 100 -> ValidationResult.Error("Description is too long (max 100 characters)")
            amount <= 0 -> ValidationResult.Error("Amount must be greater than 0")
            amount > 999999.99 -> ValidationResult.Error("Amount is too large")
            paidBy.isBlank() -> ValidationResult.Error("Please select who paid")
            splitType.isBlank() -> ValidationResult.Error("Please select a split type")
            state.groupMembers.isEmpty() -> ValidationResult.Error("No group members found")
            else -> ValidationResult.Success
        }
    }

    /**
     * Upload receipt photos to Firebase Storage
     */
    private suspend fun uploadReceiptPhotos(
        groupId: String,
        expenseId: String,
        receiptPhotos: List<ReceiptPhoto>
    ): List<String> {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val uploadedUrls = mutableListOf<String>()
        
        receiptPhotos.forEach { photo ->
            try {
                val photoFile = File(photo.filePath)
                if (photoFile.exists()) {
                    val photoRef = storageRef.child("receipts/$groupId/$expenseId/${photoFile.name}")
                    val uploadTask = photoRef.putFile(android.net.Uri.fromFile(photoFile))
                    val downloadUrl = uploadTask.await().storage.downloadUrl.await()
                    uploadedUrls.add(downloadUrl.toString())
                }
            } catch (e: Exception) {
                // Log error but continue with other photos
                _events.emit(AddExpenseEvent.ShowError("Failed to upload photo: ${e.message}"))
            }
        }
        
        return uploadedUrls
    }

    fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String,
        category: com.example.fairr.data.model.ExpenseCategory = com.example.fairr.data.model.ExpenseCategory.OTHER,
        isRecurring: Boolean = false,
        recurrenceRule: com.example.fairr.data.model.RecurrenceRule? = null,
        receiptPhotos: List<ReceiptPhoto> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                // Validate input first
                when (val validation = validateExpense(description, amount, paidBy, splitType)) {
                    is ValidationResult.Error -> {
                        _events.emit(AddExpenseEvent.ShowError(validation.message))
                        return@launch
                    }
                    is ValidationResult.Success -> {
                        // Continue with saving
                    }
                }

                state = state.copy(isLoading = true)
                
                // Add the expense first to get the expense ID
                expenseRepository.addExpense(
                    groupId = groupId,
                    description = description,
                    amount = amount,
                    currency = state.expenseCurrency,
                    date = date,
                    paidBy = paidBy,
                    splitType = splitType,
                    category = category,
                    isRecurring = isRecurring,
                    recurrenceRule = recurrenceRule
                )
                
                // Get the created expense to upload photos
                val expenses = expenseRepository.getExpensesByGroupId(groupId)
                val createdExpense = expenses.find { 
                    it.description == description && 
                    it.amount == amount && 
                    it.date.toDate() == date &&
                    it.paidBy == paidBy
                }
                
                // Upload receipt photos if any
                if (receiptPhotos.isNotEmpty() && createdExpense != null) {
                    val uploadedUrls = uploadReceiptPhotos(groupId, createdExpense.id, receiptPhotos)
                    
                    // Update the expense with attachment URLs
                    if (uploadedUrls.isNotEmpty()) {
                        val updatedExpense = createdExpense.copy(attachments = uploadedUrls)
                        expenseRepository.updateExpense(createdExpense, updatedExpense)
                    }
                }
                
                // If it's a recurring expense, generate instances
                if (isRecurring && recurrenceRule != null) {
                    createdExpense?.let { expense ->
                        expenseRepository.generateRecurringInstances(expense, monthsAhead = 3)
                    }
                }
                
                state = state.copy(isLoading = false)
                _events.emit(AddExpenseEvent.ExpenseSaved)
            } catch (e: Exception) {
                state = state.copy(isLoading = false)
                _events.emit(AddExpenseEvent.ShowError(e.message ?: "Failed to save expense"))
            }
        }
    }

    fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            try {
                groupRepository.getGroup(groupId).collectLatest { group ->
                    val memberInfos = group.members.map { member ->
                        MemberInfo(
                            userId = member.userId,
                            displayName = if (member.userId == getCurrentUserId()) "You" else member.name
                        )
                    }
                    state = state.copy(groupMembers = memberInfos)
                }
            } catch (e: Exception) {
                _events.emit(AddExpenseEvent.ShowError("Failed to load group members"))
            }
        }
    }

    fun getMemberIdByDisplayName(displayName: String): String {
        return state.groupMembers.find { it.displayName == displayName }?.userId ?: getCurrentUserId()
    }

    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: ""
    }
} 