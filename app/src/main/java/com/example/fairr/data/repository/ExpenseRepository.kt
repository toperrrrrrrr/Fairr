package com.example.fairr.data.repository

import android.util.Log
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseSplit
import com.example.fairr.data.model.ExpenseCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

private const val TAG = "ExpenseRepository"

interface ExpenseRepository {
    suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String,
        category: ExpenseCategory = ExpenseCategory.OTHER,
        isRecurring: Boolean = false,
        recurrenceRule: com.example.fairr.data.model.RecurrenceRule? = null
    )
    
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>

    suspend fun getExpenseById(expenseId: String): Expense?

    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense)

    suspend fun deleteExpense(expense: Expense)
    
    suspend fun generateRecurringInstances(expense: Expense, monthsAhead: Int)
    
    suspend fun getUpcomingRecurringExpenses(groupId: String, daysAhead: Int): List<Expense>
    
    suspend fun getRecurringExpenses(groupId: String): List<Expense>
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExpenseRepository {

    @Suppress("UNCHECKED_CAST")
    override suspend fun getExpensesByGroupId(groupId: String): List<Expense> {
        try {
            val expensesRef = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            return expensesRef.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Get the user who paid
                    val paidById = data["paidBy"] as? String
                    val paidByName = if (!paidById.isNullOrEmpty()) {
                        try {
                            val paidByUser = firestore.collection("users")
                                .document(paidById)
                                .get()
                                .await()
                            paidByUser.getString("displayName") ?: "Unknown User"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching paid by user", e)
                            "Unknown User"
                        }
                    } else {
                        "Unknown User"
                    }
                    
                    // Parse splits
                    val splitsData = data["splitBetween"] as? List<Map<String, Any>> ?: emptyList()
                    val splits = splitsData.mapNotNull { splitData ->
                        try {
                            val userId = splitData["userId"] as? String
                            if (userId.isNullOrEmpty()) return@mapNotNull null
                            
                            ExpenseSplit(
                                userId = userId,
                                userName = splitData["userName"] as? String ?: "Unknown",
                                share = (splitData["share"] as? Number)?.toDouble() ?: 0.0,
                                isPaid = splitData["isPaid"] as? Boolean ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing split data", e)
                            null
                        }
                    }

                    Expense(
                        id = doc.id,
                        groupId = data["groupId"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                        currency = data["currency"] as? String ?: "USD",
                        date = (data["date"] as? Timestamp) ?: Timestamp.now(),
                        paidBy = paidById ?: "",
                        paidByName = paidByName,
                        splitBetween = splits,
                        category = try {
                            ExpenseCategory.valueOf((data["category"] as? String)?.uppercase() ?: "OTHER")
                        } catch (e: Exception) {
                            ExpenseCategory.OTHER
                        },
                        notes = data["notes"] as? String ?: "",
                        attachments = (data["attachments"] as? List<String>) ?: emptyList(),
                        splitType = data["splitType"] as? String ?: "Equal Split"
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing expense document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expenses", e)
            return emptyList()
        }
    }

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String,
        category: ExpenseCategory,
        isRecurring: Boolean,
        recurrenceRule: com.example.fairr.data.model.RecurrenceRule?
    ) {
        val currentUser = auth.currentUser
            ?: throw Exception("User must be authenticated to add expenses")

        Log.d("ExpenseRepository", "Adding expense for group: $groupId by user: ${currentUser.uid}")

        // First, verify the user is a member of the group and get member info
        val groupMembers: List<Map<String, Any>>
        val groupCurrency: String
        try {
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            if (!groupDoc.exists()) {
                throw Exception("Group not found")
            }

            val groupData = groupDoc.data
            val members = groupData?.get("members") as? Map<*, *>
            val createdBy = groupData?.get("createdBy") as? String
            groupCurrency = groupData?.get("currency") as? String ?: "PHP"

            if (members?.containsKey(currentUser.uid) != true && createdBy != currentUser.uid) {
                throw Exception("User is not a member of this group")
            }

            // Get member details for split calculation
            groupMembers = members?.map { (userId, memberData) ->
                val memberMap = memberData as? Map<String, Any> ?: emptyMap()
                mapOf(
                    "userId" to userId.toString(),
                    "name" to (memberMap["name"] ?: "Unknown"),
                    "email" to (memberMap["email"] ?: "")
                )
            } ?: emptyList()

            Log.d("ExpenseRepository", "User verified as group member")
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Error verifying group membership: ${e.message}")
            throw Exception("Failed to verify group membership: ${e.message}")
        }

        // Calculate splits based on split type
        val splitBetween = SplitCalculator.calculateSplits(amount, splitType, groupMembers)

        val expenseData = hashMapOf(
            "groupId" to groupId,
            "description" to description,
            "amount" to amount,
            "date" to date,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "createdBy" to currentUser.uid,
            "updatedAt" to com.google.firebase.Timestamp.now(),
            "currency" to groupCurrency,
            "paidBy" to paidBy,
            "splitType" to splitType,
            "splitBetween" to splitBetween,
            "category" to category.name,
            "isRecurring" to isRecurring,
            "recurrenceRule" to recurrenceRule
        )

        Log.d("ExpenseRepository", "Expense data prepared: $expenseData")

        try {
            // Add the expense document
            Log.d("ExpenseRepository", "Attempting to add expense document")
            val expenseRef = firestore.collection("expenses")
                .add(expenseData)
                .await()
            Log.d("ExpenseRepository", "Expense document added successfully with ID: ${expenseRef.id}")

            // Try to update group total. If this fails due to permissions (non-admin user),
            // we simply log the error but do NOT surface it to the UI because the expense
            // itself has already been saved successfully. A backend function can recompute
            // totals if needed.
            try {
                Log.d("ExpenseRepository", "Updating group total")
                val groupRef = firestore.collection("groups").document(groupId)
                firestore.runTransaction { transaction ->
                    val groupDoc = transaction.get(groupRef)
                    val currentTotal = groupDoc.getDouble("totalExpenses") ?: 0.0
                    transaction.update(groupRef, "totalExpenses", currentTotal + amount)
                }.await()
                Log.d("ExpenseRepository", "Group total updated successfully")
            } catch (perm: Exception) {
                // Most likely a PERMISSION_DENIED for non-admin users
                Log.w("ExpenseRepository", "Could not update group total (ignored): ${perm.message}")
            }

        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Error saving expense: ${e.message}", e)
            throw Exception("Failed to save expense: ${e.message}")
        }
    }

    suspend fun addExpense(expense: Expense): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val expenseRef = firestore.collection("expenses").document()
            val expenseData = mapOf(
                "id" to expenseRef.id,
                "amount" to expense.amount,
                "description" to expense.description,
                "date" to expense.date,
                "groupId" to expense.groupId,
                "paidBy" to expense.paidBy,
                "splitBetween" to expense.splitBetween
            )
            
            expenseRef.set(expenseData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>> = flow {
        try {
            val expenses = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                }
            emit(expenses)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expenses for group $groupId", e)
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateExpense(oldExpense: Expense, newExpense: Expense) {
        val diffAmount = newExpense.amount - oldExpense.amount
        withContext(Dispatchers.IO) {
            firestore.runTransaction { txn ->
                val expRef = firestore.collection("expenses").document(oldExpense.id)
                txn.update(expRef, mapOf(
                    "description" to newExpense.description,
                    "amount" to newExpense.amount,
                    "date" to newExpense.date,
                    "category" to newExpense.category.name,
                    "notes" to newExpense.notes,
                    "attachments" to newExpense.attachments,
                    "splitBetween" to newExpense.splitBetween.map { split -> mapOf(
                        "userId" to split.userId,
                        "userName" to split.userName,
                        "share" to split.share,
                        "isPaid" to split.isPaid
                    ) }
                ))

                // adjust group total
                val groupRef = firestore.collection("groups").document(oldExpense.groupId)
                val groupSnap = txn.get(groupRef)
                val currentTotal = groupSnap.getDouble("totalExpenses") ?: 0.0
                txn.update(groupRef, "totalExpenses", currentTotal + diffAmount)
            }.await()
        }
    }

    override suspend fun deleteExpense(expense: Expense) {
        withContext(Dispatchers.IO) {
            firestore.runTransaction { txn ->
                val expRef = firestore.collection("expenses").document(expense.id)
                txn.delete(expRef)

                val groupRef = firestore.collection("groups").document(expense.groupId)
                val currentTotal = (txn.get(groupRef).getDouble("totalExpenses") ?: 0.0) - expense.amount
                txn.update(groupRef, "totalExpenses", currentTotal.coerceAtLeast(0.0))
            }.await()
        }
    }

    override suspend fun getExpenseById(expenseId: String): Expense? {
        try {
            val expenseRef = firestore.collection("expenses").document(expenseId)
            val expenseDoc = expenseRef.get().await()
            
            if (!expenseDoc.exists()) {
                return null
            }
            
            val data = expenseDoc.data ?: return null
            
            // Get the user who paid
            val paidById = data["paidBy"] as? String
            val paidByName = if (!paidById.isNullOrEmpty()) {
                try {
                    val paidByUser = firestore.collection("users")
                        .document(paidById)
                        .get()
                        .await()
                    paidByUser.getString("displayName") ?: "Unknown User"
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching paid by user", e)
                    "Unknown User"
                }
            } else {
                "Unknown User"
            }
            
            // Parse splits
            val splitsData = data["splitBetween"] as? List<Map<String, Any>> ?: emptyList()
            val splits = splitsData.mapNotNull { splitData ->
                try {
                    val userId = splitData["userId"] as? String
                    if (userId.isNullOrEmpty()) return@mapNotNull null
                    
                    ExpenseSplit(
                        userId = userId,
                        userName = splitData["userName"] as? String ?: "Unknown",
                        share = (splitData["share"] as? Number)?.toDouble() ?: 0.0,
                        isPaid = splitData["isPaid"] as? Boolean ?: false
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing split data", e)
                    null
                }
            }

            return Expense(
                id = expenseId,
                groupId = data["groupId"] as? String ?: "",
                description = data["description"] as? String ?: "",
                amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                currency = data["currency"] as? String ?: "USD",
                date = (data["date"] as? Timestamp) ?: Timestamp.now(),
                paidBy = paidById ?: "",
                paidByName = paidByName,
                splitBetween = splits,
                category = try {
                    ExpenseCategory.valueOf((data["category"] as? String)?.uppercase() ?: "OTHER")
                } catch (e: Exception) {
                    ExpenseCategory.OTHER
                },
                notes = data["notes"] as? String ?: "",
                attachments = (data["attachments"] as? List<String>) ?: emptyList(),
                splitType = data["splitType"] as? String ?: "Equal Split"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expense by ID", e)
            return null
        }
    }

    override suspend fun generateRecurringInstances(expense: Expense, monthsAhead: Int) {
        if (!expense.isRecurring || expense.recurrenceRule == null) {
            return
        }
        
        val rule = expense.recurrenceRule!!
        val calendar = Calendar.getInstance()
        calendar.time = expense.date.toDate()
        
        // Calculate end date (either from rule or months ahead)
        val endDate = if (rule.endDate != null) {
            rule.endDate.toDate()
        } else {
            calendar.add(Calendar.MONTH, monthsAhead)
            calendar.time
        }
        
        // Reset calendar to original date
        calendar.time = expense.date.toDate()
        
        // Generate instances based on frequency
        val instances = mutableListOf<Expense>()
        var currentDate = calendar.time
        
        while (currentDate.before(endDate)) {
            // Skip the original expense date
            if (currentDate != expense.date.toDate()) {
                val instance = expense.copy(
                    id = "", // Will be generated by Firestore
                    date = Timestamp(currentDate),
                    parentExpenseId = expense.id // Link to original recurring expense
                )
                instances.add(instance)
            }
            
            // Calculate next date based on frequency and interval
            when (rule.frequency) {
                com.example.fairr.data.model.RecurrenceFrequency.DAILY -> {
                    calendar.add(Calendar.DAY_OF_MONTH, rule.interval)
                }
                com.example.fairr.data.model.RecurrenceFrequency.WEEKLY -> {
                    calendar.add(Calendar.WEEK_OF_YEAR, rule.interval)
                }
                com.example.fairr.data.model.RecurrenceFrequency.MONTHLY -> {
                    calendar.add(Calendar.MONTH, rule.interval)
                }
                com.example.fairr.data.model.RecurrenceFrequency.YEARLY -> {
                    calendar.add(Calendar.YEAR, rule.interval)
                }
                else -> break
            }
            
            currentDate = calendar.time
        }
        
        // Save instances to Firestore
        for (instance in instances) {
            try {
                val instanceData = hashMapOf(
                    "groupId" to instance.groupId,
                    "description" to instance.description,
                    "amount" to instance.amount,
                    "date" to instance.date,
                    "createdAt" to com.google.firebase.Timestamp.now(),
                    "createdBy" to instance.paidBy, // Use paidBy as createdBy for instances
                    "updatedAt" to com.google.firebase.Timestamp.now(),
                    "currency" to instance.currency,
                    "paidBy" to instance.paidBy,
                    "splitType" to instance.splitType,
                    "splitBetween" to instance.splitBetween.map { split -> mapOf(
                        "userId" to split.userId,
                        "userName" to split.userName,
                        "share" to split.share,
                        "isPaid" to split.isPaid
                    ) },
                    "category" to instance.category.name,
                    "notes" to instance.notes,
                    "attachments" to instance.attachments,
                    "isRecurring" to false, // Instances are not recurring themselves
                    "recurrenceRule" to null, // Instances don't have recurrence rules
                    "parentExpenseId" to instance.parentExpenseId // Link to original recurring expense
                )
                
                firestore.collection("expenses").add(instanceData).await()
                Log.d(TAG, "Generated recurring instance for expense: ${expense.id}")
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating recurring instance", e)
            }
        }
    }

    override suspend fun getUpcomingRecurringExpenses(groupId: String, daysAhead: Int): List<Expense> {
        try {
            val calendar = Calendar.getInstance()
            val startDate = calendar.time
            calendar.add(Calendar.DAY_OF_MONTH, daysAhead)
            val endDate = calendar.time
            
            val startTimestamp = Timestamp(startDate)
            val endTimestamp = Timestamp(endDate)
            
            val expensesRef = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .whereGreaterThanOrEqualTo("date", startTimestamp)
                .whereLessThanOrEqualTo("date", endTimestamp)
                .whereEqualTo("isRecurring", true)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()

            return expensesRef.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Get the user who paid
                    val paidById = data["paidBy"] as? String
                    val paidByName = if (!paidById.isNullOrEmpty()) {
                        try {
                            val paidByUser = firestore.collection("users")
                                .document(paidById)
                                .get()
                                .await()
                            paidByUser.getString("displayName") ?: "Unknown User"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching paid by user", e)
                            "Unknown User"
                        }
                    } else {
                        "Unknown User"
                    }
                    
                    // Parse splits
                    val splitsData = data["splitBetween"] as? List<Map<String, Any>> ?: emptyList()
                    val splits = splitsData.mapNotNull { splitData ->
                        try {
                            val userId = splitData["userId"] as? String
                            if (userId.isNullOrEmpty()) return@mapNotNull null
                            
                            ExpenseSplit(
                                userId = userId,
                                userName = splitData["userName"] as? String ?: "Unknown",
                                share = (splitData["share"] as? Number)?.toDouble() ?: 0.0,
                                isPaid = splitData["isPaid"] as? Boolean ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing split data", e)
                            null
                        }
                    }

                    // Parse recurrence rule
                    val recurrenceRuleData = data["recurrenceRule"] as? Map<String, Any>
                    val recurrenceRule = if (recurrenceRuleData != null) {
                        try {
                            val frequencyStr = recurrenceRuleData["frequency"] as? String
                            val frequency = when (frequencyStr) {
                                "DAILY" -> com.example.fairr.data.model.RecurrenceFrequency.DAILY
                                "WEEKLY" -> com.example.fairr.data.model.RecurrenceFrequency.WEEKLY
                                "MONTHLY" -> com.example.fairr.data.model.RecurrenceFrequency.MONTHLY
                                "YEARLY" -> com.example.fairr.data.model.RecurrenceFrequency.YEARLY
                                else -> com.example.fairr.data.model.RecurrenceFrequency.NONE
                            }
                            
                            val interval = (recurrenceRuleData["interval"] as? Number)?.toInt() ?: 1
                            val endDate = recurrenceRuleData["endDate"] as? Timestamp
                            
                            com.example.fairr.data.model.RecurrenceRule(
                                frequency = frequency,
                                interval = interval,
                                endDate = endDate
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing recurrence rule", e)
                            null
                        }
                    } else null

                    Expense(
                        id = doc.id,
                        groupId = data["groupId"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                        currency = data["currency"] as? String ?: "USD",
                        date = (data["date"] as? Timestamp) ?: Timestamp.now(),
                        paidBy = paidById ?: "",
                        paidByName = paidByName,
                        splitBetween = splits,
                        category = try {
                            ExpenseCategory.valueOf((data["category"] as? String)?.uppercase() ?: "OTHER")
                        } catch (e: Exception) {
                            ExpenseCategory.OTHER
                        },
                        notes = data["notes"] as? String ?: "",
                        attachments = (data["attachments"] as? List<String>) ?: emptyList(),
                        splitType = data["splitType"] as? String ?: "Equal Split",
                        isRecurring = data["isRecurring"] as? Boolean ?: false,
                        recurrenceRule = recurrenceRule
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing expense document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting upcoming recurring expenses", e)
            return emptyList()
        }
    }

    override suspend fun getRecurringExpenses(groupId: String): List<Expense> {
        try {
            val expensesRef = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("isRecurring", true)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            return expensesRef.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Get the user who paid
                    val paidById = data["paidBy"] as? String
                    val paidByName = if (!paidById.isNullOrEmpty()) {
                        try {
                            val paidByUser = firestore.collection("users")
                                .document(paidById)
                                .get()
                                .await()
                            paidByUser.getString("displayName") ?: "Unknown User"
                        } catch (e: Exception) {
                            Log.e(TAG, "Error fetching paid by user", e)
                            "Unknown User"
                        }
                    } else {
                        "Unknown User"
                    }
                    
                    // Parse splits
                    val splitsData = data["splitBetween"] as? List<Map<String, Any>> ?: emptyList()
                    val splits = splitsData.mapNotNull { splitData ->
                        try {
                            val userId = splitData["userId"] as? String
                            if (userId.isNullOrEmpty()) return@mapNotNull null
                            
                            ExpenseSplit(
                                userId = userId,
                                userName = splitData["userName"] as? String ?: "Unknown",
                                share = (splitData["share"] as? Number)?.toDouble() ?: 0.0,
                                isPaid = splitData["isPaid"] as? Boolean ?: false
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing split data", e)
                            null
                        }
                    }

                    // Parse recurrence rule
                    val recurrenceRuleData = data["recurrenceRule"] as? Map<String, Any>
                    val recurrenceRule = if (recurrenceRuleData != null) {
                        try {
                            val frequencyStr = recurrenceRuleData["frequency"] as? String
                            val frequency = when (frequencyStr) {
                                "DAILY" -> com.example.fairr.data.model.RecurrenceFrequency.DAILY
                                "WEEKLY" -> com.example.fairr.data.model.RecurrenceFrequency.WEEKLY
                                "MONTHLY" -> com.example.fairr.data.model.RecurrenceFrequency.MONTHLY
                                "YEARLY" -> com.example.fairr.data.model.RecurrenceFrequency.YEARLY
                                else -> com.example.fairr.data.model.RecurrenceFrequency.NONE
                            }
                            
                            val interval = (recurrenceRuleData["interval"] as? Number)?.toInt() ?: 1
                            val endDate = recurrenceRuleData["endDate"] as? Timestamp
                            
                            com.example.fairr.data.model.RecurrenceRule(
                                frequency = frequency,
                                interval = interval,
                                endDate = endDate
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing recurrence rule", e)
                            null
                        }
                    } else null

                    Expense(
                        id = doc.id,
                        groupId = data["groupId"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                        currency = data["currency"] as? String ?: "USD",
                        date = (data["date"] as? Timestamp) ?: Timestamp.now(),
                        paidBy = paidById ?: "",
                        paidByName = paidByName,
                        splitBetween = splits,
                        category = try {
                            ExpenseCategory.valueOf((data["category"] as? String)?.uppercase() ?: "OTHER")
                        } catch (e: Exception) {
                            ExpenseCategory.OTHER
                        },
                        notes = data["notes"] as? String ?: "",
                        attachments = (data["attachments"] as? List<String>) ?: emptyList(),
                        splitType = data["splitType"] as? String ?: "Equal Split",
                        isRecurring = data["isRecurring"] as? Boolean ?: false,
                        recurrenceRule = recurrenceRule,
                        parentExpenseId = data["parentExpenseId"] as? String
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing expense document", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting recurring expenses", e)
            return emptyList()
        }
    }
} 