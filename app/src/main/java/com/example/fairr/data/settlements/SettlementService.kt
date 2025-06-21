package com.example.fairr.data.settlements

import com.example.fairr.data.model.Expense
import com.example.fairr.data.repository.ExpenseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

data class DebtInfo(
    val creditorId: String,
    val creditorName: String,
    val debtorId: String,
    val debtorName: String,
    val amount: Double
)

data class SettlementSummary(
    val userId: String,
    val userName: String,
    val totalOwed: Double, // Amount this user owes to others
    val totalOwedToThem: Double, // Amount others owe to this user
    val netBalance: Double // Positive means they're owed money, negative means they owe money
)

@Singleton
class SettlementService @Inject constructor(
    private val expenseRepository: ExpenseRepository
) {
    
    /**
     * Calculate who owes whom based on all expenses in a group
     */
    suspend fun calculateGroupSettlements(groupId: String): List<DebtInfo> {
        val expenses = expenseRepository.getExpensesByGroupId(groupId)
        val userBalances = mutableMapOf<String, UserBalance>()
        
        // Calculate net balances for each user
        expenses.forEach { expense ->
            calculateExpenseBalances(expense, userBalances)
        }
        
        // Convert balances to debt relationships
        return optimizeDebts(userBalances)
    }
    
    /**
     * Get settlement summary for each user in the group
     */
    suspend fun getSettlementSummary(groupId: String): List<SettlementSummary> {
        val expenses = expenseRepository.getExpensesByGroupId(groupId)
        val userBalances = mutableMapOf<String, UserBalance>()
        
        expenses.forEach { expense ->
            calculateExpenseBalances(expense, userBalances)
        }
        
        return userBalances.map { (userId, balance) ->
            val netBalance = balance.totalPaid - balance.totalOwed
            SettlementSummary(
                userId = userId,
                userName = balance.userName,
                totalOwed = balance.totalOwed,
                totalOwedToThem = balance.totalPaid,
                netBalance = netBalance
            )
        }
    }
    
    private fun calculateExpenseBalances(
        expense: Expense,
        userBalances: MutableMap<String, UserBalance>
    ) {
        // Initialize payer balance if not exists
        val payerBalance = userBalances.getOrPut(expense.paidBy) {
            UserBalance(expense.paidBy, expense.paidByName)
        }
        
        // Add the amount paid by the payer
        payerBalance.totalPaid += expense.amount
        
        // Calculate each person's share of the expense
        expense.splitBetween.forEach { split ->
            val userBalance = userBalances.getOrPut(split.userId) {
                UserBalance(split.userId, split.userName)
            }
            
            // Add to the user's total owed amount
            userBalance.totalOwed += split.share
        }
    }
    
    /**
     * Optimize debts by reducing the number of transactions needed
     * Uses a greedy algorithm to minimize the number of payments
     */
    private fun optimizeDebts(userBalances: Map<String, UserBalance>): List<DebtInfo> {
        val debts = mutableListOf<DebtInfo>()
        val balances = userBalances.mapValues { (_, balance) ->
            balance.totalPaid - balance.totalOwed
        }.toMutableMap()
        
        while (balances.values.any { abs(it) > 0.01 }) { // Using 0.01 to handle floating point precision
            // Find the person who owes the most
            val maxDebtor = balances.entries.minByOrNull { it.value }?.key ?: break
            val maxDebtAmount = balances[maxDebtor] ?: break
            
            if (maxDebtAmount >= -0.01) break // No significant debt
            
            // Find the person who is owed the most
            val maxCreditor = balances.entries.maxByOrNull { it.value }?.key ?: break
            val maxCreditAmount = balances[maxCreditor] ?: break
            
            if (maxCreditAmount <= 0.01) break // No significant credit
            
            // Calculate the settlement amount
            val settlementAmount = minOf(-maxDebtAmount, maxCreditAmount)
            
            // Create debt record
            val debtorBalance = userBalances[maxDebtor]!!
            val creditorBalance = userBalances[maxCreditor]!!
            
            debts.add(
                DebtInfo(
                    creditorId = maxCreditor,
                    creditorName = creditorBalance.userName,
                    debtorId = maxDebtor,
                    debtorName = debtorBalance.userName,
                    amount = settlementAmount
                )
            )
            
            // Update balances
            balances[maxDebtor] = (balances[maxDebtor] ?: 0.0) + settlementAmount
            balances[maxCreditor] = (balances[maxCreditor] ?: 0.0) - settlementAmount
        }
        
        return debts
    }
    
    private data class UserBalance(
        val userId: String,
        val userName: String,
        var totalPaid: Double = 0.0,
        var totalOwed: Double = 0.0
    )
} 