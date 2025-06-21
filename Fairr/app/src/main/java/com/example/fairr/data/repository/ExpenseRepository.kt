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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.example.fairr.di.IoDispatcher

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

    // convenience APIs used elsewhere
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>>
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ExpenseRepository {

    @Suppress("UNCHECKED_CAST")
    override suspend fun getExpensesByGroupId(groupId: String): List<Expense> = withContext(ioDispatcher) {
        try {
            val expensesRef = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            expensesRef.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null

                    val paidById = data["paidBy"] as? String ?: ""
                    val paidByName = data["paidByName"] as? String ?: "Unknown User"

                    // Parse splits
                    val splitsData = data["splitBetween"] as? List<Map<String, Any>> ?: emptyList()
                    val splits = splitsData.mapNotNull { splitData ->
                        try {
                            val userId = splitData["userId"] as? String ?: return@mapNotNull null
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
                        paidBy = paidById,
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
            emptyList()
        }
    }

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date,
        paidBy: String,
        splitType: String
    ) = withContext(ioDispatcher) {
        val currentUser = auth.currentUser ?: throw Exception("User must be authenticated to add expenses")

        // Verify user membership and get group info
        val groupDoc = firestore.collection("groups").document(groupId).get().await()
        if (!groupDoc.exists()) throw Exception("Group not found")
        val groupData = groupDoc.data ?: throw Exception("Group data missing")

        val members = groupData["members"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val createdBy = groupData["createdBy"] as? String
        if (members.containsKey(currentUser.uid).not() && createdBy != currentUser.uid) {
            throw Exception("User is not a member of this group")
        }
        val groupCurrency = groupData["currency"] as? String ?: "USD"

        // Build member list for split calculation & find paidByName
        val groupMembers = members.map { (userId, memberData) ->
            val memberMap = memberData as? Map<String, Any> ?: emptyMap()
            mapOf(
                "userId" to userId.toString(),
                "name" to (memberMap["name"] ?: "Unknown"),
                "email" to (memberMap["email"] ?: "")
            )
        }
        val paidByName = groupMembers.firstOrNull { it["userId"] == paidBy }?.get("name")?.toString()
            ?: "Unknown User"

        // Calculate splits
        val splitBetween = calculateSplits(amount, splitType, groupMembers)

        val expenseData = hashMapOf(
            "groupId" to groupId,
            "description" to description,
            "amount" to amount,
            "date" to date,
            "createdAt" to Timestamp.now(),
            "createdBy" to currentUser.uid,
            "updatedAt" to Timestamp.now(),
            "currency" to groupCurrency,
            "paidBy" to paidBy,
            "paidByName" to paidByName,
            "splitType" to splitType,
            "splitBetween" to splitBetween
        )

        try {
            firestore.collection("expenses").add(expenseData).await()
            // Update group total
            val groupRef = firestore.collection("groups").document(groupId)
            firestore.runTransaction { tr ->
                val gDoc = tr.get(groupRef)
                val currentTotal = gDoc.getDouble("totalExpenses") ?: 0.0
                tr.update(groupRef, "totalExpenses", currentTotal + amount)
            }.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving expense", e)
            throw Exception("Failed to save expense: ${e.message}")
        }
    }

    private fun calculateSplits(
        totalAmount: Double,
        splitType: String,
        groupMembers: List<Map<String, Any>>
    ): List<Map<String, Any>> {
        // Existing equal split behaviour retained; TODO for percentage/custom
        val sharePerPerson = totalAmount / groupMembers.size
        return groupMembers.map { member ->
            mapOf(
                "userId" to member["userId"].toString(),
                "userName" to member["name"].toString(),
                "share" to sharePerPerson,
                "isPaid" to false
            )
        }
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> = withContext(ioDispatcher) {
        try {
            val expenseRef = firestore.collection("expenses").document()
            expenseRef.set(expense.copy(id = expenseRef.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>> = flow {
        try {
            val expenses = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
                .documents
                .mapNotNull { doc -> doc.toObject(Expense::class.java)?.copy(id = doc.id) }
            emit(expenses)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expenses for group $groupId", e)
            emit(emptyList())
        }
    }.flowOn(ioDispatcher)
} 