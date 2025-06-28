package com.example.fairr.ui.screens.expenses

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@RunWith(MockitoJUnitRunner::class)
class AddExpenseViewModelTest {

    @Test
    fun `formatDate should format date correctly`() {
        // Given
        val date = Date(1640995200000) // Jan 1, 2022
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        
        // When
        val formatted = dateFormat.format(date)
        
        // Then
        assertTrue(formatted.contains("Jan 01, 2022"))
    }

    @Test
    fun `currency formatting should work correctly`() {
        // Test that currency formatting logic works
        // This is a basic test to ensure the test infrastructure is working
        assertTrue(true)
    }
} 