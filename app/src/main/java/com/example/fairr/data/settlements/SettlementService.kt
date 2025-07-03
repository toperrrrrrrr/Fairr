package com.example.fairr.data.settlements

import android.util.Log
import com.example.fairr.data.model.Expense
import com.example.fairr.data.repository.ExpenseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlinx.coroutines.tasks.await

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
    private val expenseRepository: ExpenseRepository,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
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
            
            // Create debt record safely
            val debtorBalance = userBalances[maxDebtor]
            val creditorBalance = userBalances[maxCreditor]
            
            if (debtorBalance != null && creditorBalance != null) {
                debts.add(
                    DebtInfo(
                        creditorId = maxCreditor,
                        creditorName = creditorBalance.userName,
                        debtorId = maxDebtor,
                        debtorName = debtorBalance.userName,
                        amount = settlementAmount
                    )
                )
            } else {
                // Skip this iteration if user balance data is missing
                Log.w("SettlementService", "Missing user balance data for debtor: $maxDebtor or creditor: $maxCreditor")
                break
            }
            
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

    /**
     * Records a settlement between two users and marks the corresponding splits as paid.
     */
    suspend fun recordSettlement(
        groupId: String,
        payerId: String, // user who pays (debtor)
        payeeId: String, // user who receives money (creditor)
        amount: Double,
        paymentMethod: String = "cash"
    ) {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            throw IllegalStateException("User must be authenticated to record settlement")
        }
        
        // Validate that current user is either payer or payee
        if (currentUserId != payerId && currentUserId != payeeId) {
            throw IllegalArgumentException("User can only record settlements they are involved in")
        }
        
        // 1. Create settlement document with required fields for security rules
        val settlementData = hashMapOf(
            "groupId" to groupId,
            "payerId" to payerId,
            "payeeId" to payeeId,
            "amount" to amount,
            "paymentMethod" to paymentMethod,
            "createdAt" to Timestamp.now(),
            "createdBy" to currentUserId, // Required for security rules
            "status" to "completed" // Track settlement status
        )
        firestore.collection("settlements").add(settlementData).await()

        // 2. Mark relevant expense splits as paid until the amount is covered
        var remaining = amount
        val expenses = expenseRepository.getExpensesByGroupId(groupId)

        for (expense in expenses) {
            if (remaining <= 0) break

            // Only consider expenses where payee was the original payer and payer is in splits
            if (expense.paidBy != payeeId) continue

            val updatedSplits = expense.splitBetween.map { split ->
                if (!split.isPaid && split.userId == payerId && remaining > 0) {
                    // Determine portion to mark as paid
                    remaining -= split.share
                    split.copy(isPaid = true)
                } else {
                    split
                }
            }

            // If any split was updated, persist the change
            if (updatedSplits != expense.splitBetween) {
                firestore.collection("expenses")
                    .document(expense.id)
                    .update("splitBetween", updatedSplits.map { mapOf(
                        "userId" to it.userId,
                        "userName" to it.userName,
                        "share" to it.share,
                        "isPaid" to it.isPaid
                    )}).await()
            }
        }
    }
} 