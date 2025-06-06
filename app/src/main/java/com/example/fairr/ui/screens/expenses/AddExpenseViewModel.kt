package com.example.fairr.ui.screens.expenses

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor() : ViewModel() {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }

    fun getCurrencySymbol(): String {
        // TODO: Get this from group settings
        return "$"
    }

    fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date
    ) {
        // TODO: Implement expense addition with Firebase
    }
} 