package com.example.fairr.ui.screens.expenses

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ExpenseValidationTest {

    @Test
    fun `validateSplitData should return null for Equal Split`() {
        // When
        val result = validateSplitData("Equal Split", 100.0)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return null for Percentage when UI not implemented`() {
        // When
        val result = validateSplitData("Percentage", 100.0)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return null for Custom Amount when UI not implemented`() {
        // When
        val result = validateSplitData("Custom Amount", 100.0)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return null for unknown split type`() {
        // When
        val result = validateSplitData("Unknown Split", 100.0)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitInputs should validate percentage split correctly`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("percentage" to 60.0),
            mapOf("percentage" to 40.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result) // Should be valid (100% total)
    }

    @Test
    fun `validateSplitInputs should return error for percentage under 100`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("percentage" to 60.0),
            mapOf("percentage" to 30.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total percentage must be 100%"))
        assertTrue(result.contains("90.0%"))
    }

    @Test
    fun `validateSplitInputs should return error for percentage over 100`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("percentage" to 60.0),
            mapOf("percentage" to 50.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total percentage cannot exceed 100%"))
        assertTrue(result.contains("110.0%"))
    }

    @Test
    fun `validateSplitInputs should validate custom amount split correctly`() {
        // Given
        val splitType = "Custom Amount"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 60.0),
            mapOf("customAmount" to 40.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result) // Should be valid (100.0 total)
    }

    @Test
    fun `validateSplitInputs should return error for custom amount under total`() {
        // Given
        val splitType = "Custom Amount"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 60.0),
            mapOf("customAmount" to 30.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total custom amounts must equal the expense amount"))
        assertTrue(result.contains("90.00 vs 100.00"))
    }

    @Test
    fun `validateSplitInputs should return error for custom amount over total`() {
        // Given
        val splitType = "Custom Amount"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 60.0),
            mapOf("customAmount" to 50.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total custom amounts cannot exceed the expense amount"))
        assertTrue(result.contains("110.00 vs 100.00"))
    }

    @Test
    fun `validateSplitInputs should handle empty member splits`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = emptyList<Map<String, Any>>()
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total percentage must be 100%"))
        assertTrue(result.contains("0.0%"))
    }

    @Test
    fun `validateSplitInputs should handle null values in member splits`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("percentage" to 0.0),
            mapOf("percentage" to 50.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total percentage must be 100%"))
        assertTrue(result.contains("50.0%"))
    }

    @Test
    fun `validateSplitInputs should handle missing keys in member splits`() {
        // Given
        val splitType = "Custom Amount"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 50.0),
            mapOf("otherKey" to 50.0) // Missing customAmount key
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNotNull(result)
        assertTrue(result!!.contains("Total custom amounts must equal the expense amount"))
        assertTrue(result.contains("50.00 vs 100.00"))
    }

    @Test
    fun `validateSplitInputs should return null for unknown split type`() {
        // Given
        val splitType = "Unknown Split"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 50.0),
            mapOf("customAmount" to 50.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result)
    }

    @Test
    fun `validateSplitInputs should handle edge case with 99 9 percent tolerance`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("percentage" to 99.9)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result) // Should be valid within tolerance
    }

    @Test
    fun `validateSplitInputs should handle edge case with 100 1 percent tolerance`() {
        // Given
        val splitType = "Percentage"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("percentage" to 100.1)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result) // Should be valid within tolerance
    }

    @Test
    fun `validateSplitInputs should handle edge case with 99 percent tolerance for custom amounts`() {
        // Given
        val splitType = "Custom Amount"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 99.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result) // Should be valid within tolerance (99% of 100 = 99, which is >= 99)
    }

    @Test
    fun `validateSplitInputs should handle edge case with 101 percent tolerance for custom amounts`() {
        // Given
        val splitType = "Custom Amount"
        val amount = 100.0
        val memberSplits = listOf(
            mapOf("customAmount" to 101.0)
        )
        
        // When
        val result = validateSplitInputs(splitType, amount, memberSplits)
        
        // Then
        assertNull(result) // Should be valid within tolerance (101% of 100 = 101, which is <= 101)
    }
} 