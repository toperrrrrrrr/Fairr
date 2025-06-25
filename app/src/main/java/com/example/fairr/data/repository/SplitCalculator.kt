package com.example.fairr.data.repository

object SplitCalculator {

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
                    val specifiedTotal = groupMembers.sumOf { (it["customAmount"] as? Number)?.toDouble() ?: 0.0 }
                    
                    // Validate that specified total doesn't exceed the expense amount
                    val validSpecifiedTotal = specifiedTotal.coerceAtMost(totalAmount)
                    
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