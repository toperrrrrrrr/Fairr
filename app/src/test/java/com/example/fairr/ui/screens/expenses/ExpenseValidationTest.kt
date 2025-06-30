package com.example.fairr.ui.screens.expenses

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import com.example.fairr.ui.screens.expenses.validateSplitData

@RunWith(MockitoJUnitRunner::class)
class ExpenseValidationTest {

    @Test
    fun `validateSplitData should return null for Equal Split`() {
        val memberSplits = emptyList<Map<String, Any>>()
        val result = validateSplitData("Equal Split", 100.0, memberSplits)
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return null for Percentage when totals 100 percent`() {
        val memberSplits = listOf(
            mapOf("percentage" to 50.0),
            mapOf("percentage" to 50.0)
        )
        val result = validateSplitData("Percentage", 100.0, memberSplits)
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return error for Percentage when totals less than 100`() {
        val memberSplits = listOf(
            mapOf("percentage" to 30.0),
            mapOf("percentage" to 50.0)
        )
        val result = validateSplitData("Percentage", 100.0, memberSplits)
        assertNotNull(result)
        assertTrue(result!!.contains("Total percentage must be 100%"))
    }

    @Test
    fun `validateSplitData should return null for Custom Amount when totals match expense`() {
        val memberSplits = listOf(
            mapOf("customAmount" to 60.0),
            mapOf("customAmount" to 40.0)
        )
        val result = validateSplitData("Custom Amount", 100.0, memberSplits)
        assertNull(result)
    }

    @Test
    fun `validateSplitData should return error for Custom Amount when totals exceed expense`() {
        val memberSplits = listOf(
            mapOf("customAmount" to 70.0),
            mapOf("customAmount" to 50.0)
        )
        val result = validateSplitData("Custom Amount", 100.0, memberSplits)
        assertNotNull(result)
        assertTrue(result!!.contains("Total custom amounts cannot exceed"))
    }

    @Test
    fun `validateSplitData should return null for unknown split type`() {
        val memberSplits = emptyList<Map<String, Any>>()
        val result = validateSplitData("Unknown Split", 100.0, memberSplits)
        assertNull(result)
    }
} 