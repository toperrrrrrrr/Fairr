package com.example.fairr.data.repository

import org.junit.Assert.*
import org.junit.Test

class SplitCalculatorTest {

    @Test
    fun `equal split divides amount equally among members`() {
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice"),
            mapOf("userId" to "2", "name" to "Bob")
        )
        val splits = SplitCalculator.calculateSplits(100.0, "Equal Split", members)
        assertEquals(2, splits.size)
        assertTrue(splits.all { it["share"] == 50.0 })
    }

    @Test
    fun `percentage split divides amount by given percentages`() {
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "percentage" to 70),
            mapOf("userId" to "2", "name" to "Bob", "percentage" to 30)
        )
        val splits = SplitCalculator.calculateSplits(200.0, "Percentage", members)
        assertEquals(140.0, splits[0]["share"])
        assertEquals(60.0, splits[1]["share"])
    }

    @Test
    fun `percentage split with invalid total falls back to equal`() {
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "percentage" to 60),
            mapOf("userId" to "2", "name" to "Bob", "percentage" to 20)
        )
        val splits = SplitCalculator.calculateSplits(100.0, "Percentage", members)
        assertTrue(splits.all { it["share"] == 50.0 })
    }

    @Test
    fun `custom amount split uses specified and fills rest equally`() {
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "customAmount" to 30.0),
            mapOf("userId" to "2", "name" to "Bob")
        )
        val splits = SplitCalculator.calculateSplits(100.0, "Custom Amount", members)
        assertEquals(30.0, splits[0]["share"])
        assertEquals(70.0, splits[1]["share"])
    }

    @Test
    fun `custom amount split with over total clamps to total`() {
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice", "customAmount" to 120.0),
            mapOf("userId" to "2", "name" to "Bob")
        )
        val splits = SplitCalculator.calculateSplits(100.0, "Custom Amount", members)
        val total = splits.sumOf { it["share"] as Double }
        assertEquals(100.0, total, 0.01)
    }

    @Test
    fun `empty members returns empty list`() {
        val splits = SplitCalculator.calculateSplits(100.0, "Equal Split", emptyList())
        assertTrue(splits.isEmpty())
    }

    @Test
    fun `zero amount returns zero shares`() {
        val members = listOf(
            mapOf("userId" to "1", "name" to "Alice"),
            mapOf("userId" to "2", "name" to "Bob")
        )
        val splits = SplitCalculator.calculateSplits(0.0, "Equal Split", members)
        assertTrue(splits.all { it["share"] == 0.0 })
    }
} 