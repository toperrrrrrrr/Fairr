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
        splitType: String
    )
    
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>

    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense)

    suspend fun deleteExpense(expense: Expense)
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
                        attachments = (data["attachments"] as? List<String>) ?: emptyList()
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
        splitType: String
    ) {
        val currentUser = auth.currentUser
            ?: throw Exception("User must be authenticated to add expenses")

        Log.d("ExpenseRepository", "Adding expense for group: $groupId by user: ${currentUser.uid}")

        // First, verify the user is a member of the group and get member info
        val groupMembers: List<Map<String, Any>>
        try {
            val groupDoc = firestore.collection("groups").document(groupId).get().await()
            if (!groupDoc.exists()) {
                throw Exception("Group not found")
            }

            val groupData = groupDoc.data
            val members = groupData?.get("members") as? Map<*, *>
            val createdBy = groupData?.get("createdBy") as? String

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
        val splitBetween = calculateSplits(amount, splitType, groupMembers)

        val expenseData = hashMapOf(
            "groupId" to groupId,
            "description" to description,
            "amount" to amount,
            "date" to date,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "createdBy" to currentUser.uid,
            "updatedAt" to com.google.firebase.Timestamp.now(),
            "currency" to "USD", // TODO: Make this configurable
            "paidBy" to paidBy,
            "splitType" to splitType,
            "splitBetween" to splitBetween
        )

        Log.d("ExpenseRepository", "Expense data prepared: $expenseData")

        try {
            // Add the expense document
            Log.d("ExpenseRepository", "Attempting to add expense document")
            val expenseRef = firestore.collection("expenses")
                .add(expenseData)
                .await()
            Log.d("ExpenseRepository", "Expense document added successfully with ID: ${expenseRef.id}")

            // Update group total
            Log.d("ExpenseRepository", "Updating group total")
            val groupRef = firestore.collection("groups").document(groupId)
            firestore.runTransaction { transaction ->
                val groupDoc = transaction.get(groupRef)
                val currentTotal = groupDoc.getDouble("totalExpenses") ?: 0.0
                transaction.update(groupRef, "totalExpenses", currentTotal + amount)
            }.await()
            Log.d("ExpenseRepository", "Group total updated successfully")

        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Error saving expense: ${e.message}", e)
            throw Exception("Failed to save expense: ${e.message}")
        }
    }

    private fun calculateSplits(
        totalAmount: Double,
        splitType: String,
        groupMembers: List<Map<String, Any>>
    ): List<Map<String, Any>> {
        return when (splitType) {
            "Equal Split" -> {
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
            "Percentage" -> {
                // Expect each member map to optionally include a "percentage" key (Double 0-100)
                val providedPercentTotal = groupMembers.sumOf { (it["percentage"] as? Number)?.toDouble() ?: 0.0 }

                val percentages = if (providedPercentTotal in 99.9..100.1) {
                    // Use given percentages, default missing ones to 0
                    groupMembers.map { member ->
                        val pct = (member["percentage"] as? Number)?.toDouble() ?: 0.0
                        member to pct
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
            }
            "Custom Amount" -> {
                // Expect a "customAmount" per member; fallback to equal split for unspecified
                val specifiedTotal = groupMembers.sumOf { (it["customAmount"] as? Number)?.toDouble() ?: 0.0 }
                val remainingMembers = groupMembers.filter { (it["customAmount"] as? Number) == null }
                val remainingTotal = (totalAmount - specifiedTotal).coerceAtLeast(0.0)
                val equalShareForRemaining = if (remainingMembers.isNotEmpty()) remainingTotal / remainingMembers.size else 0.0

                groupMembers.map { member ->
                    val share = (member["customAmount"] as? Number)?.toDouble() ?: equalShareForRemaining
                    mapOf(
                        "userId" to member["userId"].toString(),
                        "userName" to member["name"].toString(),
                        "share" to share,
                        "isPaid" to false
                    )
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
} 