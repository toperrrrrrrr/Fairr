package com.example.fairr.data.repository

/**
 * Utility class for calculating expense splits among group members.
 * 
 * Provides algorithms for different splitting strategies including equal division,
 * percentage-based splits, and custom amount distributions with proper fallback handling.
 */
object SplitCalculator {

    /**
     * Calculates optimal expense splits based on the specified strategy.
     * 
     * This function handles all expense splitting logic for the Fairr app, including
     * input validation, error handling, and fallback to equal splits when needed.
     * 
     * @param totalAmount The total expense amount to split (must be > 0)
     * @param splitType The splitting strategy:
     *                  - "Equal Split": Divides amount equally among all members
     *                  - "Percentage": Uses percentage values from member data (must total 100%)
     *                  - "Custom Amount": Uses custom amounts specified per member
     *                  - Any other value defaults to equal split
     * @param groupMembers List of group members, each containing:
     *                     - "userId": String - unique identifier for the member
     *                     - "name": String - display name for the member
     *                     - "percentage": Number (optional) - percentage for percentage splits (0-100)
     *                     - "customAmount": Number (optional) - custom amount for custom splits
     * 
     * @return List of split results, each containing:
     *         - "userId": String - member's unique identifier
     *         - "userName": String - member's display name
     *         - "share": Double - calculated share amount for this member
     *         - "isPaid": Boolean - always false (payment status managed elsewhere)
     *         - "percentage": Double (percentage splits only) - the percentage used
     * 
     * @throws None - All exceptions are caught and handled with fallback to equal split
     * 
     * **Business Rules:**
     * - Empty member list returns empty result
     * - Zero or negative amounts result in zero shares for all members
     * - Invalid percentages (not totaling 100%) fall back to equal split
     * - Custom amounts exceeding total are clamped to total
     * - Negative custom amounts are clamped to 0
     * - Any parsing errors fall back to equal split
     * 
     * **Examples:**
     * ```kotlin
     * // Equal split: $100 among 4 people = $25 each
     * calculateSplits(100.0, "Equal Split", members) // Each gets 25.0
     * 
     * // Percentage split: 60% and 40%
     * calculateSplits(100.0, "Percentage", membersWithPercentages) // 60.0 and 40.0
     * 
     * // Custom amounts with remainder distributed equally
     * calculateSplits(100.0, "Custom Amount", membersWithCustomAmounts)
     * ```
     */
    fun calculateSplits(
        totalAmount: Double,
        splitType: String,
        groupMembers: List<Map<String, Any>>
    ): List<Map<String, Any>> {
        // Validate inputs
        if (groupMembers.isEmpty()) {
            return emptyList()
        }
        
        if (totalAmount <= 0) {
            return groupMembers.map { member ->
                mapOf(
                    "userId" to member["userId"].toString(),
                    "userName" to member["name"].toString(),
                    "share" to 0.0,
                    "isPaid" to false
                )
            }
        }
        
        return when (splitType) {
            "Equal Split" -> {
                try {
                    val sharePerPerson = totalAmount / groupMembers.size
                    groupMembers.map { member ->
                        mapOf(
                            "userId" to member["userId"].toString(),
                            "userName" to member["name"].toString(),
                            "share" to sharePerPerson,
                            "isPaid" to false
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to equal distribution
                    val sharePerPerson = totalAmount / groupMembers.size
                    groupMembers.map { member ->
                        mapOf(
                            "userId" to member["userId"].toString(),
                            "userName" to member["name"].toString(),
                            "share" to sharePerPerson,
                            "isPaid" to false
                        )
                    }
                }
            }
            "Percentage" -> {
                try {
                    // Expect each member map to optionally include a "percentage" key (Double 0-100)
                    val providedPercentTotal = groupMembers.sumOf { (it["percentage"] as? Number)?.toDouble() ?: 0.0 }

                    val percentages = if (providedPercentTotal in 99.9..100.1) {
                        // Use given percentages, default missing ones to 0
                        groupMembers.map { member ->
                            val pct = (member["percentage"] as? Number)?.toDouble() ?: 0.0
                            // Validate percentage is within valid range
                            val validPct = pct.coerceIn(0.0, 100.0)
                            member to validPct
                        }
                    } else {
                        // Fallback: divide equally
                        val equalPct = 100.0 / groupMembers.size
                        groupMembers.map { member -> member to equalPct }
                    }

                    percentages.map { (member, pct) ->
                        mapOf(
                            "userId" to member["userId"].toString(),
                            "userName" to member["name"].toString(),
                            "share" to (totalAmount * pct / 100),
                            "isPaid" to false,
                            "percentage" to pct
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to equal split
                    val sharePerPerson = totalAmount / groupMembers.size
                    groupMembers.map { member ->
                        mapOf(
                            "userId" to member["userId"].toString(),
                            "userName" to member["name"].toString(),
                            "share" to sharePerPerson,
                            "isPaid" to false
                        )
                    }
                }
            }
            "Custom Amount" -> {
                try {
                    // Expect a "customAmount" per member; fallback to equal split for unspecified
                    // Also ensure negative amounts are clamped to 0
                    val validSpecifiedTotal = groupMembers.sumOf { member ->
                        val customAmount = (member["customAmount"] as? Number)?.toDouble() ?: 0.0
                        customAmount.coerceAtLeast(0.0)
                    }.coerceAtMost(totalAmount)
                    
                    val remainingMembers = groupMembers.filter { (it["customAmount"] as? Number) == null }
                    val remainingTotal = (totalAmount - validSpecifiedTotal).coerceAtLeast(0.0)
                    val equalShareForRemaining = if (remainingMembers.isNotEmpty()) remainingTotal / remainingMembers.size else 0.0

                    groupMembers.map { member ->
                        val customAmount = (member["customAmount"] as? Number)?.toDouble() ?: 0.0
                        // Ensure custom amounts are non-negative and don't exceed the total
                        val validCustomAmount = customAmount.coerceAtLeast(0.0).coerceAtMost(totalAmount)
                        val share = if (member["customAmount"] != null) validCustomAmount else equalShareForRemaining
                        
                        mapOf(
                            "userId" to member["userId"].toString(),
                            "userName" to member["name"].toString(),
                            "share" to share,
                            "isPaid" to false
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to equal split
                    val sharePerPerson = totalAmount / groupMembers.size
                    groupMembers.map { member ->
                        mapOf(
                            "userId" to member["userId"].toString(),
                            "userName" to member["name"].toString(),
                            "share" to sharePerPerson,
                            "isPaid" to false
                        )
                    }
                }
            }
            else -> {
                // Default to equal split
                val sharePerPerson = totalAmount / groupMembers.size
                groupMembers.map { member ->
                    mapOf(
                        "userId" to member["userId"].toString(),
                        "userName" to member["name"].toString(),
                        "share" to sharePerPerson,
                        "isPaid" to false
                    )
                }
            }
        }
    }
} 