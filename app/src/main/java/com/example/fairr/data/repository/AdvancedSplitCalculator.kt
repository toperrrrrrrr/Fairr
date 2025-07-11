package com.example.fairr.data.repository

import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseSplit

/**
 * Advanced split calculator with sophisticated algorithms for expense splitting and settlement optimization
 */
object AdvancedSplitCalculator {

    data class SettlementTransaction(
        val fromUserId: String,
        val fromUserName: String,
        val toUserId: String,
        val toUserName: String,
        val amount: Double,
        val currency: String
    )

    data class UserBalance(
        val userId: String,
        val userName: String,
        val totalPaid: Double,
        val totalOwed: Double,
        val netBalance: Double
    )

    data class SplitResult(
        val splits: List<ExpenseSplit>,
        val totalAmount: Double,
        val splitType: String,
        val currency: String
    )

    /**
     * Calculate optimal settlements to minimize the number of transactions
     */
    fun calculateOptimalSettlements(
        expenses: List<Expense>,
        groupMembers: List<Map<String, Any>>
    ): List<SettlementTransaction> {
        val userBalances = calculateUserBalances(expenses, groupMembers)
        val transactions = mutableListOf<SettlementTransaction>()

        // Group expenses by currency
        val expensesByCurrency = expenses.groupBy { it.currency }

        expensesByCurrency.forEach { (currency, currencyExpenses) ->
            // Calculate net balances for this currency
            val balances = userBalances.filter { balance ->
                currencyExpenses.any { it.paidBy == balance.userId }
            }

            // Create settlement transactions
            val debtors = balances.filter { it.netBalance < 0 }.sortedBy { it.netBalance }
            val creditors = balances.filter { it.netBalance > 0 }.sortedByDescending { it.netBalance }

            var debtorIndex = 0
            var creditorIndex = 0

            while (debtorIndex < debtors.size && creditorIndex < creditors.size) {
                val debtor = debtors[debtorIndex]
                val creditor = creditors[creditorIndex]

                val amount = minOf(abs(debtor.netBalance), creditor.netBalance)
                if (amount > 0) {
                    transactions.add(
                        SettlementTransaction(
                            fromUserId = debtor.userId,
                            fromUserName = debtor.userName,
                            toUserId = creditor.userId,
                            toUserName = creditor.userName,
                            amount = amount,
                            currency = currency
                        )
                    )
                }

                // Update indices based on remaining balances
                if (abs(debtor.netBalance) <= creditor.netBalance) {
                    debtorIndex++
                }
                if (abs(debtor.netBalance) >= creditor.netBalance) {
                    creditorIndex++
                }
            }
        }

        return transactions
    }

    /**
     * Calculate balances for all users in the group
     */
    fun calculateUserBalances(
        expenses: List<Expense>,
        groupMembers: List<Map<String, Any>>
    ): List<UserBalance> {
        val balances = mutableMapOf<String, UserBalance>()

        // Initialize balances for all members
        groupMembers.forEach { member ->
            val userId = member["userId"] as String
            val userName = member["displayName"] as String
            balances[userId] = UserBalance(
                userId = userId,
                userName = userName,
                totalPaid = 0.0,
                totalOwed = 0.0,
                netBalance = 0.0
            )
        }

        // Calculate paid amounts and owed amounts
        expenses.forEach { expense ->
            val paidBy = expense.paidBy
            val amount = expense.amount

            // Update amount paid
            balances[paidBy]?.let { balance ->
                balances[paidBy] = balance.copy(
                    totalPaid = balance.totalPaid + amount
                )
            }

            // Update amounts owed based on splits
            expense.splitBetween.forEach { split ->
                balances[split.userId]?.let { balance ->
                    balances[split.userId] = balance.copy(
                        totalOwed = balance.totalOwed + (split.share * amount)
                    )
                }
            }
        }

        // Calculate net balances
        return balances.values.map { balance ->
            balance.copy(netBalance = balance.totalPaid - balance.totalOwed)
        }
    }

    /**
     * Calculate splits with advanced options
     */
    fun calculateAdvancedSplits(
        totalAmount: Double,
        splitType: String,
        groupMembers: List<Map<String, Any>>,
        currency: String = "PHP"  // Changed from "USD" to "PHP" to match app default
    ): SplitResult {
        val splits = when (splitType) {
            "Equal Split" -> calculateEqualSplits(totalAmount, groupMembers)
            "Percentage" -> calculatePercentageSplits(totalAmount, groupMembers)
            "Custom Amount" -> calculateCustomSplits(totalAmount, groupMembers)
            else -> emptyList()
        }

        return SplitResult(
            splits = splits,
            totalAmount = totalAmount,
            splitType = splitType,
            currency = currency
        )
    }

    private fun calculateEqualSplits(
        totalAmount: Double,
        groupMembers: List<Map<String, Any>>
    ): List<ExpenseSplit> {
        val memberCount = groupMembers.size
        val equalShare = totalAmount / memberCount.toDouble()

        return groupMembers.map { member ->
            ExpenseSplit(
                userId = member["userId"] as String,
                userName = member["displayName"] as String,
                share = equalShare / totalAmount
            )
        }
    }

    private fun calculatePercentageSplits(
        totalAmount: Double,
        groupMembers: List<Map<String, Any>>
    ): List<ExpenseSplit> {
        return groupMembers.map { member ->
            val percentage = (member["percentage"] as? Number)?.toDouble() ?: (100.0 / groupMembers.size)
            val share = percentage / 100.0
            ExpenseSplit(
                userId = member["userId"] as String,
                userName = member["displayName"] as String,
                share = share
            )
        }
    }

    private fun calculateCustomSplits(
        totalAmount: Double,
        groupMembers: List<Map<String, Any>>
    ): List<ExpenseSplit> {
        val customAmounts = groupMembers.map { member ->
            val customAmount = (member["customAmount"] as? Number)?.toDouble() 
                ?: (totalAmount / groupMembers.size)
            ExpenseSplit(
                userId = member["userId"] as String,
                userName = member["displayName"] as String,
                share = customAmount / totalAmount
            )
        }

        // Normalize shares if total doesn't match
        val totalShare = customAmounts.sumOf { it.share }
        if (abs(totalShare - 1.0) > 0.01) {
            val factor = 1.0 / totalShare
            return customAmounts.map { split ->
                split.copy(share = split.share * factor)
            }
        }

        return customAmounts
    }

    /**
     * Validate split calculations
     */
    fun validateSplits(splits: List<ExpenseSplit>, totalAmount: Double): Boolean {
        if (splits.isEmpty()) return false

        val totalShare = splits.sumOf { it.share }
        val tolerance = 0.01 // Allow for small rounding differences

        return abs(totalShare - 1.0) <= tolerance
    }

    /**
     * Get split statistics
     */
    fun getSplitStatistics(splits: List<ExpenseSplit>, totalAmount: Double): Map<String, Any> {
        if (splits.isEmpty()) {
            return mapOf(
                "totalParticipants" to 0,
                "averageShare" to 0.0,
                "minShare" to 0.0,
                "maxShare" to 0.0,
                "totalAmount" to 0.0
            )
        }

        val shares = splits.map { it.share }
        val totalShare = shares.sum()
        
        return mapOf(
            "totalParticipants" to splits.size,
            "averageShare" to totalShare / splits.size,
            "minShare" to (shares.minOrNull() ?: 0.0),
            "maxShare" to (shares.maxOrNull() ?: 0.0),
            "totalAmount" to totalAmount
        )
    }

    private fun abs(value: Double): Double = if (value < 0) -value else value
}

