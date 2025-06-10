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
        date: Date
    )
    
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>
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
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            return expensesRef.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    
                    // Get the user who paid
                    val paidByUser = firestore.collection("users")
                        .document(data["paidBy"] as? String ?: "")
                        .get()
                        .await()
                    
                    val paidByName = paidByUser.getString("displayName") ?: "Unknown User"
                    
                    // Parse splits
                    val splitsData = data["splitBetween"] as? List<Map<String, Any>> ?: emptyList()
                    val splits = splitsData.mapNotNull { splitData ->
                        try {
                            ExpenseSplit(
                                userId = splitData["userId"] as? String ?: return@mapNotNull null,
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
                        paidBy = data["paidBy"] as? String ?: "",
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
        date: Date
    ) {
        val currentUser = auth.currentUser
            ?: throw Exception("User must be authenticated to add expenses")

        Log.d("ExpenseRepository", "Adding expense for group: $groupId by user: ${currentUser.uid}")

        // First, verify the user is a member of the group
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

            Log.d("ExpenseRepository", "User verified as group member")
        } catch (e: Exception) {
            Log.e("ExpenseRepository", "Error verifying group membership: ${e.message}")
            throw Exception("Failed to verify group membership: ${e.message}")
        }

        val expenseData = hashMapOf(
            "groupId" to groupId,
            "description" to description,
            "amount" to amount,
            "date" to date,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "createdBy" to currentUser.uid,
            "updatedAt" to com.google.firebase.Timestamp.now(),
            "currency" to "USD" // TODO: Make this configurable
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
} 