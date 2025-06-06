package com.example.fairr.data.repository

import com.google.firebase.firestore.FirebaseFirestore
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
    private val firestore: FirebaseFirestore
) : ExpenseRepository {

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        date: Date
    ) {
        val expenseData = hashMapOf(
            "groupId" to groupId,
            "description" to description,
            "amount" to amount,
            "date" to date,
            "createdAt" to Date()
        )

        firestore.collection("expenses")
            .add(expenseData)
            .addOnSuccessListener { documentReference ->
                // TODO: Update group total and member balances
            }
            .addOnFailureListener { e ->
                // TODO: Handle failure
            }
    }
} 