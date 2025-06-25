# Phase 5: Data Models and Persistence - Fairr Android Codebase Analysis

## Overview

This phase provides a comprehensive analysis of the data architecture, Firestore schema design, persistence strategies, and performance optimizations that form the foundation of Fairr's data management system.

## Data Model Architecture

### 1. Core Data Models

#### Expense Model
```kotlin
data class Expense(
    val id: String = "",
    val groupId: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val currency: String = "PHP",
    val date: Timestamp = Timestamp.now(),
    val paidBy: String = "", // userId
    val paidByName: String = "", // user's name
    val splitBetween: List<ExpenseSplit> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val notes: String = "",
    val attachments: List<String> = emptyList() // URLs to attachments
)
```

**Key Design Decisions:**
- **Denormalized User Data**: `paidByName` stored directly for performance
- **Flexible Splitting**: `ExpenseSplit` objects allow complex splitting scenarios
- **Category System**: Enum-based categorization for analytics
- **Attachment Support**: URL-based attachment system for receipts

#### Group Model
```kotlin
data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val currency: String = "PHP",
    val createdAt: Timestamp = Timestamp.now(),
    val createdBy: String = "",
    val inviteCode: String = "",
    val members: List<GroupMember> = emptyList()
)
```

**Key Design Decisions:**
- **Per-Group Currency**: Each group can have different currency
- **Invite Code System**: 6-character alphanumeric codes for joining
- **Role-Based Access**: ADMIN/MEMBER roles for permissions
- **Member Management**: Embedded member data for efficient queries

#### GroupJoinRequest & Notification Models
```kotlin
data class GroupJoinRequest(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val groupId: String = "",
    val groupName: String = "",
    val groupCreatorId: String = "",
    val status: JoinRequestStatus = JoinRequestStatus.PENDING,
    val requestedAt: Timestamp = Timestamp.now(),
    val respondedAt: Timestamp? = null,
    val inviteCode: String = ""
)
```

**Key Design Decisions:**
- **Denormalized Data**: Group and user names stored for offline access
- **Status Tracking**: PENDING/APPROVED/REJECTED workflow
- **Timestamp Management**: Request and response timestamps
- **Audit Trail**: Complete request history

### 2. Data Relationships

#### Entity Relationship Diagram
```
User (Firebase Auth)
├── Groups (createdBy, memberIds)
│   ├── Expenses (groupId)
│   │   └── ExpenseSplits (userId)
│   ├── GroupMembers (userId)
│   └── GroupJoinRequests (userId, groupId)
├── Notifications (recipientId)
├── GroupInvites (inviteeId)
└── FriendRequests (senderId/receiverId)
```

#### Relationship Patterns

**One-to-Many Relationships:**
- User → Groups (via memberIds array)
- Group → Expenses (via groupId)
- Group → GroupMembers (embedded)

**Many-to-Many Relationships:**
- Users ↔ Groups (through memberIds array)
- Users ↔ Expenses (through ExpenseSplits)

**Audit Relationships:**
- GroupJoinRequests (tracks join workflow)
- Notifications (tracks system events)
- Settlements (tracks payment history)

## Firestore Schema Design

### 1. Collection Structure

#### Core Collections
```
/users/{userId}
/groups/{groupId}
/expenses/{expenseId}
/settlements/{settlementId}
/notifications/{notificationId}
/groupJoinRequests/{requestId}
/groupInvites/{inviteId}
/friendRequests/{requestId}
/friends/{friendId}
```

#### Document Structure Examples

**Group Document:**
```json
{
  "name": "Trip to Japan",
  "description": "Vacation expenses",
  "currency": "PHP",
  "createdAt": "2024-01-15T10:30:00Z",
  "createdBy": "user123",
  "inviteCode": "ABC123",
  "memberIds": ["user123", "user456", "user789"],
  "members": {
    "user123": {
      "name": "John Doe",
      "email": "john@example.com",
      "isAdmin": true,
      "joinedAt": "2024-01-15T10:30:00Z"
    },
    "user456": {
      "name": "Jane Smith",
      "email": "jane@example.com",
      "isAdmin": false,
      "joinedAt": "2024-01-15T11:00:00Z"
    }
  },
  "totalExpenses": 1500.00
}
```

**Expense Document:**
```json
{
  "groupId": "group123",
  "description": "Dinner at Restaurant",
  "amount": 120.00,
  "currency": "PHP",
  "date": "2024-01-15T19:00:00Z",
  "createdAt": "2024-01-15T19:30:00Z",
  "createdBy": "user123",
  "updatedAt": "2024-01-15T19:30:00Z",
  "paidBy": "user123",
  "splitType": "Equal Split",
  "splitBetween": [
    {
      "userId": "user123",
      "userName": "John Doe",
      "share": 40.00,
      "isPaid": false
    },
    {
      "userId": "user456",
      "userName": "Jane Smith",
      "share": 40.00,
      "isPaid": false
    },
    {
      "userId": "user789",
      "userName": "Bob Wilson",
      "share": 40.00,
      "isPaid": false
    }
  ],
  "category": "FOOD",
  "notes": "Great restaurant!",
  "attachments": ["https://storage.googleapis.com/receipts/receipt1.jpg"]
}
```

### 2. Schema Optimization Strategies

#### Denormalization for Performance

**User Data Denormalization:**
```kotlin
// Instead of joining with users collection
val expenseData = hashMapOf(
    "paidBy" to paidBy,           // userId
    "paidByName" to paidByName,   // Denormalized for performance
    "splitBetween" to splitBetween.map { split ->
        mapOf(
            "userId" to split.userId,
            "userName" to split.userName,  // Denormalized
            "share" to split.share,
            "isPaid" to split.isPaid
        )
    }
)
```

**Benefits:**
- **Offline Access**: User names available without network
- **Query Performance**: No joins required for expense display
- **Reduced Reads**: Fewer Firestore document reads

**Trade-offs:**
- **Data Consistency**: Names may become stale
- **Storage Cost**: Duplicate data storage
- **Update Complexity**: Need to update multiple documents

#### Array Fields for Efficient Queries

**MemberIds Array:**
```kotlin
"memberIds": ["user123", "user456", "user789"]
```

**Benefits:**
- **Efficient Queries**: `whereArrayContains("memberIds", userId)`
- **Index Optimization**: Single field index for membership checks
- **Real-time Updates**: Easy to add/remove members

#### Embedded Documents for Related Data

**Group Members:**
```kotlin
"members": {
    "user123": {
        "name": "John Doe",
        "email": "john@example.com",
        "isAdmin": true,
        "joinedAt": "2024-01-15T10:30:00Z"
    }
}
```

**Benefits:**
- **Atomic Updates**: Single document update for member changes
- **Consistency**: Member data always in sync with group
- **Performance**: No subcollection queries needed

## Firestore Security Rules

### 1. Authentication & Authorization

#### User-Level Security
```javascript
// Users can only access their own data
match /users/{userId} {
  allow read: if request.auth != null;
  allow create, update: if request.auth != null && request.auth.uid == userId;
}
```

#### Group Membership Validation
```javascript
function isGroupMember(groupId) {
  let group = get(/databases/$(database)/documents/groups/$(groupId));
  return request.auth.uid in group.data.memberIds;
}

function isGroupAdmin(groupId) {
  let group = get(/databases/$(database)/documents/groups/$(groupId));
  let members = group.data.members;
  return members[request.auth.uid].isAdmin == true;
}
```

### 2. Data Access Control

#### Expense Access Rules
```javascript
match /expenses/{expenseId} {
  // Only group members can read expenses
  allow read: if request.auth != null && isGroupMember(expenseGroupId());
  
  // Only group members can create expenses
  allow create: if request.auth != null && isGroupMember(expenseGroupId());
  
  // Creator or admin can update expenses
  allow update: if request.auth != null && (
    (request.auth.uid == resource.data.createdBy) ||
    isGroupAdmin(expenseGroupId())
  );
  
  // Only admins can delete expenses
  allow delete: if request.auth != null && isGroupAdmin(expenseGroupId());
}
```

#### Notification Access Rules
```javascript
match /notifications/{notificationId} {
  // Users can only read their own notifications
  allow read: if request.auth != null && 
              request.auth.uid == resource.data.recipientId;
  
  // Users can only update read status
  allow update: if request.auth != null && 
               request.auth.uid == resource.data.recipientId &&
               request.resource.data.diff(resource.data).affectedKeys().hasOnly(['isRead']);
}
```

### 3. Query Limitations

#### Rate Limiting
```javascript
// Global query limits
match /{collection}/{document=**} {
  allow list: if request.auth != null && request.query.limit <= 50;
}

// User query limits
match /users/{userId} {
  allow list: if request.auth != null && 
              request.query.limit <= 10 &&
              request.query.filters.size() == 1 &&
              'email' in request.query.filters;
}
```

## Indexing Strategy

### 1. Composite Indexes

#### Expense Queries
```json
{
  "collectionGroup": "expenses",
  "queryScope": "COLLECTION",
  "fields": [
    { "fieldPath": "groupId", "order": "ASCENDING" },
    { "fieldPath": "createdAt", "order": "DESCENDING" }
  ]
}
```

**Usage:**
```kotlin
firestore.collection("expenses")
    .whereEqualTo("groupId", groupId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .get()
```

#### Notification Queries
```json
{
  "collectionGroup": "notifications",
  "queryScope": "COLLECTION",
  "fields": [
    { "fieldPath": "recipientId", "order": "ASCENDING" },
    { "fieldPath": "createdAt", "order": "DESCENDING" }
  ]
}
```

### 2. Array Indexes

#### Group Membership
```json
{
  "collectionGroup": "groups",
  "fieldPath": "memberIds",
  "indexes": [
    {
      "order": "ASCENDING",
      "queryScope": "COLLECTION"
    },
    {
      "arrayConfig": "CONTAINS",
      "queryScope": "COLLECTION"
    }
  ]
}
```

**Usage:**
```kotlin
firestore.collection("groups")
    .whereArrayContains("memberIds", currentUserId)
    .get()
```

### 3. Index Optimization

#### Query Performance Considerations
- **Compound Indexes**: Multiple field combinations for complex queries
- **Array Indexes**: Efficient membership checks
- **Ordering Indexes**: Support for sorted queries
- **Limit Optimization**: Indexes support query limits

## Data Persistence Strategies

### 1. Repository Pattern Implementation

#### ExpenseRepository Data Persistence
```kotlin
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
    "splitBetween" to splitBetween
)

firestore.collection("expenses")
    .add(expenseData)
    .await()
```

#### Transaction Management
```kotlin
firestore.runTransaction { transaction ->
    val groupDoc = transaction.get(groupRef)
    val currentTotal = groupDoc.getDouble("totalExpenses") ?: 0.0
    transaction.update(groupRef, "totalExpenses", currentTotal + amount)
}.await()
```

### 2. Real-time Data Synchronization

#### Firestore Listeners
```kotlin
val subscription = groupsCollection
    .whereArrayContains("memberIds", currentUser.uid)
    .addSnapshotListener { snapshot, error ->
        if (error != null) {
            close(error)
            return@addSnapshotListener
        }
        
        val groups = snapshot.documents.mapNotNull { doc ->
            // Parse group data
        }
        trySend(groups)
    }
```

#### Offline Support
```kotlin
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()
firestore.firestoreSettings = settings
```

### 3. Data Validation

#### Input Validation
```kotlin
private fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun isValidPassword(password: String): Boolean {
    return password.length >= 6
}
```

#### Business Logic Validation
```kotlin
// Verify group membership before expense creation
val groupDoc = firestore.collection("groups").document(groupId).get().await()
val members = groupDoc.data?.get("members") as? Map<*, *>
if (members?.containsKey(currentUser.uid) != true) {
    throw Exception("User is not a member of this group")
}
```

## Performance Optimization

### 1. Query Optimization

#### Efficient Group Queries
```kotlin
// Use memberIds array for efficient membership checks
firestore.collection("groups")
    .whereArrayContains("memberIds", currentUserId)
    .get()
    .await()
```

#### Pagination Support
```kotlin
// Limit query results for performance
firestore.collection("expenses")
    .whereEqualTo("groupId", groupId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
    .limit(20)
    .get()
    .await()
```

### 2. Batch Operations

#### Group Deletion with Cleanup
```kotlin
val batch = firestore.batch()

// Delete all expenses for this group
expensesSnapshot.documents.forEach { doc ->
    batch.delete(firestore.collection("expenses").document(doc.id))
}

// Delete the group
batch.delete(groupsCollection.document(groupId))

// Commit the batch
batch.commit().await()
```

#### Settlement Recording
```kotlin
firestore.runTransaction { txn ->
    // Update expense splits
    val expRef = firestore.collection("expenses").document(expense.id)
    txn.update(expRef, "splitBetween", updatedSplits)
    
    // Create settlement record
    val settlementRef = firestore.collection("settlements").document()
    txn.set(settlementRef, settlementData)
}.await()
```

### 3. Caching Strategy

#### Local Data Caching
```kotlin
// Firestore offline persistence
val settings = FirebaseFirestoreSettings.Builder()
    .setPersistenceEnabled(true)
    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
    .build()
```

#### Memory Caching
```kotlin
// ViewModel-level caching
private var groupBalances by mutableStateOf<Map<String, Double>>(emptyMap())

// Cache balances for performance
groups.forEach { group ->
    viewModelScope.launch {
        val summary = settlementService.getSettlementSummary(group.id)
        val balance = summary.firstOrNull { it.userId == currentUserId }?.netBalance ?: 0.0
        groupBalances = groupBalances + (group.id to balance)
    }
}
```

## Data Migration & Versioning

### 1. Schema Evolution

#### Backward Compatibility
```kotlin
// Handle missing fields gracefully
val expense = Expense(
    id = doc.id,
    groupId = data["groupId"] as? String ?: "",
    description = data["description"] as? String ?: "",
    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
    currency = data["currency"] as? String ?: "PHP",
    date = (data["date"] as? Timestamp) ?: Timestamp.now(),
    paidBy = data["paidBy"] as? String ?: "",
    paidByName = data["paidByName"] as? String ?: "Unknown User",
    splitBetween = parseSplits(data["splitBetween"]),
    category = parseCategory(data["category"]),
    notes = data["notes"] as? String ?: "",
    attachments = (data["attachments"] as? List<String>) ?: emptyList()
)
```

#### Field Addition Strategy
```kotlin
// New fields with default values
val category = try {
    ExpenseCategory.valueOf((data["category"] as? String)?.uppercase() ?: "OTHER")
} catch (e: Exception) {
    ExpenseCategory.OTHER
}
```

### 2. Data Validation & Integrity

#### Type Safety
```kotlin
// Safe type casting with fallbacks
val amount = (data["amount"] as? Number)?.toDouble() ?: 0.0
val timestamp = (data["createdAt"] as? Timestamp) ?: Timestamp.now()
val stringList = (data["attachments"] as? List<String>) ?: emptyList()
```

#### Business Rule Validation
```kotlin
// Validate expense splits
private fun validateSplits(splits: List<ExpenseSplit>, totalAmount: Double): Boolean {
    val splitTotal = splits.sumOf { it.share }
    return (splitTotal - totalAmount).absoluteValue < 0.01 // Allow for floating point precision
}
```

## Error Handling & Recovery

### 1. Data Access Errors

#### Graceful Degradation
```kotlin
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
    emit(emptyList()) // Graceful degradation
}
```

#### Retry Mechanisms
```kotlin
suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 100,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms", e)
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
    }
    return block() // Last attempt
}
```

### 2. Data Consistency

#### Transaction Rollback
```kotlin
firestore.runTransaction { transaction ->
    try {
        // Update expense
        val expRef = firestore.collection("expenses").document(expense.id)
        transaction.update(expRef, expenseData)
        
        // Update group total
        val groupRef = firestore.collection("groups").document(groupId)
        val groupDoc = transaction.get(groupRef)
        val currentTotal = groupDoc.getDouble("totalExpenses") ?: 0.0
        transaction.update(groupRef, "totalExpenses", currentTotal + amount)
        
    } catch (e: Exception) {
        // Transaction automatically rolls back
        throw e
    }
}.await()
```

## Summary

The Fairr data architecture demonstrates sophisticated design with:

**Data Model Strengths:**
1. **Flexible Relationships**: Complex many-to-many relationships handled efficiently
2. **Denormalization Strategy**: Performance-optimized with denormalized user data
3. **Type Safety**: Comprehensive null safety and type validation
4. **Extensibility**: Easy to add new fields and features

**Firestore Schema Strengths:**
1. **Security-First**: Comprehensive security rules with role-based access
2. **Performance Optimized**: Strategic indexing and query optimization
3. **Real-time Capable**: Efficient listeners and offline support
4. **Scalable Design**: Handles complex group and expense relationships

**Persistence Strategy Strengths:**
1. **Transaction Management**: Atomic operations for data consistency
2. **Error Recovery**: Graceful degradation and retry mechanisms
3. **Offline Support**: Full offline capability with sync
4. **Data Validation**: Comprehensive input and business rule validation

**Performance Optimizations:**
1. **Efficient Queries**: Strategic indexing and query patterns
2. **Batch Operations**: Optimized bulk operations
3. **Caching Strategy**: Multi-level caching for performance
4. **Memory Management**: Proper cleanup and lifecycle management

## Next Steps

**Phase 6: Testing and Quality Assurance** will focus on:
- Unit testing strategies and coverage
- Integration testing approaches
- UI testing and automation
- Performance testing and optimization
- Code quality metrics and analysis 