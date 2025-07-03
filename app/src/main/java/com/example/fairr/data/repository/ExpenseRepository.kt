package com.example.fairr.data.repository

import android.util.Log
import com.example.fairr.data.model.Expense
import com.example.fairr.data.model.ExpenseSplit
import com.example.fairr.data.model.ExpenseCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.DocumentSnapshot
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
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

private const val TAG = "ExpenseRepository"

// Pagination constants
private const val DEFAULT_PAGE_SIZE = 20    x
private const val MAX_PAGE_SIZE = 50
private const val INITIAL_LOAD_SIZE = 10

/**
 * Pagination result wrapper
 */
data class PaginatedExpenses(
    val expenses: List<Expense>,
    val hasMore: Boolean,
    val lastDocument: DocumentSnapshot?,
    val totalCount: Int? = null
)

/**
 * Expense query parameters for filtering and sorting
 */
data class ExpenseQueryParams(
    val groupId: String,
    val pageSize: Int = DEFAULT_PAGE_SIZE,
    val lastDocument: DocumentSnapshot? = null,
    val category: ExpenseCategory? = null,
    val startDate: Date? = null,
    val endDate: Date? = null,
    val paidBy: String? = null,
    val searchQuery: String? = null,
    val includeRecurring: Boolean = true
)

interface ExpenseRepository {
    suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        currency: String = "PHP",
        date: Date,
        paidBy: String,
        splitType: String,
        category: ExpenseCategory = ExpenseCategory.OTHER,
        isRecurring: Boolean = false,
        recurrenceRule: com.example.fairr.data.model.RecurrenceRule? = null
    )
    
    @Deprecated("Use getPaginatedExpenses instead for better performance")
    suspend fun getExpensesByGroupId(groupId: String): List<Expense>
    
    /**
     * Get paginated expenses with optimized performance
     */
    suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses
    
    /**
     * Get expenses for initial screen load (optimized)
     */
    suspend fun getInitialExpenses(groupId: String): PaginatedExpenses
    
    /**
     * Load more expenses for pagination
     */
    suspend fun loadMoreExpenses(
        groupId: String, 
        lastDocument: DocumentSnapshot,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): PaginatedExpenses

    fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>>

    suspend fun getExpenseById(expenseId: String): Expense?

    suspend fun updateExpense(oldExpense: Expense, newExpense: Expense)

    suspend fun deleteExpense(expense: Expense)
    
    suspend fun generateRecurringInstances(expense: Expense, monthsAhead: Int)
    
    suspend fun getUpcomingRecurringExpenses(groupId: String, daysAhead: Int): List<Expense>
    
    suspend fun getRecurringExpenses(groupId: String): List<Expense>

    suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>>
    
    /**
     * Search expenses with pagination
     */
    suspend fun searchExpenses(
        query: String,
        groupIds: List<String>,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastDocument: DocumentSnapshot? = null
    ): PaginatedExpenses
    
    /**
     * Get expense count for a group (cached for performance)
     */
    suspend fun getExpenseCount(groupId: String): Int
}

@Singleton
class ExpenseRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val activityService: com.example.fairr.data.activity.ActivityService
) : ExpenseRepository {

    // Cache for user names to reduce Firestore calls
    private val userNameCache = mutableMapOf<String, String>()
    
    override suspend fun getPaginatedExpenses(params: ExpenseQueryParams): PaginatedExpenses = 
        withContext(Dispatchers.IO) {
            try {
                val pageSize = params.pageSize.coerceAtMost(MAX_PAGE_SIZE)
                var query = firestore.collection("expenses")
                    .whereEqualTo("groupId", params.groupId)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(pageSize.toLong())
                
                // Apply filters
                params.category?.let { category ->
                    query = query.whereEqualTo("category", category.name)
                }
                
                params.paidBy?.let { paidBy ->
                    query = query.whereEqualTo("paidBy", paidBy)
                }
                
                // Handle pagination
                params.lastDocument?.let { lastDoc ->
                    query = query.startAfter(lastDoc)
                }
                
                val snapshot = query.get().await()
                val expenses = parseExpensesOptimized(snapshot.documents)
                
                // Check if there are more results
                val hasMore = snapshot.documents.size == pageSize
                val lastDocument = snapshot.documents.lastOrNull()
                
                PaginatedExpenses(
                    expenses = expenses,
                    hasMore = hasMore,
                    lastDocument = lastDocument
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting paginated expenses", e)
                PaginatedExpenses(
                    expenses = emptyList(),
                    hasMore = false,
                    lastDocument = null
                )
            }
        }
    
    override suspend fun getInitialExpenses(groupId: String): PaginatedExpenses = 
        getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                pageSize = INITIAL_LOAD_SIZE
            )
        )
    
    override suspend fun loadMoreExpenses(
        groupId: String, 
        lastDocument: DocumentSnapshot,
        pageSize: Int
    ): PaginatedExpenses = 
        getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                pageSize = pageSize,
                lastDocument = lastDocument
            )
        )
    
    override suspend fun searchExpenses(
        query: String,
        groupIds: List<String>,
        pageSize: Int,
        lastDocument: DocumentSnapshot?
    ): PaginatedExpenses = withContext(Dispatchers.IO) {
        try {
            val allExpenses = mutableListOf<Expense>()
            var combinedHasMore = false
            var combinedLastDocument: DocumentSnapshot? = null
            
            // Search across all groups (limited for performance)
            groupIds.take(10).forEach { groupId -> // Limit to 10 groups max
                val params = ExpenseQueryParams(
                    groupId = groupId,
                    pageSize = pageSize / groupIds.size.coerceAtLeast(1),
                    lastDocument = lastDocument,
                    searchQuery = query
                )
                
                val result = getPaginatedExpenses(params)
                
                // Filter by search query locally (since Firestore doesn't support full-text search)
                val filteredExpenses = result.expenses.filter { expense ->
                    expense.description.contains(query, ignoreCase = true) ||
                    expense.paidByName.contains(query, ignoreCase = true) ||
                    expense.category.name.contains(query, ignoreCase = true)
                }
                
                allExpenses.addAll(filteredExpenses)
                if (result.hasMore) combinedHasMore = true
                if (result.lastDocument != null) combinedLastDocument = result.lastDocument
            }
            
            // Sort by date (most recent first) and take only requested page size
            val sortedExpenses = allExpenses
                .sortedByDescending { it.date.toDate() }
                .take(pageSize)
            
            PaginatedExpenses(
                expenses = sortedExpenses,
                hasMore = combinedHasMore,
                lastDocument = combinedLastDocument
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error searching expenses", e)
            PaginatedExpenses(
                expenses = emptyList(),
                hasMore = false,
                lastDocument = null
            )
        }
    }
    
    override suspend fun getExpenseCount(groupId: String): Int = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expense count", e)
            0
        }
    }
    
    /**
     * Optimized expense parsing with user name caching
     */
    @Suppress("UNCHECKED_CAST")
    private suspend fun parseExpensesOptimized(documents: List<DocumentSnapshot>): List<Expense> = 
        withContext(Dispatchers.IO) {
            // Get all unique user IDs first
            val userIds = documents.mapNotNull { doc ->
                doc.data?.get("paidBy") as? String
            }.toSet()
            
            // Batch fetch user names for unknown users
            val unknownUserIds = userIds.filterNot { userNameCache.containsKey(it) }
            if (unknownUserIds.isNotEmpty()) {
                try {
                    val userDocs = firestore.collection("users")
                        .whereIn("__name__", unknownUserIds.take(10)) // Firestore limit
                        .get()
                        .await()
                    
                    userDocs.documents.forEach { userDoc ->
                        val displayName = userDoc.getString("displayName") ?: "Unknown User"
                        userNameCache[userDoc.id] = displayName
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error batch fetching user names", e)
                }
            }
            
            // Parse expenses with cached user names
            documents.mapNotNull { doc ->
                try {
                    parseExpenseDocument(doc)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing expense document ${doc.id}", e)
                    null
                }
            }
        }
    
    /**
     * Parse single expense document with optimizations
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseExpenseDocument(doc: DocumentSnapshot): Expense? {
        val data = doc.data ?: return null
        
        // Get cached user name or fallback
        val paidById = data["paidBy"] as? String ?: ""
        val paidByName = userNameCache[paidById] ?: "Unknown User"
        
        // Parse splits efficiently
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

        return Expense(
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
            attachments = (data["attachments"] as? List<String>) ?: emptyList(),
            splitType = data["splitType"] as? String ?: "Equal Split"
        )
    }

    // Keep the old method for backward compatibility but mark as deprecated
    @Suppress("UNCHECKED_CAST")
    override suspend fun getExpensesByGroupId(groupId: String): List<Expense> {
        Log.w(TAG, "Using deprecated getExpensesByGroupId. Consider using getPaginatedExpenses for better performance.")
        
        // Use pagination with a larger page size for backward compatibility
        val result = getPaginatedExpenses(
            ExpenseQueryParams(
                groupId = groupId,
                pageSize = MAX_PAGE_SIZE
            )
        )
        return result.expenses
    }

    @Suppress("UNCHECKED_CAST")
    override fun getExpensesByGroupIdFlow(groupId: String): Flow<List<Expense>> = callbackFlow {
        val query = firestore.collection("expenses")
            .whereEqualTo("groupId", groupId)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50) // Limit for performance
        
        val listenerRegistration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Error in expense flow listener", e)
                close(e)
                return@addSnapshotListener
            }
            
            if (snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            
            try {
                // Parse expenses without async user fetching for real-time updates
                val expenses = snapshot.documents.mapNotNull { doc ->
                    parseExpenseDocument(doc)
                }
                trySend(expenses)
            } catch (parseError: Exception) {
                Log.e(TAG, "Error parsing expenses in flow", parseError)
                trySend(emptyList())
            }
        }
        
        awaitClose { 
            listenerRegistration.remove()
            Log.d(TAG, "Expense flow listener removed for group: $groupId")
        }
    }

    override suspend fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        currency: String,
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
            "currency" to currency,
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

            // Log activity for expense added
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userDoc = firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .await()
                    
                    val userName = userDoc.getString("displayName") ?: currentUser.email?.substringBefore("@") ?: "Unknown User"
                    val userInitials = userName.split(" ").take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
                    
                    activityService.logActivity(
                        groupId = groupId,
                        type = com.example.fairr.data.model.ActivityType.EXPENSE_ADDED,
                        title = description,
                        description = "Added expense for $description",
                        amount = amount,
                        userName = userName,
                        userInitials = userInitials,
                        isPositive = false
                    )
                }
            } catch (e: Exception) {
                Log.w("ExpenseRepository", "Failed to log activity for expense added: ${e.message}")
            }

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

    override suspend fun getExpensesByGroup(groupId: String): Flow<List<Expense>> = flow {
        try {
            val expensesRef = firestore.collection("expenses")
                .whereEqualTo("groupId", groupId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val expenses = expensesRef.documents.mapNotNull { doc ->
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
            emit(expenses)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expenses", e)
            emit(emptyList())
        }
    }

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
        
        val rule = expense.recurrenceRule ?: return
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