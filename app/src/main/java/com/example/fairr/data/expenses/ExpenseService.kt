package com.example.fairr.data.expenses

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val date: Date = Date(),
    val createdAt: Date = Date(),
    val createdBy: String = "",
    val currency: String = "PHP"
)

@Singleton
class ExpenseService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun getExpensesForGroup(groupId: String): Flow<List<Expense>> = callbackFlow {
        val query = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val expenses = snapshot.documents.map { doc ->
                Expense(
                    id = doc.id,
                    groupId = doc.getString("groupId") ?: "",
                    description = doc.getString("description") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    date = doc.getDate("date") ?: Date(),
                    createdAt = doc.getDate("createdAt") ?: Date(),
                    createdBy = doc.getString("createdBy") ?: "",
                    currency = doc.getString("currency") ?: "PHP"
                )
            }
            trySend(expenses)
        }
        awaitClose { subscription.remove() }
    }

    suspend fun getTotalExpensesForGroup(groupId: String): Double {
        val snapshot = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .get()
            .await()

        return snapshot.documents.sumOf { doc ->
            doc.getDouble("amount") ?: 0.0
        }
    }

    suspend fun deleteExpense(expenseId: String) {
        firestore.collection("expenses").document(expenseId).delete().await()
    }

    suspend fun updateExpense(expense: Expense) {
        val data = mapOf(
            "groupId" to expense.groupId,
            "description" to expense.description,
            "amount" to expense.amount,
            "date" to expense.date,
            "createdAt" to expense.createdAt,
            "createdBy" to expense.createdBy,
            "currency" to expense.currency
        )
        firestore.collection("expenses").document(expense.id).update(data).await()
    }
} 