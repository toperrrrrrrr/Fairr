package com.example.fairr.data.expenses

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    fun getExpensesForGroup(groupId: String): Flow<List<Expense>> = flow {
        val snapshot = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

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
        emit(expenses)
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
} 