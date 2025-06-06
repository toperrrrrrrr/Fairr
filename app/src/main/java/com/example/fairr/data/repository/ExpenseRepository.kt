package com.example.fairr.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface ExpenseRepository {
    suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date
    )
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ExpenseRepository {

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
} 