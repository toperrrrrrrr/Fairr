package com.example.fairr.data.settlements

/**
 * Example of how settlement calculations work:
 * 
 * Let's say we have 3 people: Alice, Bob, Charlie
 * 
 * Expenses:
 * 1. Alice paid $120 for dinner, split equally (each owes $40)
 * 2. Bob paid $60 for taxi, split equally (each owes $20)  
 * 3. Charlie paid $90 for drinks, split equally (each owes $30)
 * 
 * Total spent: $270
 * Each person should pay: $90
 * 
 * What each person paid:
 * - Alice: $120
 * - Bob: $60
 * - Charlie: $90
 * 
 * What each person owes:
 * - Alice: $90 (from all expenses)
 * - Bob: $90 (from all expenses)
 * - Charlie: $90 (from all expenses)
 * 
 * Net balances:
 * - Alice: +$30 (paid $120, owes $90)
 * - Bob: -$30 (paid $60, owes $90)
 * - Charlie: $0 (paid $90, owes $90)
 * 
 * Optimized settlements:
 * - Bob owes Alice $30
 * 
 * This is exactly what our SettlementService calculates!
 */
data class SettlementExample(
    val description: String,
    val expenses: List<ExpenseExample>,
    val expectedSettlements: List<DebtExample>
)

data class ExpenseExample(
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitAmong: List<String>
)

data class DebtExample(
    val from: String,
    val to: String,
    val amount: Double
)

object SettlementExamples {
    val simpleThreePersonExample = SettlementExample(
        description = "Three friends sharing expenses",
        expenses = listOf(
            ExpenseExample("Dinner", 120.0, "Alice", listOf("Alice", "Bob", "Charlie")),
            ExpenseExample("Taxi", 60.0, "Bob", listOf("Alice", "Bob", "Charlie")),
            ExpenseExample("Drinks", 90.0, "Charlie", listOf("Alice", "Bob", "Charlie"))
        ),
        expectedSettlements = listOf(
            DebtExample("Bob", "Alice", 30.0)
        )
    )
    
    val complexFourPersonExample = SettlementExample(
        description = "Four friends with various expenses",
        expenses = listOf(
            ExpenseExample("Hotel", 200.0, "Alice", listOf("Alice", "Bob", "Charlie", "David")),
            ExpenseExample("Car rental", 120.0, "Bob", listOf("Alice", "Bob", "Charlie", "David")),
            ExpenseExample("Groceries", 80.0, "Charlie", listOf("Alice", "Bob", "Charlie", "David")),
            ExpenseExample("Gas", 40.0, "David", listOf("Alice", "Bob", "Charlie", "David"))
        ),
        expectedSettlements = listOf(
            DebtExample("Bob", "Alice", 20.0),
            DebtExample("Charlie", "Alice", 30.0),
            DebtExample("David", "Alice", 60.0)
        )
    )
} 